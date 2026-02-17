#!/bin/bash
# ═══════════════════════════════════════════════════════
# Compile .idl files → Java stubs
#
# Uses JacORB IDL compiler (works with Java 21+)
# ═══════════════════════════════════════════════════════

IDL_DIR="src/main/idl"
OUT_DIR="target/generated-sources/idl"

mkdir -p "$OUT_DIR"

echo "╔══════════════════════════════════════════╗"
echo "║   IDL Compiler                           ║"
echo "╚══════════════════════════════════════════╝"
echo ""
echo "Source:  $IDL_DIR"
echo "Output:  $OUT_DIR"
echo ""

# First ensure dependencies are available
if [ ! -d "target/dependency" ]; then
    echo "Downloading JacORB dependencies..."
    mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -q
fi

# Build classpath from dependencies
JACORB_CP=$(find target/dependency -name "*.jar" | tr '\n' ':')

# Method 1: JacORB IDL compiler (Java 21+)
if [ -n "$JACORB_CP" ]; then
    echo "Using JacORB IDL compiler...$JACORB_CP"
    for idl_file in "$IDL_DIR"/*.idl; do
        echo "  Compiling: $(basename "$idl_file")"
        java -cp "$JACORB_CP" org.jacorb.idl.parser \
            -d "$OUT_DIR" \
            -i2jpackage :FleetManagement \
            "$idl_file"

        if [ $? -eq 0 ]; then
            echo "  ✓ OK"
        else
            echo "  ✗ Failed, trying alternative..."
            # Fallback: try JDK idlj if available
            if command -v idlj &> /dev/null; then
                echo "  Using JDK idlj..."
                idlj -fall -td "$OUT_DIR" "$idl_file"
            else
                echo "  ERROR: No IDL compiler available"
                exit 1
            fi
        fi
    done
else
    # Method 2: JDK idlj (Java 8-10 only)
    if command -v idlj &> /dev/null; then
        echo "Using JDK idlj compiler..."
        for idl_file in "$IDL_DIR"/*.idl; do
            echo "  Compiling: $(basename "$idl_file")"
            idlj -fall -td "$OUT_DIR" "$idl_file"
        done
    else
        echo "ERROR: Neither JacORB nor JDK IDL compiler found."
        echo "Run 'mvn dependency:copy-dependencies' first."
        exit 1
    fi
fi

echo ""
echo "Generated files:"
find "$OUT_DIR" -name "*.java" | sort | while read f; do
    echo "  $f"
done
echo ""
echo "Done! Now run: mvn compile"