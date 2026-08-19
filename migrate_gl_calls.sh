#!/bin/bash

# Migration script for OpenGL facade API migration
# This script migrates from JOGL direct calls to GlRenderer.api facade

# List of files to migrate (excluding already completed ones)
files=(
    "client/src/main/java/rt4/GlTexture.java"
    "client/src/main/java/rt4/GlFont.java"
    "client/src/main/java/rt4/GlSolidColorTexture.java"
    "client/src/main/java/rt4/GlVertexBufferObject.java"
    "client/src/main/java/rt4/ParticleSystem.java"
    "client/src/main/java/rt4/SpecularMaterialRenderer.java"
    "client/src/main/java/rt4/WaterMaterialRenderer.java"
    "client/src/main/java/rt4/UnderwaterMaterialRenderer.java"
    "client/src/main/java/rt4/SceneGraph.java"
    "client/src/main/java/rt4/gl/JoglBackend.java"
)

echo "Starting OpenGL facade API migration..."
echo "This will migrate gl.glX() and GlRenderer.gl.glX() patterns to GlRenderer.api.glX()"
echo ""

for file in "${files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "WARNING: File not found: $file"
        continue
    fi

    echo "Processing: $file"

    # Backup original file
    cp "$file" "$file.bak"

    # Count original GL calls (before migration)
    gl_direct_count=$(grep -o '\bgl\.gl[A-Z]' "$file" | wc -l)
    gl_renderer_count=$(grep -o 'GlRenderer\.gl\.gl[A-Z]' "$file" | wc -l)
    total_before=$((gl_direct_count + gl_renderer_count))

    # Step 1: Replace local gl variable calls: gl.glX() -> GlRenderer.api.glX()
    sed -i 's/\bgl\.gl\([A-Z][a-zA-Z0-9]*\)/GlRenderer.api.gl\1/g' "$file"

    # Step 2: Replace GlRenderer.gl.glX() -> GlRenderer.api.glX()
    sed -i 's/GlRenderer\.gl\.gl\([A-Z][a-zA-Z0-9]*\)/GlRenderer.api.gl\1/g' "$file"

    # Step 3: Remove local GL2 variable declarations
    # Pattern: @Pc(X) GL2 local|gl = GlRenderer.gl;
    sed -i '/^[[:space:]]*@Pc([0-9]*)[[:space:]]*GL2[[:space:]]*\(gl\|local[0-9]*\)[[:space:]]*=[[:space:]]*GlRenderer\.gl;/d' "$file"

    # Count migrated calls
    api_count=$(grep -o 'GlRenderer\.api\.gl[A-Z]' "$file" | wc -l)

    echo "  - Migrated $total_before GL calls -> $api_count API calls"
    echo "  - Backup saved to: $file.bak"
    echo ""
done

echo ""
echo "Migration complete!"
echo ""
echo "Next steps:"
echo "1. Review the changes with: git diff"
echo "2. Build the project: ./gradlew :client:compileJava --console=plain"
echo "3. If successful, remove backups: find . -name '*.bak' -delete"
echo "4. Commit changes with appropriate message"
