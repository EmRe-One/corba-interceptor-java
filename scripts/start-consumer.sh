#!/bin/bash
# ═══════════════════════════════════════════════════════
# Start VehicleTracker Consumer (Client)
# ═══════════════════════════════════════════════════════

NS_HOST=${1:-localhost}
NS_PORT=${2:-2809}
JAR="../demo/target/demo-1.0-SNAPSHOT.jar"

if [ ! -f "$JAR" ]; then
    echo "ERROR: $JAR not found. Run 'mvn clean package -DskipTests' first."
    exit 1
fi

# Optional: Add monitor agent to classpath
MONITOR_JAR="../agent/target/agent-1.0-SNAPSHOT.jar"
MONITOR_OPTS=""
if [ -f "$MONITOR_JAR" ]; then
    JAR="$JAR:$MONITOR_JAR"
    MONITOR_OPTS="-Dorg.omg.PortableInterceptor.ORBInitializerClass.tr.akguel.interceptor.MonitorORBInitializer= -Dmonitor.api.url=https://corba-interceptor-web.test/api -Dmonitor.ssl.trust-all=true"
    echo "[+] CORBA Monitor agent detected — interceptors enabled"
fi

java -cp "$JAR" $MONITOR_OPTS \
    tr.akguel.client.Consumer "$NS_HOST" "$NS_PORT"