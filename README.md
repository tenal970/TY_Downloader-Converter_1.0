# 📱 TY Downloader & Converter v1.0 / v3.1 (Native Android App)

<p align="center">
  <img src="https://img.shields.io/badge/Android-API%2026%20to%2034-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android API 26-34" />
  <img src="https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge" alt="MIT License" /></a>
  <a href="https://github.com/tenal970/TY_Downloader-Converter_1.0/actions"><img src="https://img.shields.io/badge/CI%2FCD-Passing-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white" alt="Build Status" /></a>
</p>

แอปพลิเคชัน Android Native 100% พัฒนาด้วย **Kotlin + Jetpack Compose (Material 3)** สืบทอดและพัฒนาต่อยอดความสามารถทั้งหมดจาก `ytdl_gui` ให้ทำงานในรูปแบบแอปมือถือสมบูรณ์แบบ ติดตั้งและเปิดใช้งานได้ทันทีจากหน้าจอโฮม ไม่ต้องเปิด Terminal หรือรันเซิร์ฟเวอร์อีกต่อไป!

---

## 📋 ข้อกำหนดเบื้องต้น (Prerequisites)

- **อุปกรณ์ Android**: ระบบปฏิบัติการ Android 8.0 (API 26) ขึ้นไป (แนะนำ Android 11–14 / API 30–34)
- **สิทธิ์การใช้งาน (Permissions)**:
  - สิทธิ์การเข้าถึงพื้นที่จัดเก็บข้อมูล (Storage Permissions / All files access) สำหรับบันทึกไฟล์ลง `Downloads`, `Movies`, `Music`, หรือโฟลเดอร์ที่คุณกำหนดเอง
  - สิทธิ์การแจ้งเตือน (Notifications) และ Background Service เพื่อดาวน์โหลดหรือแปลงไฟล์ต่อเนื่องขณะสลับแอป
  - สิทธิ์การติดตั้งแอปที่ไม่รู้จัก (เมื่อต้องการให้อัปเดต APK อัตโนมัติ)
- **สำหรับการคอมไพล์จาก Source Code**:
  - JDK 17 (แนะนำ Temurin / OpenJDK 17)
  - Android SDK Platform 34 & Build-Tools

---

## 📸 ภาพตัวอย่างหน้าจอ (Screenshots & UI Preview)

