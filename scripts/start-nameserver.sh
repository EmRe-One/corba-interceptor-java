#!/bin/bash
# ═══════════════════════════════════════════════════════
# Start JacORB Naming Service on port 2809
# ═══════════════════════════════════════════════════════

PORT=${1:-2809}
JAR="../demo/target/demo-1.0-SNAPSHOT.jar"

if [ ! -f "$JAR" ]; then
    echo "ERROR: $JAR not found. Run 'mvn clean package -DskipTests' first."
    exit 1
fi

echo "╔══════════════════════════════════════════╗"
echo "║   JacORB Naming Service                  ║"
echo "║   Port: $PORT                            ║"
echo "╚══════════════════════════════════════════╝"
echo ""

java -cp "$JAR" \
    -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
    -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
    -DOAPort=$PORT \
    org.jacorb.naming.NameServer
