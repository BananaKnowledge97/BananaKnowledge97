# 🍌 BananaKnowledge97 Android Wrapper

![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)

A lightweight, high-performance native Android wrapper for **BananaKnowledge97.in**. This app provides a seamless mobile experience by rendering the web platform within a native container.

---

## ✨ Key Features

- **⚡ Instant Loading:** Optimized WebView cache for a snappier feel.
- **📱 Responsive Layout:** Forced mobile-view optimization.
- **🌐 Progress Tracking:** Built-in loading bar so users never guess the status.

---

## 🛠️ Installation & Setup

### Prerequisites
* Android Studio Ladybug or newer
* JDK 17
* Android SDK (API 21+)

### Getting Started
1. **Clone the project:**
   ```bash
   git clone [https://github.com/yourusername/bananaknowledge-android.git](https://github.com/yourusername/bananaknowledge-android.git)

 * Open in Android Studio:
   Select "Open an Existing Project" and point to the directory.
 * Customize URL:
   Open Strings.xml and update the base URL:
   <string name="website_url">[https://bananaknowledge97.in](https://bananaknowledge97.in)</string>

⚙️ Configuration
| File | Purpose |
|---|---|
| AndroidManifest.xml | Internet permissions & App Icon |
| Colors.xml | Adjust Primary/Secondary brand colors |
| MainActivity.kt | WebView logic & Back-button handling |
🚀 Deployment
To generate a signed APK for distribution:
 * Go to Build > Generate Signed Bundle / APK.
 * Follow the wizard to create a Keystore.
 * Locate your file in app/release/app-release.apk.
🤝 Contributing
Contributions make the open-source community an amazing place to learn and create.
