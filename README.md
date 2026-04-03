# 🍌 BananaKnowledge97: Native Android Wrapper
![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)

A high-performance, Kotlin-based Android application that transforms the [BananaKnowledge97.in](https://bananaknowledge97.in) web experience into a native mobile environment. Built with security, speed, and modern Android APIs in mind.

---

## 🚀 Key Features

| Feature | Description |
| :--- | :--- |
| **Hardware Acceleration** | GPU-optimized rendering for smooth scrolling of Website. |
| **Smart Navigation** | Internal links stay in-app; external links use **Chrome Custom Tabs**. |
| **Deep-Linking** | Instant redirection for YouTube, WhatsApp, Telegram, and X (Twitter). |
| **Native Context Menu** | Long-press to **Download Images**, **Copy Links**, or **Share** content. |
| **Lifecycle Aware** | Advanced `OnBackPressedDispatcher` logic prevents accidental app exits. |
| **Offline Ready** | Custom "No Connection" UI with a one-tap retry mechanism. |
| **Modern UI** | Supports Android 12+ Splash Screen API and Material Design 3. |

---

## 🛠️ Configuration & Customization

### 1. Changing the Base URL
To point the app to a different domain or a development server, update the `loadUrl` string in `MainActivity.kt`:

```kotlin
private fun tryLoading() {
    if (isNetworkAvailable()) {
        // Edit this URL to change the app's target website
        webView.loadUrl("[https://bananaknowledge97.in](https://bananaknowledge97.in)") 
    } else {
        showError()
    }
}

```
## 2. Signing Keys & Security (Critical)
This project follows industry-standard security practices by keeping sensitive credentials out of the version history.
 * Create a local.properties file in the project root.
 * Add your private keystore details (this file is ignored by .gitignore):
  ```
RELEASE_STORE_PASSWORD=your_secure_password
RELEASE_KEY_ALIAS=banana_alias
RELEASE_KEY_PASSWORD=your_secure_password
```

 * Note: Never push your .jks file or local.properties to public repositories.
   
## 📦 Technical Specifications
 * Language: Kotlin 1.9+
 * Build System: Gradle (Kotlin DSL / Groovy)
 * Min SDK: 26 (Android 8.0)
 * Target SDK: 36 (Android 16)
 * Architecture: ViewBinding & Material 3
## 🏗️ How to Build
 * Clone:
   ```
    git clone https://github.com/BananaKnowledge97/BananaKnowledge97.git
   ```
 * Configure: Set up your local.properties as described above.
 * Compile: * AndroidIDE: Open the project and click the Build icon.
   * CLI: Run ./gradlew assembleRelease to generate a signed APK.
     
## 📄 Maintenance
Maintained by the owner of BananaKnowledge97.in. Developed using AndroidIDE and Termux.
