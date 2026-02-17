#!/bin/bash
# ═══════════════════════════════════════════════════════
# Start full CORBA Demo (Nameserver + Supplier + Consumer)
# Requires: 3 terminal windows or tabs
# ═══════════════════════════════════════════════════════

set -e

NS_PORT=${1:-2809}
JAR="../demo/target/demo-1.0-SNAPSHOT.jar"

# Build if needed
if [ ! -f "$JAR" ]; then
    echo "Building project..."
    mvn package -DskipTests -q
fi

echo "╔══════════════════════════════════════════╗"
echo "║   CORBA Demo — Full Stack               ║"
echo "╚══════════════════════════════════════════╝"
echo ""
echo "Starting 3 components:"
echo "  1. Naming Service  (port $NS_PORT)"
echo "  2. Supplier        (VehicleTracker)"
echo "  3. Consumer        (Interactive Client)"
echo ""

# ── 1. Start Nameserver ──────────────────────────────
echo "[1/3] Starting Naming Service..."
java -cp "$JAR" \
    -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB \
    -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton \
    -DOAPort=$NS_PORT \
    org.jacorb.naming.NameServer &
NS_PID=$!
echo "  PID: $NS_PID"

# Wait for nameserver to be ready
sleep 2

# ── 2. Start Supplier ───────────────────────────────
echo "[2/3] Starting Supplier..."
java -cp "$JAR" \
    tr.akguel.server.Supplier localhost $NS_PORT &
SUP_PID=$!
echo "  PID: $SUP_PID"

# Wait for supplier to register
sleep 2

# ── 3. Start Consumer (foreground) ──────────────────
echo "[3/3] Starting Consumer..."
echo ""
java -cp "$JAR" \
    tr.akguel.client.Consumer localhost $NS_PORT

# ── Cleanup ─────────────────────────────────────────
echo ""
echo "Stopping Supplier (PID $SUP_PID)..."
kill $SUP_PID 2>/dev/null || true

echo "Stopping Naming Service (PID $NS_PID)..."
kill $NS_PID 2>/dev/null || true

echo "Done."