# Changelog

All notable changes to ProgressPal will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Complete Dark Mode Support** - Professional dark theme implementation following Material Design 3 guidelines (Session: 2025-08-20-0816)
  - Automatic light/dark mode switching based on system settings using DayNight theme
  - Material3 surface container system with 5-level elevation hierarchy for proper depth perception
  - Comprehensive values-night/ resource qualifiers with optimized dark mode color palette
  - Adaptive color state selectors for seamless theme transitions
  - Complete migration documentation in color-theme-update-and-migration.md
- **GitHub Actions CI/CD Pipeline** - Automated build system for continuous integration (Session: 2025-08-20-0816)
  - Debug and release APK builds on every push to main branch
  - Automated testing and lint checks in build pipeline
  - APK artifacts uploaded for easy download and installation
  - Release workflow for creating GitHub releases with proper APK naming
- **Professional GitHub Repository** - Complete project setup with comprehensive documentation (Session: 2025-08-20-0816)
  - Detailed README with technical architecture, features, and setup instructions
  - Professional badges showing platform, language, and architecture information
  - Public repository for community contributions and project showcasing

### Fixed
- **Critical Dark Mode Visibility Issues** - Resolved invisible UI elements causing user frustration (Session: 2025-08-20-0816)
  - Fixed calendar date picker text invisible in dark mode by migrating to theme attributes
  - Corrected "ProgressPal" app bar title invisible on dark backgrounds using proper contrast colors
  - Eliminated hardcoded color references causing poor visibility across 10+ layout files
  - All UI components now properly visible and accessible in both light and dark modes
- **Critical StackOverflowError in Settings Tab** - Eliminated app crash when opening Settings (Session: 2025-08-20-0816)
  - Fixed recursive getContext() call causing infinite loop in SettingsFragment
  - Proper Fragment context handling restored with super.getContext() implementation
- **Material Design 3 Theme Compatibility** - Resolved crashes in Analysis tab with Material3 components (Session: 2025-08-20-0816)
  - Complete theme migration from Material2 to Material3 framework
  - Added all required Material3 color attributes and text appearances
  - Fixed NullPointerException in Material3 Chip components
- **Onboarding Flow Navigation** - Fixed Getting Started wizard appearing on every app launch (Session: 2025-08-20-0816)
  - Implemented proper user existence check in SplashActivity using Room database
  - Added intelligent navigation flow: existing users → MainActivity, new users → OnboardingActivity
  - Proper coroutine-based database queries with lifecycle management
- **Statistics Tab Infinite Refresh Loop** - Eliminated constant loading state when no user data exists (Session: 2025-08-20-0816)
  - Enhanced StatisticsPresenter to properly handle null user cases
  - Added immediate empty state display for non-registered users
  - Fixed race conditions in LiveData observer setup

### Changed
- **Complete Material3 Design System Migration** - Modernized UI framework for enhanced user experience (Session: 2025-08-20-0816)
  - Migrated from Material2 to Material3 components across all layouts
  - Updated button styles: MaterialComponents → Material3 variants
  - Enhanced text input layouts with Material3 OutlinedBox styling
  - Implemented semantic color roles (?attr/colorOnSurface, ?attr/colorPrimary) for theme consistency
  - Updated custom theme styles to use Material3 theme attributes instead of hardcoded colors
- **Mandatory User Registration** - Enhanced onboarding flow to ensure complete profile setup (Session: 2025-08-20-0816)
  - Removed ability to skip onboarding to prevent incomplete user states
  - Hidden skip button throughout registration process for consistent experience
- **Improved Empty State Messaging** - Enhanced user guidance for Statistics tab (Session: 2025-08-20-0816)
  - Updated empty state from "Complete Registration" to "No Statistics Available"
  - Professional messaging: "Start tracking your weight to unlock detailed analytics, progress charts, and personalized insights"
  - Intelligent UI element visibility based on data availability
- **Progress Photos System** - Complete photo capture and management system for visual progress tracking (Phase 3)
  - Camera integration with compression and FileProvider security
  - Gallery view with grid layout and photo metadata
  - Before/after photo comparison with timeline analysis
  - Photo detail screens with full-screen viewing and actions

- **Advanced Analytics Engine** - Sophisticated pattern detection and insights system (Phase 3)
  - Mathematical analysis using linear regression and statistical methods
  - Plateau identification with severity classification and breakthrough strategies
  - Trend detection, anomaly identification, and predictive modeling
  - Personalized tips and recommendations based on user patterns

- **Insights UI System** - Dynamic card-based interface for displaying analytics (Phase 3)
  - Multiple insight card types (progress summary, plateau analysis, tips, predictions)
  - Flexible RecyclerView adapter with sealed classes for type safety
  - Loading states, error handling, and empty state management
  - Smooth animations and Material Design 3 compliance

- **Premium Features Framework** - Complete freemium monetization system (Phase 3)
  - Feature flag management with usage tracking and limits
  - Intelligent upgrade prompts based on user behavior
  - Trial management and subscription tier system
  - Behavioral psychology integration for optimal conversion timing

- **Premium Upgrade Flow** - Comprehensive subscription purchase experience (Phase 3)
  - Personalized value propositions based on usage patterns
  - Visual pricing tier selection with savings calculations
  - Benefits showcase with animated presentations
  - Trial activation and subscription simulation

### Changed
- Database schema enhanced with PhotoDao advanced query methods for analytics
- Permission system updated with camera and storage permissions with runtime handling
- UI architecture implemented with card-based design system for scalable insights display
- Navigation flow enhanced with premium upgrade integration points throughout the app

### Technical
- Implemented MVP + Repository pattern across all new features
- Added mathematical algorithms for sophisticated data analysis
- Integrated reactive programming with StateFlow for real-time updates
- Enhanced security with FileProvider for photo sharing

## [1.0.0] - Initial Release (Phases 1-2)

### Added
- Basic weight tracking with Room database
- Onboarding flow with user setup
- Dashboard with current weight and BMI display
- History view with weight entries
- Statistics and charts using MPAndroidChart
- Settings and data export functionality
- Notifications and reminders using WorkManager

---

*This changelog tracks the evolution of ProgressPal from a basic weight tracker to a comprehensive fitness analytics platform.*