# 🔐 BYOK Vault — Android

**Bring Your Own Key** — Secure API key storage for Android.

Store API keys from AI services (OpenAI, Anthropic, Gemini, etc.) in one protected place with device-level encryption.

---

## ✨ Features

* 🔒 **Secure Storage** — Keys are encrypted using Android Keystore (AES-256-GCM)
* 🏷️ **Platform Organization** — Group keys by services
* 🎨 **Custom Platforms** — Add your own services with custom icons
* 📋 **Quick Paste** — Paste key from clipboard in one tap
* 🔍 **Duplicate Detection** — Protection against accidentally adding identical keys
* 📝 **Notes** — Add descriptions to your keys
* ✅ **API Key Validation** — Verify keys work before saving (Anthropic, OpenAI, Gemini, DeepSeek, Hailuo)
* 🌙 **Dark & Light Theme** — Full support for both themes
* ✨ **Glassmorphism UI** — Modern glass-effect design matching iOS version

## 🎯 Supported Platforms

Built-in icons for popular AI services:

| Platform            | Icon | Validation |
| ------------------- | ---- | ---------- |
| Anthropic           | ✅   | ✅         |
| OpenAI              | ✅   | ✅         |
| Gemini              | ✅   | ✅         |
| DeepSeek            | ✅   | ✅         |
| Hailuo              | ✅   | ✅         |
| Reve AI             | ✅   | —          |
| GitHub              | ✅   | —          |
| Google Image Search | ✅   | —          |

> You can add any platform with a custom icon

---

## 🏗️ Architecture

```
app/
├── data/
│   ├── dao/           # Room DAO for database operations
│   ├── database/      # Room Database configuration
│   ├── keystore/      # KeystoreService — key encryption
│   ├── model/         # Data models (Platform, APIKey)
│   ├── repository/    # Repository pattern
│   └── validation/    # API key validation services
├── ui/
│   ├── components/    # Reusable UI components (GlassCard, PlatformIcon)
│   ├── navigation/    # Jetpack Navigation
│   ├── screens/       # App screens (MVVM)
│   └── theme/         # Material 3 theme
└── utils/             # Utilities (validation, image processing)
```

### Security

The app uses a two-level storage architecture:

1. **Room Database** — Stores only metadata (name, platform, date)
2. **EncryptedSharedPreferences** — Stores actual key values with AES-256-GCM encryption via Android Keystore

```kotlin
// The key itself is never stored in the database
data class APIKey(
    val id: Long,
    val myName: String,           // Key name
    val keystoreId: String,       // UUID for accessing encrypted value
    val platformId: Long,
    val isValid: Boolean,         // Validation status
    // ...
)
```

### API Key Validation

Before saving, keys can be validated against the actual API:

```kotlin
// Validation services for each platform
object KeyValidationService {
    val supportedPlatforms = listOf(
        "Anthropic", "DeepSeek", "Gemini", "OpenAI", "Hailuo"
    )
    
    suspend fun validateKey(platformName: String, apiKey: String): ValidationResult
}
```

---

## 🛠️ Technologies

| Category         | Technology                   |
| ---------------- | ---------------------------- |
| **Language**     | Kotlin                       |
| **UI**           | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Repository            |
| **Database**     | Room + KSP                   |
| **Navigation**   | Navigation Compose           |
| **Security**     | AndroidX Security Crypto     |
| **Images**       | Coil                         |
| **Min SDK**      | 26 (Android 8.0)             |
| **Target SDK**   | 36                           |

---

## 🚀 Getting Started

### Requirements

* Android Studio Ladybug (2024.2.1) or newer
* JDK 17
* Android SDK 36

### Build

```bash
# Clone the repository
git clone https://github.com/malgana/byok-vault-android.git

# Open in Android Studio and sync Gradle
# or build from command line:
./gradlew assembleDebug
```

---

## 📱 Screenshots

| Main Screen | Key Details | Add Key |
|-------------|-------------|---------|
| Glass cards grid | Copy with one tap | Validation flow |

*Screenshots coming soon*

---

## 🆕 What's New in v2.0

- **Glassmorphism Design** — Modern glass-effect UI matching iOS version
- **API Key Validation** — Verify keys before saving
- **Animated Transitions** — Smooth appear animations
- **Improved Theming** — Better dark/light mode support
- **Quick Copy** — Copy keys directly from the list

---

## 📄 License

```
MIT License

Copyright (c) 2025-2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🔗 Related Projects

* [BYOK Vault iOS](https://github.com/malgana/keyvault-ios) — iOS version (SwiftUI + SwiftData)

---

## 👤 Author

**Aleksandr Prostetsov**

* GitHub: [@malgana](https://github.com/malgana)
