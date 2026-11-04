# 📱 YAOITRON v1.1

> ตัวดาวน์โหลดและแปลงไฟล์มีเดียจาก YouTube และแหล่งอื่นๆ บน Android — ขับเคลื่อนโดย `yt-dlp` และ `ffmpeg`

[![Build Status](https://github.com/tenal970/TY_Downloader-Converter_1.0/actions/workflows/build.yml/badge.svg)](https://github.com/tenal970/TY_Downloader-Converter_1.0/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/tenal970/TY_Downloader-Converter_1.0?label=latest%20release&color=magenta)](https://github.com/tenal970/TY_Downloader-Converter_1.0/releases/latest)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blueviolet)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ⬇️ Download

### 🚀 ดาวน์โหลด APK ได้ที่นี่เลย!

**[➡️ ไปที่หน้า Releases เพื่อดาวน์โหลด APK ล่าสุด](https://github.com/tenal970/TY_Downloader-Converter_1.0/releases/latest)**

> ⚠️ **หมายเหตุ:** ก่อนติดตั้งต้องเปิดใช้งาน "ติดตั้งแอปจากแหล่งที่ไม่รู้จัก" ในการตั้งค่า Android

---

## ✨ Features

| ฟีเจอร์ | รายละเอียด |
|---------|-----------|
| 📥 **ดาวน์โหลดวิดีโอ** | รองรับ YouTube, Twitter, TikTok และอีกมากมาย |
| 🎵 **แยก Audio** | ดาวน์โหลดเฉพาะเสียงในรูปแบบ MP3, AAC, Opus |
| 🔄 **แปลงไฟล์** | แปลงไฟล์มีเดียในเครื่องด้วย ffmpeg |
| ⬇️ **เลือกคุณภาพ** | เลือก Resolution และ Format ได้ตามต้องการ |
| 📂 **เลือกโฟลเดอร์** | กำหนดปลายทางการบันทึกได้เอง |
| 🔄 **อัปเดตอัตโนมัติ** | ระบบตรวจสอบและดาวน์โหลด binary ล่าสุด |
| 📊 **แสดง Log** | ดู Progress และ Log แบบ Real-time |
| 🎨 **Dark Theme** | ธีมสีม่วงเข้มสวยงาม |

---

## 📋 Requirements

- **Android:** 8.0 Oreo (API Level 26) ขึ้นไป
- **สิทธิ์ที่ต้องการ:**
  - อินเทอร์เน็ต (ดาวน์โหลด)
  - เข้าถึง Storage (บันทึกไฟล์)
  - การแจ้งเตือน (Foreground Service)

---

## 🛠️ Build from Source

```bash
# Clone repository
git clone https://github.com/tenal970/TY_Downloader-Converter_1.0.git
cd TY_Downloader-Converter_1.0

# Build APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/
```

**ข้อกำหนด:**
- Android Studio Hedgehog+ หรือ JDK 17
- Android SDK API 35

---

## 🏗️ Architecture

```
app/src/main/java/com/tenal/yaoitron/
├── YaoitronApplication.kt       # Application class
├── data/
│   ├── MediaModels.kt           # Data models
│   ├── LogManager.kt            # Thread-safe log management
│   └── WallpaperManager.kt      # Dynamic wallpaper
├── engine/
│   ├── DownloaderEngine.kt      # yt-dlp wrapper
│   ├── ConverterEngine.kt       # ffmpeg wrapper
│   ├── MediaScanner.kt          # Media scanning
│   └── UpdateManager.kt         # Binary updater (SHA256 verified)
├── service/
│   └── DownloadForegroundService.kt  # Background download service
└── ui/
    ├── MainActivity.kt
    ├── theme/                   # Jetpack Compose theming
    ├── components/              # Reusable UI components
    ├── screens/                 # App screens
    └── viewmodel/               # ViewModels (MVVM)
```

---

## 📦 Tech Stack

- **Language:** Kotlin 100%
- **UI:** Jetpack Compose + Material3
- **Architecture:** MVVM + Clean Architecture
- **Background Tasks:** Kotlin Coroutines + Flow
- **Media Engine:** yt-dlp + ffmpeg (downloaded at runtime)
- **Build:** Gradle Kotlin DSL

---

## 🔄 Changelog

### v1.1 (Current)
- 🎉 รีแบรนด์เป็น **YAOITRON** พร้อมไอคอนตัวละครใหม่
- 🔒 Thread-safe LogManager ด้วย `StateFlow.update`
- 🌐 WallpaperManager รันบน `Dispatchers.IO`
- 🔐 SHA256 Checksum verification ใน UpdateManager
- 🔧 แก้ไข DownloaderEngine path handling
- 🛠️ แยก ViewModel หลายไฟล์ชัดเจน
- 📋 Network Security Config สำหรับ Binary Downloads
- ⚙️ GitHub Actions Auto-Release

### v1.0
- 🎬 ดาวน์โหลดวิดีโอและ Audio ด้วย yt-dlp
- 🔄 แปลงไฟล์ด้วย ffmpeg
- 📱 Jetpack Compose UI
- 🌙 Dark Theme สีม่วง

---

## 📄 License

```
MIT License — Copyright (c) 2026 tenal970
```

ดูรายละเอียดได้ที่ [LICENSE](LICENSE)

---

<div align="center">
  <strong>⬡ YAOITRON</strong> — Made with ❤️ and bat wings 🦇
</div>
