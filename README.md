# 📱 TY Downloader & Converter v3.1 (Native Android App)

แอปพลิเคชัน Android Native 100% พัฒนาด้วย **Kotlin + Jetpack Compose (Material 3)** สืบทอดและพัฒนาต่อยอดความสามารถทั้งหมดจาก `ytdl_gui_v3_1.py` ให้ทำงานในรูปแบบแอปมือถือสมบูรณ์แบบ ติดตั้งและเปิดใช้งานได้ทันทีจากหน้าจอโฮม ไม่ต้องเปิด Terminal หรือรันเซิร์ฟเวอร์อีกต่อไป!

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
  - กำหนดโฟลเดอร์เอง (Custom Path เช่น `/storage/emulated/0/...`)
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

### 3. 🔄 อัพเดท Library อัตโนมัติทุกครั้งที่เปิดแอป (Auto-Update Engine)
- ทุกครั้งที่เปิดแอป แอปจะตรวจสอบและอัพเดท **`yt-dlp`** ไปยังเวอร์ชันล่าสุดจาก GitHub ในแบ็กกราวด์อัตโนมัติ
- ป้องกันปัญหาเว็บไซต์เปลี่ยนโครงสร้างแล้วดาวน์โหลดไม่ได้
- มีปุ่ม Refresh ที่มุมขวาบนให้กดเช็คเวอร์ชันหรือสั่ง Force Update ได้ทุกเวลา

---

### 4. 🖼️ เปลี่ยนวอลเปเปอร์ได้อิสระ (Custom Wallpaper Support)
- สามารถเลือกรูปภาพจากเครื่อง (Gallery / Internal Storage / Downloads) มาตั้งเป็นพื้นหลังของแอปได้
- ระบบจะบันทึกรูปไว้ใน Internal Cache ของแอปอย่างปลอดภัย รูปไม่หายแม้รีสตาร์ตเครื่องหรือย้ายตำแหน่งรูปต้นฉบับ
- มีเลเยอร์ Dark Gradient Overlay ปรับแสงเงาอัตโนมัติ เพื่อให้ภาพพื้นหลังสวยงามแต่ตัวหนังสือและปุ่มยังอ่านง่าย ชัดเจน 100%
- มีตัวเลือกกด "🔄 คืนค่าเป็นวอลเปเปอร์เริ่มต้น" เพื่อกลับมาใช้ธีมสี Dark Magenta ดั้งเดิมได้ตลอดเวลา

---

### 4. 📝 Real-Time Terminal Log & บันทึกลงไฟล์
- แสดงขั้นตอนการทำงานแบบ Real-Time บรรทัดต่อบรรทัดในกล่อง Terminal สีดำบนหน้าจอ
- บันทึกไฟล์ Log ลงใน `Movies/TYLOG/` ทุกครั้ง เช่น `ty_download_20261103_164800_xxx.log`
- ระบบ Redact URL ซ่อน Token และรหัสผ่านเพื่อความปลอดภัยของข้อมูล

---

### 5. 🔍 Media Scanner & termux-media-scan
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
│   └── build.yml               # CI/CD ออโต้คอมไพล์เป็น APK บน GitHub
├── app/
│   ├── build.gradle.kts        # ตั้งค่า dependencies (Compose, youtubedl-android, Coil)
│   ├── proguard-rules.pro      # กฎ Obfuscation ป้องกัน binary หลุด
│   └── src/main/
│       ├── AndroidManifest.xml # กำหนด Permissions, Intents, Application
│       ├── java/com/ty/downloader/
│       │   ├── TYApplication.kt          # จุดเริ่มต้นแอป + Auto-update on startup
│       │   ├── data/
│       │   │   ├── MediaModels.kt        # Data classes, Qualities, Enums
│       │   │   └── LogManager.kt         # ระบบเขียน Log ลง Movies/TYLOG
│       │   ├── engine/
│       │   │   ├── DownloaderEngine.kt   # คำสั่ง yt-dlp, พรีวิว, ดาวน์โหลด, คลีนไฟล์ค้าง
│       │   │   ├── ConverterEngine.kt    # คำสั่ง FFmpeg แปลงภาพ/เสียง/วิดีโอ/GIF
│       │   │   ├── UpdateManager.kt      # ระบบอัพเดท yt-dlp & ตรวจ FFmpeg
│       │   │   └── MediaScanner.kt       # สแกนไฟล์เข้า Gallery + termux-media-scan
│       │   └── ui/
│       │       ├── MainActivity.kt       # Activity หลัก + Intent Receiver
│       │       ├── theme/                # ธีมสี Dark Magenta (#4D0030) + นีออน
│       │       ├── components/           # Header, Tabs, Preview, Log Card
│       │       ├── screens/              # หน้า Downloader & หน้า Converter
│       │       └── viewmodel/            # จัดการ State ทั้งหมดด้วย Flow
│       └── res/                          # Icon, Colors, Strings, XML
├── compile_apk.sh              # สคริปต์คอมไพล์ APK ได้ในคำสั่งเดียว
├── gradlew & gradlew.bat       # Gradle Wrapper มาตรฐาน (v8.7)
└── build.gradle.kts            # Root Build Script
```

---
