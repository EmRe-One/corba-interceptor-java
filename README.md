# CORBA Monitor — Java Interceptor Agent

Java-Agent, der sich per Portable Interceptor in bestehende CORBA-Anwendungen einklinkt und den gesamten Traffic (Requests, Replies, Exceptions) an die CORBA Monitor Laravel-API sendet.

## Architektur

```
┌─────────────────────┐         ┌─────────────────────┐
│  Deine CORBA App    │         │  CORBA Monitor API   │
│                     │  HTTP   │  (Laravel)           │
│  ┌───────────────┐  │ ──────> │                      │
│  │ Client        │  │  JSON   │  POST /api/traffic   │
│  │ Interceptor   │  │         │  POST /api/traffic/  │
│  ├───────────────┤  │         │       batch          │
│  │ Server        │  │         │  POST /api/nameserver│
│  │ Interceptor   │  │         │       /scan          │
│  ├───────────────┤  │         └─────────┬────────────┘
│  │ Nameserver    │  │                   │
│  │ Scanner       │  │                   ▼
│  └───────────────┘  │         ┌─────────────────────┐
│         │           │         │  Dashboard           │
│         ▼           │         │  (Livewire/Flux UI)  │
│  ┌───────────────┐  │         └─────────────────────┘
│  │ CORBA         │  │
│  │ Nameserver    │  │
│  └───────────────┘  │
└─────────────────────┘
```

## Build

```bash
mvn clean package
```

Erzeugt: `target/corba-interceptor-1.0.0.jar` (Fat-JAR mit allen Dependencies)

## Verwendung

### Modus 1: Embedded (empfohlen)

Die JAR wird zur bestehenden CORBA-Anwendung hinzugefügt. Alle Interceptors werden automatisch beim ORB-Start registriert.

```bash
java \
  -Dorg.omg.PortableInterceptor.ORBInitializerClass.com.corbamonitor.interceptor.MonitorORBInitializer= \
  -Dmonitor.api.url=http://dein-monitor-server:8080/api \
  -Dmonitor.nameserver.host=dein-nameserver \
  -Dmonitor.nameserver.port=2809 \
  -cp "deine-app.jar:corba-interceptor-1.0.0.jar" \
  com.deineapp.Main
```

Das ist der Standardweg für Portable Interceptors — der ORBInitializer wird vom ORB beim Start automatisch aufgerufen und registriert:
- **ClientRequestInterceptor** — fängt alle ausgehenden Calls ab (Consumer-Seite)
- **ServerRequestInterceptor** — fängt alle eingehenden Calls ab (Supplier-Seite)

### Modus 2: Standalone

Der Agent startet einen eigenen ORB, verbindet sich mit dem Nameserver und scannt ihn periodisch:

```bash
java -jar corba-interceptor-1.0.0.jar
```

In diesem Modus werden **nur die Nameserver-Einträge** erfasst (kein Traffic, da kein CORBA-Traffic durch diesen ORB läuft). Nützlich für die Topology-Ansicht im Dashboard.

## Konfiguration

Konfiguration über `monitor.properties` (im Classpath), System-Properties oder Umgebungsvariablen:

| Property | Env-Variable | Default | Beschreibung |
|---|---|---|---|
| `monitor.api.url` | `CORBA_MONITOR_API_URL` | `http://localhost:8080/api` | Monitor API URL |
| `monitor.api.token` | `CORBA_MONITOR_API_TOKEN` | *(leer)* | Bearer-Token |
| `monitor.enabled` | `CORBA_MONITOR_ENABLED` | `true` | Agent an/aus |
| `monitor.capture.request` | `CORBA_MONITOR_CAPTURE_REQ` | `true` | Request-Daten erfassen |
| `monitor.capture.response` | `CORBA_MONITOR_CAPTURE_RES` | `true` | Response-Daten erfassen |
| `monitor.max.payload.bytes` | `CORBA_MONITOR_MAX_PAYLOAD` | `65536` | Max Payload-Größe |
| `monitor.batch.size` | `CORBA_MONITOR_BATCH_SIZE` | `50` | Batch-Größe |
| `monitor.flush.interval.ms` | `CORBA_MONITOR_FLUSH_MS` | `1000` | Flush-Intervall (ms) |
| `monitor.http.timeout.ms` | `CORBA_MONITOR_HTTP_TIMEOUT` | `5000` | HTTP-Timeout |
| `monitor.http.pool.size` | `CORBA_MONITOR_HTTP_POOL` | `4` | HTTP-Threads |
| `monitor.nameserver.host` | `CORBA_NAMESERVER_HOST` | `localhost` | Nameserver-Host |
| `monitor.nameserver.port` | `CORBA_NAMESERVER_PORT` | `2809` | Nameserver-Port |
| `monitor.scan.enabled` | `CORBA_MONITOR_SCAN_ENABLED` | `true` | Nameserver-Scan an/aus |
| `monitor.scan.interval.seconds` | `CORBA_MONITOR_SCAN_INTERVAL` | `30` | Scan-Intervall (s) |