```
┌─────────────────────────────────────────────────────────┐
│  TY Downloader & Converter                              │
│  [yt-dlp: v2024.04.09]  [FFmpeg: 6.0 Active]   [🔄 อัปเดต]│
├────────────────────────────┬────────────────────────────┤
│     ⬇️ แท็บ Downloader      │     🔄 แท็บ Converter      │
├────────────────────────────┴────────────────────────────┤
│  วางลิงก์วิดีโอ (YouTube / X / TikTok / FB / IG):        │
│  [ https://www.youtube.com/watch?v=...          ][ตรวจ] │
│                                                         │
│  ┌─ Thumbnail & Metadata Preview ──────────────────┐    │
│  │  [🖼️ Video Thumbnail]  Title: Video Name         │    │
│  │                       Uploader: Channel Name    │    │
│  │  [✅ ใช่ เริ่มดาวน์โหลด]     [❌ ยกเลิก]               │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  โหมด: (•) 🎬 วิดีโอ MP4      ( ) 🎵 เสียง MP3          │
│  ความละเอียด: [ 1080p (แนะนำ)                  ▼ ]      │
│  โฟลเดอร์เซฟ: [ Download ของเครื่อง (แนะนำ)       ▼ ]      │
│                                                         │
│  ┌─ Real-Time Terminal Log ────────────────────────┐    │
│  │ [download] 100% of 42.50MiB at 8.20MiB/s        │    │
│  │ 🎉 ดาวน์โหลดเสร็จสมบูรณ์!                        │    │
│  │ 📂 ไฟล์เซฟที่: /storage/emulated/0/Download/...   │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

---

## ✨ ไฮไลท์ฟีเจอร์หลัก (Key Features)

### 1. ⬇️ แท็บที่ 1: Downloader
- **รองรับทุกแพลตฟอร์มที่ yt-dlp รองรับ**: YouTube, X (Twitter), Facebook, TikTok, Instagram, Threads, Vimeo, Twitch ฯลฯ
- **Thumbnail Preview & Quick Confirmation**:
  - เมื่อวางลิงก์แล้วกดตรวจ แอปจะดึงรูป Thumbnail, ชื่อคลิป, ช่อง/ผู้สร้าง, ความยาว และเว็บไซต์มาแสดงให้ตรวจก่อน
  - มีปุ่มยืนยัน *"✅ ใช่ เริ่มโหลดเลย"* หรือ *"❌ ไม่ใช่ ยกเลิก"* ป้องกันโหลดผิดคลิป
- **เลือกโหมดดาวน์โหลด**:
  - 🎬 **วิดีโอ MP4**: เลือกระดับความละเอียดได้ตั้งแต่ ดีที่สุด (Best h264), 8K (4320p), 4K (2160p), 2K (1440p), 1080p, 720p, 480p, 360p (มีระบบเตือนและสลับ codec VP9 อัตโนมัติสำหรับคลิปเกิน 1080p)
  - 🎵 **เสียง MP3**: เลือกบิตเรตได้ 320 kbps (สูงสุด), 192 kbps (แนะนำ), 128 kbps (ประหยัดพื้นที่) พร้อมฝัง Thumbnail และ Metadata ID3 tag ให้อัตโนมัติ
- **เลือกโฟลเดอร์ปลายทาง**:
  - Downloads ของเครื่อง (แนะนำ)
  - Movies ของเครื่อง
  - Music ของเครื่อง
  - Pictures ของเครื่อง
  - กำหนดโฟลเดอร์เอง (Custom Path ผ่านตัวเลือกปลอดภัย)
- **แชร์ลิงก์เข้าแอปได้ทันที (Android Share Intent)**:
  - ขณะดูคลิปใน YouTube, Twitter/X, TikTok หรือเบราว์เซอร์ กดปุ่ม "แชร์ (Share)" แล้วเลือก **TY Downloader & Converter** แอปจะเปิดขึ้นมาและตรวจลิงก์ให้อัตโนมัติทันที

---

### 2. 🔄 แท็บที่ 2: File Converter
- **เลือกไฟล์จากเครื่องได้โดยตรง**: ผ่านตัวเลือกไฟล์มาตรฐานของ Android (แกลเลอรี, Download, File Manager)
- **แปลงภาพ (Image)**:
  - ต้นทาง: JPG, PNG, WebP, BMP, TIFF, GIF, AVIF, HEIC, HEIF
  - ปลายทาง: JPG, PNG, WebP, BMP, TIFF, GIF
  - ตั้งค่าขนาดได้ (Width, Height px โดยรักษาสัดส่วนภาพ ไม่ยืดภาพให้เสียรูป)
  - ตั้งค่าคุณภาพ: สูง, กลาง, ประหยัดพื้นที่
- **แปลงเสียง (Audio)**:
  - ต้นทาง: MP3, M4A, AAC, WAV, FLAC, OGG, OPUS, WMA, AIFF, ALAC
  - ปลายทาง: MP3, M4A, WAV, FLAC, OGG, OPUS
  - ปรับ Bitrate อัตโนมัติตามระดับคุณภาพ
- **แปลงวิดีโอ (Video)**:
  - ต้นทาง: MP4, MKV, WebM, MOV, AVI, WMV, FLV, 3GP, TS ฯลฯ
  - ปลายทาง: MP4, MKV, WebM, MOV, AVI
  - **แปลงวิดีโอเป็นภาพเคลื่อนไหว GIF**:
    - ตั้งค่าความกว้าง px (64–1280 px)
    - ปรับ FPS (1–20)
    - ตั้งเวลาเริ่มต้น (วินาที)
    - ตั้งความยาว GIF (สูงสุด 30 วินาที)
    - ใช้ฟิลเตอร์พิเศษ `2-pass palettegen lanczos` เพื่อให้ GIF มีสีสันสดใส ภาพคมชัด ไม่แตก
  - **ดึงเสียงจากวิดีโอ (Audio Extraction)**: แปลงคลิปวิดีโอเป็น MP3, M4A, WAV, FLAC, OGG, OPUS ได้ในคลิกเดียว

---

### 3. 🛡️ สถาปัตยกรรมความปลอดภัยและ Native Binaries
- **จัดการ Binary Assets ปลอดภัย**: รวม native runtime สำหรับ `yt-dlp` และ `ffmpeg` ผ่าน `youtubedl-android` พร้อมระบบ fallback โหลดจาก GitHub runtime ป้องกันแอป Crash
- **Background Startup Thread**: ฟังก์ชัน setup native binaries ทั้งหมดรันบน `Dispatchers.IO` ป้องกันปัญหา ANR (Application Not Responding) บน Android 14
- **SHA-256 Checksum Verification**: ตรวจสอบความถูกต้องและป้องกันมัลแวร์สำหรับไฟล์อัปเดต APK ก่อนติดตั้ง
- **Foreground Service**: รันดาวน์โหลดต่อเนื่องในพื้นหลังพร้อมการแจ้งเตือนแถบ Notification ตามข้อกำหนด Android 14 (API 34)

---

### 4. 🖼️ เปลี่ยนวอลเปเปอร์ได้อิสระ (Custom Wallpaper Support)
- สามารถเลือกรูปภาพจากเครื่อง (Gallery / Internal Storage / Downloads) มาตั้งเป็นพื้นหลังของแอปได้
- ระบบจะบันทึกรูปไว้ใน Internal Cache ของแอปอย่างปลอดภัย รูปไม่หายแม้รีสตาร์ตเครื่องหรือย้ายตำแหน่งรูปต้นฉบับ
- มีเลเยอร์ Dark Gradient Overlay ปรับแสงเงาอัตโนมัติ เพื่อให้ภาพพื้นหลังสวยงามแต่ตัวหนังสือและปุ่มยังอ่านง่าย ชัดเจน 100%
- มีตัวเลือกกด "🔄 คืนค่าเป็นวอลเปเปอร์เริ่มต้น" เพื่อกลับมาใช้ธีมสี Dark Magenta ดั้งเดิมได้ตลอดเวลา

---

### 5. 📝 Real-Time Terminal Log & บันทึกลงไฟล์
- แสดงขั้นตอนการทำงานแบบ Real-Time บรรทัดต่อบรรทัดในกล่อง Terminal สีดำบนหน้าจอ
- บันทึกไฟล์ Log ลงใน `Movies/TYLOG/` ทุกครั้ง เช่น `ty_download_20261103_164800_xxx.log`
- ระบบ Redact URL ซ่อน Token และรหัสผ่านเพื่อความปลอดภัยของข้อมูล
- ทำงานด้วย Thread-Safe StateFlow Update ป้องกันปัญหา Race Condition

---

### 6. 🔍 Media Scanner & termux-media-scan
- เมื่อดาวน์โหลดหรือแปลงไฟล์เสร็จสิ้น แอปจะเรียก:
  1. `MediaScannerConnection` ของ Android Native
  2. Broadcast `ACTION_MEDIA_SCANNER_SCAN_FILE`
  3. `termux-media-scan` (หากอยู่ในสภาพแวดล้อม Termux / มี Termux:API)
- ไฟล์จะโผล่ในแกลเลอรีรูปภาพและเครื่องเล่นเพลงของมือถือทันทีโดยไม่ต้องรีสตาร์ตเครื่อง!

---

## 🛠 โครงสร้างโปรเจกต์ (Project Structure)

```
TYDownloaderApp/
├── .github/workflows/
│   └── build.yml               # CI/CD ออโต้คอมไพล์ & Unit Test บน GitHub
├── app/
│   ├── build.gradle.kts        # ตั้งค่า dependencies (Compose, youtubedl-android, Coil)
│   ├── proguard-rules.pro      # กฎ Obfuscation ป้องกัน binary หลุด
│   └── src/main/
│       ├── AndroidManifest.xml # กำหนด Permissions, Intents, Foreground Service
│       ├── assets/             # การจัดการ Native Binary Assets
│       ├── java/com/ty/downloader/
│       │   ├── TYApplication.kt          # จุดเริ่มต้นแอป + Background Binary Setup
│       │   ├── data/
│       │   │   ├── MediaModels.kt        # Data classes, Qualities, Enums
│       │   │   ├── LogManager.kt         # ระบบเขียน Log Thread-safe + Redact URL
│       │   │   └── WallpaperManager.kt   # จัดการรูปพื้นหลังผ่าน Dispatchers.IO
│       │   ├── engine/
│       │   │   ├── DownloaderEngine.kt   # คำสั่ง yt-dlp, พรีวิว, ดาวน์โหลด, คลีนไฟล์ค้าง
│       │   │   ├── ConverterEngine.kt    # คำสั่ง FFmpeg แปลงภาพ/เสียง/วิดีโอ/GIF
│       │   │   ├── UpdateManager.kt      # อัปเดต yt-dlp + ตรวจสอบ SHA256 Checksum APK
│       │   │   └── MediaScanner.kt       # สแกนไฟล์เข้า Gallery + termux-media-scan
│       │   ├── service/
│       │   │   └── DownloadForegroundService.kt # Android 14 Foreground Service
│       │   └── ui/
│       │       ├── MainActivity.kt       # Activity หลัก + Intent Receiver
│       │       ├── theme/                # ธีมสี Dark Magenta (#4D0030) + นีออน
│       │       ├── components/           # Header, Tabs, Preview, Log Card
│       │       ├── screens/              # หน้า Downloader & หน้า Converter
│       │       └── viewmodel/
│       │           ├── MainViewModel.kt        # Coordinator ViewModel
│       │           ├── DownloaderViewModel.kt  # จัดการ State การดาวน์โหลด
│       │           ├── ConverterViewModel.kt   # จัดการ State การแปลงไฟล์
│       │           └── UpdateViewModel.kt      # จัดการ State อัปเดตและ Wallpaper
│       └── res/
│           ├── xml/
│           │   ├── network_security_config.xml # ความปลอดภัยของเครือข่าย
│           │   └── file_paths.xml              # FileProvider สำหรับแชร์และติดตั้ง APK
│           └── values/                         # Icon, Colors, Strings, Styles
├── compile_apk.sh              # สคริปต์คอมไพล์ APK ได้ในคำสั่งเดียว
├── gradlew & gradlew.bat       # Gradle Wrapper มาตรฐาน (v8.7)
├── LICENSE                     # MIT License
├── .gitignore                  # กฎกรองไฟล์ build artifacts และ credentials
└── build.gradle.kts            # Root Build Script
```

---

## 🚀 คู่มือการติดตั้งและคอมไพล์ (Installation & Build Guide)

### 📥 1. ติดตั้งแอปพลิเคชันจาก Pre-built APK (เร็วที่สุด)
1. ไปที่แท็บ **Releases** หรือ **Actions** ใน GitHub Repository
2. ดาวน์โหลดไฟล์ APK รุ่นล่าสุด (`TY-Downloader-v3.1-Release.apk`)
3. เปิดไฟล์บนมือถือ Android และกด **"ติดตั้ง (Install)"**
4. เปิดแอป และอนุญาตสิทธิ์การเข้าถึงไฟล์ (All Files Access) ในครั้งแรกเพื่อให้แอปสามารถบันทึกไฟล์วิดีโอ/เสียงลงเครื่องได้

---

### 💻 2. คอมไพล์ด้วยตัวเองผ่าน Terminal / Shell
เปิด Terminal ในโฟลเดอร์โปรเจกต์:
```bash
# ให้สิทธิ์รันสคริปต์
chmod +x gradlew compile_apk.sh

# สั่งคอมไพล์ APK อัตโนมัติ
./compile_apk.sh
```
หรือรันตรงผ่าน Gradle:
```bash
./gradlew assembleRelease
```
ไฟล์ APK พร้อมใช้งานจะอยู่ที่:
`app/build/outputs/apk/release/app-release.apk`

---

### 🌐 3. คอมไพล์ผ่าน GitHub Actions (ฟรี ไม่มีคอมพิวเตอร์ก็ทำได้)
1. Push โปรเจกต์ขึ้น GitHub
2. ไปที่แท็บ **Actions** > เลือก **Build & Test TY Downloader & Converter APK**
3. กด **Run workflow**
4. เมื่อทำงานเสร็จสิ้น ดาวน์โหลด APK ได้ทันทีจากส่วน **Artifacts** ด้านล่างของหน้ารัน

---

## 📄 ใบอนุญาต (License)

โปรเจกต์นี้เผยแพร่ภายใต้ใบอนุญาต **[MIT License](LICENSE)** โดย © 2026 tenal970
สามารถนำไปศึกษา ใช้งาน และพัฒนาต่อยอดได้อย่างอิสระ
