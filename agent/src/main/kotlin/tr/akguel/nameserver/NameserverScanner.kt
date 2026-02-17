package tr.akguel.nameserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.omg.CORBA.ORB
import org.omg.CORBA.Object
import org.omg.CORBA.portable.ObjectImpl
import org.omg.CosNaming.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tr.akguel.config.MonitorConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * Scans the CORBA Naming Service tree and reports discovered services
 * to the Monitor API. This serves as the "bridge" that the Laravel
 * CorbaNameserverService expects on port 9090.
 *
 * It also provides a minimal REST API for the Laravel app to query:
 * GET  /list?path=...         → list entries at path
 * GET  /resolve?path=...      → resolve a name to IOR
 * GET  /health                → health check
 */
class NameserverScanner(private val orb: ORB) {

    private val config: MonitorConfig = MonitorConfig.instance!!
    private val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
    private var rootContext: NamingContextExt? = null
    private var scheduler: ScheduledExecutorService? = null

    /**
     * Connect to the naming service and start periodic scanning.
     */
    fun start() {
        if (!config.scanEnabled) {
            log.info("Nameserver scanning disabled")
            return
        }

        try {
            connectToNameserver()

            scheduler = Executors.newSingleThreadScheduledExecutor { r: Runnable? ->
                val t = Thread(r, "corba-monitor-ns-scan")
                t.setDaemon(true)
                t
            }

            scheduler?.scheduleAtFixedRate(
                { this.scan() },
                0,
                config.scanIntervalSeconds.toLong(),
                TimeUnit.SECONDS
            )

            log.info(
                "Nameserver scanner started (interval={}s, host={}:{})",
                config.scanIntervalSeconds,
                config.nameserverHost,
                config.nameserverPort
            )
        } catch (e: Exception) {
            log.error("Failed to start nameserver scanner: {}", e.message)
        }
    }

    /**
     * Connect to the CORBA Naming Service.
     */
    @Throws(Exception::class)
    private fun connectToNameserver() {
        try {
            // Try resolve_initial_references first (standard)
            val nsObj: Object? = orb.resolve_initial_references("NameService")
            rootContext = NamingContextExtHelper.narrow(nsObj)
            log.info("Connected to NameService via resolve_initial_references")
        } catch (e: Exception) {
            // Fallback: try corbaloc
            val corbaloc = String.format(
                "corbaloc:iiop:%s:%d/NameService",
                config.nameserverHost, config.nameserverPort
            )
            log.info("Trying corbaloc: {}", corbaloc)
            val nsObj: Object? = orb.string_to_object(corbaloc)
            rootContext = NamingContextExtHelper.narrow(nsObj)
            log.info("Connected to NameService via corbaloc")
        }
    }

    /**
     * Scan the entire naming tree and report findings to the API.
     */
    fun scan() {
        if (rootContext == null) {
            try {
                connectToNameserver()
            } catch (e: Exception) {
                log.warn("Cannot connect to nameserver: {}", e.message)
                return
            }
        }

        try {
            val entries: MutableList<MutableMap<String, Any>?> = ArrayList<MutableMap<String, Any>?>()
            walkTree(rootContext as NamingContext, "", entries)

            log.info("Nameserver scan found {} entries", entries.size)

            // Send to API
            reportEntries(entries)
        } catch (e: Exception) {
            log.error("Nameserver scan failed: {}", e.message)
            rootContext = null // Force reconnect on next scan
        }
    }