### Priorität

System-Property > Umgebungsvariable > `monitor.properties` > Default

## Was wird erfasst?

### Traffic (via Portable Interceptors)

Für jeden CORBA-Call werden erfasst:

| Feld | Beschreibung |
|---|---|
| `request_id` | Eindeutige Request-ID |
| `operation` | CORBA-Operation (z.B. `getPosition`) |
| `interface_name` | IDL-Interface (z.B. `VehicleTracker`) |
| `repository_id` | Volle Repository-ID (z.B. `IDL:FleetManagement/VehicleTracker:1.0`) |
| `direction` | `request` oder `reply` |
| `status` | `success`, `error`, `timeout`, `exception` |
| `latency_ms` | Latenz in Millisekunden (bei Reply) |
| `request_data` | Serialisierte Request-Argumente (IDL in-params) |
| `response_data` | Serialisierte Response/Return-Werte |
| `error_message` | CORBA-Exception (z.B. `CORBA::TRANSIENT`) |
| `giop_version` | GIOP-Protokollversion |
| `interceptor_point` | `send_request`, `receive_reply`, `receive_exception`, etc. |
| `source_host/port` | Absender |
| `target_host/port` | Empfänger (aus IOR) |
| `context_data` | CORBA Service Contexts |

### Interceptor-Punkte

```
Consumer (Client)                    Supplier (Server)
─────────────────                    ─────────────────
send_request      ──── Request ────> receive_request_service_contexts
                                     receive_request
receive_reply     <──── Reply ─────  send_reply
receive_exception <── Exception ───  send_exception
```

Jeder Interceptor-Punkt erzeugt ein eigenes TrafficEvent. Bei einem erfolgreichen Call entstehen also typisch 4 Events (2 Client + 2 Server).

### Nameserver (via Scanner)

Periodisch wird der gesamte Naming-Tree traversiert:

| Feld | Beschreibung |
|---|---|
| `path` | Voller Pfad im Nameserver (z.B. `fleet/VehicleTracker`) |
| `name` | Name-Komponente |
| `kind` | Kind-Feld der NameComponent |
| `type` | `context` (Verzeichnis) oder `object` (Binding) |
| `ior` | Interoperable Object Reference |
| `is_alive` | Ob das Objekt erreichbar ist (`_non_existent()` Check) |

## Integration mit bestehendem Code

### Beispiel: Bestehender JacORB-Consumer

```java
// VOR der Änderung — deine bestehende App:
Properties props = new Properties();
props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
props.setProperty("ORBInitRef.NameService", "corbaloc::ns-host:2809/NameService");
ORB orb = ORB.init(args, props);

// Nameservice resolve, narrow, call...
```

```java
// NACH der Änderung — nur 1 Property hinzufügen:
Properties props = new Properties();
props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
props.setProperty("ORBInitRef.NameService", "corbaloc::ns-host:2809/NameService");

// ──── Diese Zeile hinzufügen ────
props.setProperty(
    "org.omg.PortableInterceptor.ORBInitializerClass.com.corbamonitor.interceptor.MonitorORBInitializer",
    ""
);
// ────────────────────────────────

ORB orb = ORB.init(args, props);
// Alles weitere bleibt gleich — Interceptors sind jetzt aktiv
```

### Beispiel: JVM-Property statt Code-Änderung

Wenn du den Quellcode nicht ändern willst/kannst:

```bash
# Einfach die JAR zum Classpath und das Property per -D setzen
java \
  -Dorg.omg.PortableInterceptor.ORBInitializerClass.com.corbamonitor.interceptor.MonitorORBInitializer= \
  -Dmonitor.api.url=http://monitor:8080/api \
  -cp "original-app.jar:corba-interceptor-1.0.0.jar" \
  com.original.Main
```

**Keine Code-Änderung nötig** — die Interceptors registrieren sich automatisch beim ORB-Start.

## Performance

Der Agent ist auf minimalen Overhead ausgelegt:

- **Non-blocking**: Interceptors schreiben nur in eine Queue, kein HTTP im Call-Pfad
- **Batching**: Events werden gebündelt gesendet (konfigurierbar)
- **Daemon-Threads**: Alle Agent-Threads sind Daemon-Threads und verhindern nicht das Beenden der JVM
- **Fail-safe**: Fehler im Agent werden gefangen und geloggt, brechen nie den CORBA-Call ab
- **Backpressure**: Queue-Overflow wird per Drop-Strategie behandelt

## Logs

```
logs/corba-monitor-agent.log    # Rotiert, max 7 Tage, 100MB total
```

Oder Konsole (stdout).