#!/bin/bash
# ═══════════════════════════════════════════════════════
# Start VehicleTracker Supplier (Server)
# ═══════════════════════════════════════════════════════

NS_HOST=${1:-localhost}
NS_PORT=${2:-2809}
JAR="target/corba-demo-1.0.0.jar"

if [ ! -f "$JAR" ]; then
    echo "ERROR: $JAR not found. Run 'mvn clean package -DskipTests' first."
    exit 1
fi

# Optional: Add monitor agent to classpath
MONITOR_JAR="../corba-interceptor/target/corba-interceptor-1.0.0.jar"
MONITOR_OPTS=""
if [ -f "$MONITOR_JAR" ]; then
    JAR="$JAR:$MONITOR_JAR"
    MONITOR_OPTS="-Dorg.omg.PortableInterceptor.ORBInitializerClass.com.corbamonitor.interceptor.MonitorORBInitializer= -Dmonitor.api.url=http://localhost:8080/api -Dmonitor.ssl.trust-all=true"
    echo "[+] CORBA Monitor agent detected — interceptors enabled"
fi

java -cp "$JAR" $MONITOR_OPTS \
    tr.akguel.server.Supplier "$NS_HOST" "$NS_PORT"