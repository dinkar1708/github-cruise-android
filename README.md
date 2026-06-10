# GitHub Cruise Android App

A modern Android application built with Jetpack Compose that allows users to search GitHub users, view profiles, and explore repositories.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Material Design 3](https://img.shields.io/badge/Design-Material%203-purple.svg)](https://m3.material.io/)

## Screenshots & Demo

<img width="260" height="600" alt="Screenshot_1781061222" src="https://github.com/user-attachments/assets/714cbf84-e99e-430a-81e1-e098183d6ba4" />

<img width="260" height="600" alt="Screenshot_1781061219" src="https://github.com/user-attachments/assets/0f4e67af-f866-410c-ad8e-ea4315900a91" />
<img width="260" height="600" alt="Screenshot_1781061214" src="https://github.com/user-attachments/assets/59bbeb7b-ce38-4b84-82fc-1a39f3978338" />
<img width="260" height="600" alt="Screenshot_1781061210" src="https://github.com/user-attachments/assets/dd747c73-1f24-4ca9-b8f2-eeabbf96ef34" />

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Documentation](#documentation)
- [Testing](#testing)
- [Build Variants](#build-variants)
- [FAQ](#faq)
- [Roadmap](#roadmap)
- [Contributing](#contributing)

---

## Features

### Core Functionality
- Search GitHub users with real-time results and pagination
- View detailed user profiles (followers, following, bio)
- Browse user repositories with filtering options
- View repository details in integrated WebView

### User Experience
- Dark and Light theme support (automatic switching)
- English and Japanese language support
- Responsive design for all screen sizes
- Screen rotation with state preservation
- Smooth performance with pagination and image caching

### Design & Accessibility
- Material Design 3 implementation
- WCAG AA compliant colors and accessibility features
- Clean animations and transitions

See complete features in [FEATURES.md](documentation/FEATURES.md)

---

## Tech Stack

### Core Technologies
- **Language:** Kotlin 100%
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt (Dagger)
- **Async:** Kotlin Coroutines + Flow

### Libraries
- **Networking:** Retrofit, OkHttp, Moshi
- **Image Loading:** Coil
- **Navigation:** Navigation Compose
- **Logging:** Timber
- **Testing:** JUnit 4, MockK, Coroutines Test

### Requirements
- **Minimum SDK:** 21 (Android 5.0 Lollipop) - Supports 99.6% of devices
- **Target SDK:** 34 (Android 14)
- **JDK:** Java 17
- **Build Tool:** Gradle 8.0+

See full architecture in [ARCHITECTURE.md](documentation/ARCHITECTURE.md)

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK (API 21+)
- Emulator or physical device

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/dinkar1708/GithubCruise.git
   cd GithubCruise
   ```

2. **Open in Android Studio**
   - File → Open → Select project folder
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the app**
   - Select `debug` build variant
   - Click Run (▶️) or use `Shift + F10`
   - Choose emulator or connected device

### Quick Run Commands

```bash
# Run debug build
./gradlew installDebug

# Run tests
./gradlew test

# Run lint checks
./gradlew lint

# Build release APK
./gradlew assembleRelease
```

---

## Project Structure

```
GithubCruiseAndroid/
├── app/src/main/java/com/jetpack/compose/github/github/cruise/
│   ├── di/                    # Dependency Injection (Hilt modules)
│   ├── domain/                # Business logic layer
│   │   ├── model/            # Domain models
│   │   └── usecase/          # Use cases
│   ├── network/              # Network layer (Retrofit, API)
│   ├── repository/           # Data repositories
│   └── ui/                   # Presentation layer
│       ├── features/         # Feature screens
│       │   ├── splash/
│       │   ├── users/
│       │   ├── userrepository/
│       │   └── repodetails/
│       ├── shared/           # Reusable UI components
│       └── theme/            # Material Design 3 theme & tokens
├── documentation/             # Project documentation
│   ├── FEATURES.md           # Detailed features documentation
│   ├── ARCHITECTURE.md       # Technical architecture guide
│   ├── DESIGN_SYSTEM.md      # Material Design 3 design system
│   └── API.md                # GitHub API integration details
└── README.md                 # This file
```

See detailed architecture in [ARCHITECTURE.md](documentation/ARCHITECTURE.md)

---

## Documentation

- [FEATURES.md](documentation/FEATURES.md) - Complete feature documentation
- [ARCHITECTURE.md](documentation/ARCHITECTURE.md) - Architecture patterns and data flow
- [API.md](documentation/API.md) - GitHub API endpoints
- [DESIGN_SYSTEM.md](documentation/DESIGN_SYSTEM.md) - Material Design 3 tokens

---

## Testing

### Run Tests

**From Android Studio:**
- Right-click on test package → Run Tests
- View test results in the test runner window

**From Command Line:**
```bash
# Run all unit tests
./gradlew test

# Run debug unit tests
./gradlew testDebugUnitTest

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

### Test Structure
- ViewModel tests
- Repository tests
- Use case tests
- Data transformation tests

### Testing Tools
- **JUnit 4** - Test framework
- **MockK** - Mocking library for Kotlin
- **Coroutines Test** - Testing async code

---

## Build Variants

### Debug Build
```
App Name: DebugGithubCruise
Package: com.jetpack.compose.github.github.cruise.debug
API URL: https://api.github.com
Features: Logging enabled, can install alongside release
```

### Release Build
```
App Name: GithubCruise
Package: com.jetpack.compose.github.github.cruise
API URL: https://release.api.github.com
Features: Optimized, ProGuard ready
```

### Running Both Variants
You can install both debug and release builds on the same device simultaneously for testing.

<img width="500" src="https://github.com/dinkar1708/GithubCruise/assets/14831652/b361028e-4aac-4107-8642-238de57bbeff">

---

## FAQ

### General Questions

**Q: What is the minimum Android version supported?**
A: Android 5.0 (API 21) - Supporting 99.6% of Android devices.

**Q: Does the app work offline?**
A: Currently, the app requires an internet connection to fetch data from GitHub API. Offline caching is planned for future releases.

**Q: How do I switch between light and dark themes?**
A: The app automatically follows your system theme settings. Change your device theme to switch.

**Q: How do I change the language?**
A: The app automatically detects your device language. Currently supports English and Japanese.

### Developer Questions

**Q: What architecture pattern is used?**
A: MVVM (Model-View-ViewModel) with Clean Architecture principles. See [ARCHITECTURE.md](documentation/ARCHITECTURE.md).

**Q: How is dependency injection handled?**
A: Using Hilt (Dagger) for compile-time dependency injection.

**Q: Can I run debug and release builds together?**
A: Yes! Debug build has a different application ID suffix, allowing both to be installed simultaneously.

**Q: How do I handle GitHub API rate limits?**
A: The free API allows 60 requests/hour. You can add a personal access token to increase the limit to 5000 requests/hour.

**Q: Where are the design tokens defined?**
A: In `ui/theme/` package - Spacing.kt, Color.kt, Typography.kt, etc. See [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md).

### Troubleshooting

**Q: Hilt build errors?**
A: Make sure you have added `@HiltAndroidApp` to Application class and `@AndroidEntryPoint` to activities. Check [FAQ.md](documentation/API.md) for common Hilt issues.

**Q: API rate limit exceeded?**
A: Wait for the limit to reset (60 requests/hour) or implement personal access token authentication.

---

## Roadmap

### Planned Features
- Debounced/throttled search input
- Repository caching with refresh strategy
- Comprehensive UI tests
- Enhanced tablet optimization
- Repository pagination support
- User authentication with GitHub OAuth
- Favorite users/repositories
- Advanced filtering options

### Planned Improvements
- Improve code coverage to 80%
- Add integration tests
- Implement analytics
- Add crash reporting
- Optimize app size

---

## CI/CD

### GitHub Actions
The project uses GitHub Actions for continuous integration.

**Automated Checks:**
- Build verification
- Lint checks
- Unit tests
- Code quality analysis

**Configuration:** `.github/workflows/build.yml`

**PR Details:** [CI/CD Setup PR #12](https://github.com/dinkar1708/GithubCruise/pull/12)

---

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable/function names
- Add comments for complex logic
- Write tests for new features
- Update documentation

---

## Development Tools

### Network Inspection
Use Android Studio's App Inspection tool to monitor network requests:
1. Run the app
2. View → Tool Windows → App Inspection
3. Select Network Inspector
4. View API requests, responses, and timing

<img width="1000" src="https://github.com/dinkar1708/GithubCruise/assets/14831652/4c6516e3-ceeb-4e7f-ac36-b60922e9a17b">

### Jetpack Compose Preview
- Live preview of composables during development
- Multiple device configurations
- Dark/light theme previews
- Instant feedback without running the app

<img width="900" src="https://github.com/dinkar1708/GithubCruise/assets/14831652/ca3d0f4e-119f-4708-ae4c-c2a265a3bb5d">

---

## License

This project is available for educational and portfolio purposes.

---

## Acknowledgments

- **Jetpack Compose** - Google's modern UI toolkit
- **Material Design 3** - Google's design system
- **GitHub API** - For providing the data
- Android developer community

---

## Contact

**Repository:** [github.com/dinkar1708/GithubCruise](https://github.com/dinkar1708/GithubCruise)

**Issues:** [Report a bug or request a feature](https://github.com/dinkar1708/GithubCruise/issues)

---

**Made with ❤️ using Jetpack Compose**
