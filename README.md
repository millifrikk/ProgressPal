# ProgressPal

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="ProgressPal Logo" width="120"/>
  
  **A comprehensive Android fitness tracking app for weight management and progress analytics**
  
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
  [![MVP Architecture](https://img.shields.io/badge/Architecture-MVP-orange.svg)](https://developer.android.com/topic/architecture)
  [![Material3](https://img.shields.io/badge/UI-Material%203-purple.svg)](https://m3.material.io/)
</div>

## 📱 Overview

ProgressPal is a feature-rich Android application designed to help users track their fitness journey through comprehensive weight management, BMI calculations, and detailed progress analytics. Built with modern Android development practices using Kotlin and MVP architecture.

## ✨ Features

### Core Functionality
- **Weight Tracking**: Log daily weight entries with timestamps and optional notes
- **BMI Analysis**: Automatic BMI calculations with health category classifications
- **Progress Charts**: Interactive weight and BMI trend visualizations using MPAndroidChart
- **Statistics Dashboard**: Comprehensive analytics with trend analysis and goal tracking

### Advanced Features
- **Photo Progress Tracking**: Before/after photo comparisons with gallery management
- **Smart Insights**: AI-powered plateau detection and progress pattern analysis
- **Goal Management**: Set and track weight loss/gain goals with estimated completion dates
- **Premium Features**: Advanced analytics, unlimited photo storage, and personalized insights
- **Data Export**: Export tracking data for external analysis

### User Experience
- **Material Design 3**: Modern, accessible UI following Google's latest design guidelines
- **Onboarding Flow**: Guided setup for new users with profile customization
- **Dark Mode Support**: Automatic theme switching based on system preferences
- **Offline-First**: Local data storage with Room database

## 🏗️ Technical Architecture

### Architecture Pattern
- **MVP (Model-View-Presenter)**: Clean separation of concerns with testable business logic
- **Repository Pattern**: Abstracted data layer with Room database integration
- **Dependency Injection**: Manual DI for lightweight, maintainable code structure

### Tech Stack
- **Language**: Kotlin
- **Database**: Room SQLite with coroutines support
- **UI Framework**: Android Views with ViewBinding
- **Charts**: MPAndroidChart for interactive data visualizations
- **Image Handling**: Glide for efficient photo loading and caching
- **Background Tasks**: WorkManager for notifications and data sync
- **Permissions**: PermissionX for runtime permission handling

### Project Structure
```
app/src/main/java/com/progresspal/app/
├── data/                    # Data layer (Room, repositories)
├── domain/                  # Business logic and models
│   ├── contracts/          # MVP contracts
│   ├── models/             # Domain models
│   ├── insights/           # Analytics engine
│   └── premium/            # Monetization logic
├── presentation/           # UI layer (Activities, Fragments, Presenters)
└── utils/                  # Utility classes and helpers
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK API 24+ (Android 7.0)

### Building the Project

1. **Clone the repository**
   ```bash
   git clone https://github.com/millifrikk/ProgressPal.git
   cd ProgressPal
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### Development Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Run lint checks
./gradlew lint

# Clean build
./gradlew clean
```

## 📊 Key Components

### Analytics Engine
- **InsightsCalculator**: Linear regression and statistical analysis
- **PlateauIdentifier**: Advanced plateau detection with severity classification
- Mathematical algorithms using variance and moving averages

### Photo Management
- **PhotoCaptureHelper**: Camera/gallery integration with compression
- Secure FileProvider implementation for photo sharing
- Comparison views with before/after visualization

### Premium System
- **PremiumManager**: Feature flags and subscription management
- Behavioral upgrade prompts with personalized value propositions
- Trial management and usage tracking

## 🎯 Roadmap

- [ ] Health app integration (Google Fit, Samsung Health)
- [ ] Social features and community challenges
- [ ] Advanced meal tracking
- [ ] Workout integration
- [ ] Cloud backup and sync
- [ ] Wear OS companion app

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Guidelines
- Follow MVP architecture patterns
- Use Material Design 3 components
- Write unit tests for business logic
- Follow Kotlin coding conventions
- Update documentation for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏆 Acknowledgments

- Material Design 3 for UI components
- MPAndroidChart for data visualization
- Room database for local storage
- All contributors and testers

---

<div align="center">
  Made with ❤️ for fitness enthusiasts
</div>