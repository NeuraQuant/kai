#!/bin/bash

echo "======================================"
echo "     Kai Library Verification"
echo "======================================"
echo ""

# Check if Gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Gradle wrapper not found!"
    exit 1
fi

echo "1. Building the library..."
./gradlew clean build -x test --quiet
if [ $? -eq 0 ]; then
    echo "   ✅ Build successful"
else
    echo "   ❌ Build failed"
    exit 1
fi

echo ""
echo "2. Running tests..."
./gradlew test --quiet
if [ $? -eq 0 ]; then
    echo "   ✅ All tests passed"
else
    echo "   ❌ Some tests failed"
    exit 1
fi

echo ""
echo "3. Creating JAR..."
./gradlew jar --quiet
if [ -f "build/libs/kai-1.0.0.jar" ]; then
    echo "   ✅ JAR created: build/libs/kai-1.0.0.jar"
    echo "   📦 JAR size: $(du -h build/libs/kai-1.0.0.jar | cut -f1)"
else
    echo "   ❌ JAR not found"
    exit 1
fi

echo ""
echo "4. Publishing to local Maven..."
./gradlew publishToMavenLocal --quiet
if [ $? -eq 0 ]; then
    echo "   ✅ Published to local Maven repository"
    echo "   📍 Location: ~/.m2/repository/io/kai/kai/1.0.0/"
else
    echo "   ❌ Publishing failed"
    exit 1
fi

echo ""
echo "5. Checking source files..."
file_count=$(find src/main/kotlin -name "*.kt" | wc -l | tr -d ' ')
echo "   📄 Kotlin source files: $file_count"

echo ""
echo "======================================"
echo "✨ Verification Complete!"
echo "======================================"
echo ""
echo "The Kai library is ready to use!"
echo ""
echo "Quick start:"
echo "  1. Add to your build.gradle.kts:"
echo "     implementation(\"io.kai:kai:1.0.0\")"
echo ""
echo "  2. Create an agent:"
echo "     val agent = Agent(LmStudioClient())"
echo "     val response = agent.chat(\"Hello!\")"
echo ""
echo "For more examples, see:"
echo "  - README.md"
echo "  - src/main/kotlin/Example.kt"
echo "  - RunExample.kt"