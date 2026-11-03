#!/bin/bash
set -e

echo "=================================================="
echo "  TY Downloader & Converter v3.1 APK Builder      "
echo "=================================================="

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

chmod +x gradlew

# Check Java
if ! command -v java &> /dev/null; then
    echo "⚠ ไม่พบ Java ในเครื่อง กำลังติดตั้ง openjdk-17..."
    if command -v pkg &> /dev/null; then
        pkg update -y && pkg install openjdk-17 -y
    fi
fi

echo "🚀 กำลังเริ่มคอมไพล์ APK (assembleRelease)..."
./gradlew assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

if [ -f "$APK_PATH" ]; then
    echo ""
    echo "=================================================="
    echo "✅ สำเร็จ! สร้าง APK เรียบร้อยแล้วที่:"
    echo "   $PROJECT_DIR/$APK_PATH"
    echo "=================================================="
    if command -v termux-open &> /dev/null; then
        echo "📲 กำลังเปิดตัวติดตั้ง APK บนมือถือ..."
        termux-open "$PROJECT_DIR/$APK_PATH" || true
    fi
else
    echo "❌ ไม่พบไฟล์ APK ที่เอาต์พุต ตรวจสอบ log ด้านบน"
    exit 1
fi