    /**
     * Recursively walk the naming tree.
     */
    private fun walkTree(
        context: NamingContext,
        parentPath: String,
        entries: MutableList<MutableMap<String, Any>?>
    ) {
        try {
            val blh = BindingListHolder()
            val bih = BindingIteratorHolder()

            context.list(1000, blh, bih)

            for (binding in blh.value) {
                val nc = binding.binding_name[0]
                var name = nc.id
                if (nc.kind != null && !nc.kind.isEmpty()) {
                    name += "." + nc.kind
                }
                val fullPath = if (parentPath.isEmpty()) name else "$parentPath/$name"

                if (binding.binding_type == BindingType.ncontext) {
                    // It's a naming context (directory) — recurse
                    val entry: MutableMap<String, Any> = LinkedHashMap<String, Any>()
                    entry["path"] = fullPath
                    entry["name"] = nc.id
                    entry["kind"] = nc.kind
                    entry["type"] = "context"
                    entries.add(entry)

                    try {
                        val subName = arrayOf<NameComponent?>(nc)
                        val subObj = context.resolve(subName)
                        val subCtx = NamingContextHelper.narrow(subObj)
                        walkTree(subCtx, fullPath, entries)
                    } catch (e: Exception) {
                        log.debug("Cannot descend into {}: {}", fullPath, e.message)
                    }
                } else {
                    // It's an object binding
                    val entry: MutableMap<String, Any> = LinkedHashMap<String, Any>()
                    entry["path"] = fullPath
                    entry["name"] = nc.id
                    entry["kind"] = nc.kind
                    entry["type"] = "object"

                    // Resolve to get IOR
                    try {
                        val objName = arrayOf<NameComponent?>(nc)
                        val obj = context.resolve(objName)
                        if (obj != null) {
                            val ior: String = orb.object_to_string(obj)
                            entry["ior"] = ior

                            // Try to ping
                            var alive = false
                            try {
                                alive = !obj._non_existent()
                            } catch (ignored: Exception) {
                            }
                            entry["is_alive"] = alive

                            // Parse IOR for host/port
                            parseIOR(ior, entry)
                        }
                    } catch (e: Exception) {
                        e.message?.let { entry.put("error", it) }
                        entry["is_alive"] = false
                    }

                    entries.add(entry)
                }
            }

            // Handle binding iterator if present
            if (bih.value != null) {
                try {
                    val bh = BindingHolder()
                    while (bih.value.next_one(bh)) {
                        // Process remaining bindings (unlikely for most nameservers)
                        val nc = bh.value.binding_name[0]
                        val name = nc.id
                        val fullPath = if (parentPath.isEmpty()) name else "$parentPath/$name"

                        val entry: MutableMap<String, Any> = LinkedHashMap<String, Any>()
                        entry["path"] = fullPath
                        entry["name"] = nc.id
                        entry["kind"] = nc.kind
                        entry["type"] = if (bh.value.binding_type == BindingType.ncontext) "context" else "object"
                        entries.add(entry)
                    }
                    bih.value.destroy()
                } catch (ignored: Exception) {
                }
            }
        } catch (e: Exception) {
            log.error("Error walking tree at '{}': {}", parentPath, e.message)
        }
    }

    /**
     * Best-effort IOR parsing to extract host and port.
     */
    private fun parseIOR(ior: String?, entry: MutableMap<String, Any>) {
        if (ior == null || !ior.startsWith("IOR:")) return
        try {
            // JacORB can parse IOR internally
            val obj: Object? = orb.string_to_object(ior)
            if (obj is ObjectImpl) {
                val delegate = obj._get_delegate()
                // The delegate often has host/port info but the exact API varies
                // We'll extract from the IOR hex string as fallback
            }
        } catch (ignored: Exception) {
        }

        // Hex-based extraction (fallback)
        try {
            val hex = ior.substring(4)
            // IOR structure: byte_order + type_id_length + type_id + profiles...
            // This is simplified — full IOR parsing would need CDR decoding
            entry["ior_length"] = hex.length / 2
        } catch (ignored: Exception) {
        }
    }

    /**
     * Send discovered entries to the Monitor API.
     */
    private fun reportEntries(entries: MutableList<MutableMap<String, Any>?>) {
        try {
            val payload: MutableMap<String, Any> = LinkedHashMap<String, Any>()
            payload["source"] = "java-agent"
            payload["entries"] = entries
            payload["timestamp"] = Instant.now().toString()

            val json: String = gson.toJson(payload)
            val endpoint: String = config.apiBaseUrl + "/nameserver/report"

            val url = URI(endpoint).toURL()
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestMethod("POST")
            conn.setDoOutput(true)
            conn.setConnectTimeout(5000)
            conn.setReadTimeout(5000)
            conn.setRequestProperty("Content-Type", "application/json")

            val bytes = json.toByteArray(StandardCharsets.UTF_8)
            conn.setFixedLengthStreamingMode(bytes.size)

            conn.getOutputStream().use { os ->
                os.write(bytes)
            }
            val status = conn.getResponseCode()
            conn.disconnect()

            if (status in 200..<300) {
                log.info("Reported {} nameserver entries to API", entries.size)
            } else {
                log.warn("Nameserver report failed with HTTP {}", status)
            }
        } catch (e: IOException) {
            log.error("Failed to report nameserver entries: {}", e.message)
        }
    }

    fun stop() {
        scheduler?.shutdown()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(NameserverScanner::class.java)
    }
}