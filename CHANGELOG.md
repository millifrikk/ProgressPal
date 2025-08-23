# Changelog

All notable changes to ProgressPal will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Gender-Aware Body Measurements Dialog** - Intelligent Navy Method body fat calculations with dynamic UI adaptation (Session: 2025-08-23-ui-enhancement-session)
  - Smart gender selection with Material Design 3 RadioGroup components for accurate Navy Method requirements
  - Dynamic field visibility showing hip measurements for females, neck+waist for males with automatic field clearing
  - Real-time benefits text explaining gender-specific Navy Method requirements ("Requires neck + waist measurements" vs "Requires neck + waist + hip measurements")
  - Integrated gender profile updates allowing users to correct gender settings during measurement entry
  - Enhanced measurement system display with clear metric/imperial unit indication
  - Comprehensive form validation with gender-aware measurement range checking
- **Comprehensive Body Measurements System** - Revolutionary upgrade from simple waist-only to full Navy Method body composition analysis (Session: 2025-08-23-Current)
  - Complete BodyMeasurementsDialog supporting neck, waist, and hip circumferences with gender-specific measurement fields
  - Navy Method body fat calculation integration providing accurate body composition analysis for athletic builds
  - Intelligent measurement system detection with automatic metric/imperial conversion and validation
  - Enhanced dashboard body composition card with comprehensive multi-measurement support
  - Database schema evolution v5→v6 adding neck circumference field with seamless migration
- **Interactive Settings Health Configuration** - Transformed placeholder settings into fully functional health preference controls (Session: 2025-08-23-Current)  
  - Material Design 3 RadioGroups for measurement system selection (Metric vs Imperial) with instant preference updates
  - Medical guidelines selection system (US AHA vs EU ESC) for region-appropriate health assessments with persistent storage
  - Real-time settings synchronization across dashboard and body composition analysis systems
  - Complete settings UI overhaul replacing non-functional placeholders with working preference controls
- **Enhanced Session Management System** - Professional development workflow with comprehensive tracking (Session: 2025-08-22-1605)
  - Structured session templates with metadata tracking and git integration
  - Automatic session metrics collection and progress monitoring
  - Session archiving workflow with comprehensive analytics
  - Current session tracking system for development continuity
- **Comprehensive Body Composition Assessment** - Replaces oversimplified BMI with advanced health metrics (Session: 2025-08-21-1647)
  - Waist-to-Height Ratio (WHtR) as primary health metric for accurate risk assessment
  - Body Roundness Index (BRI) calculations optimized for athletic builds
  - Activity level adjustments solving "athletic build incorrectly labeled overweight" problem
  - Smart waist measurement prompts for enhanced assessment accuracy
  - Multi-metric health evaluation combining BMI, WHtR, BRI, and ABSI calculations
- **Multi-Standard Blood Pressure Support** - Implements international medical guidelines for global accuracy (Session: 2025-08-21-1647)
  - US (American Heart Association) and EU (European Society of Cardiology) medical standards
  - Age-adjusted thresholds for seniors (65+ and 80+ relaxed target ranges)
  - Personalized health messages based on user activity level and demographics
  - Color-coded risk categories with Material Design 3 visual hierarchy
  - Comprehensive blood pressure tracking with trend analysis capabilities
- **Health Settings Infrastructure** - Complete user health configuration system (Session: 2025-08-21-1647)
  - Imperial/Metric measurement system support with intelligent unit conversion
  - Medical guideline selection (US/EU) for region-appropriate health assessments
  - Activity level profiles (Sedentary to Endurance Athlete) with BMI threshold adjustments
  - User birth date tracking for age-appropriate medical recommendations
  - Database schema evolution v2→v5 with comprehensive health fields
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
- **Critical Body Measurements Access Issue** - Resolved Add button invisibility preventing users from accessing body measurements (Session: 2025-08-23-ui-enhancement-session)
  - Removed problematic BMI range restriction (23-30) that was hiding Add button for many users outside this range
  - Changed Body Composition card button default visibility from 'gone' to 'visible' for universal reliability
  - All users can now access Navy Method body fat analysis regardless of their current BMI or activity level
  - Root cause: Flawed BMIUtils.shouldAddWaistMeasurement() logic created accessibility barrier for athletes and others
  - Impact: Universal access to body measurements vs. previously limited to specific BMI ranges only
- **Critical Kotlin Compilation Errors** - Resolved build-blocking compilation failures in dashboard presenter (Session: 2025-08-22-1605)
  - Fixed missing userId parameter in weight repository method call
  - Corrected Weight object construction using proper suspend function and existing mapper utility
  - Eliminated cascade compilation errors in DashboardPresenter body composition feature
- **Android Runtime Warnings** - Significantly reduced warning noise and improved future compatibility (Session: 2025-08-22-1605)
  - Updated AppCompat library from 1.6.1 to 1.7.0 to eliminate hidden API access warnings
  - Added AppLocalesMetadataHolderService declaration to resolve AppCompatDelegate warnings
  - Downgraded Core KTX from 1.16.0 to 1.13.1 for improved dependency stability
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
- **Dashboard UI Enhanced** - Replaced simple BMI card with comprehensive body composition display (Session: 2025-08-21-1647)
  - Enhanced body composition card showing WHtR as primary metric when available
  - Health risk indicators with color-coded Material Design chips
  - Intelligent prompts for measurement improvements and accuracy enhancement
  - Real-time activity level consideration for personalized health assessments
  - Seamless integration with waist measurement input dialog for better data collection
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