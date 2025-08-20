# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building & Running
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Install debug build on connected device
./gradlew installDebug

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "*BMICalculatorTest*"

# Run Android instrumentation tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

### Code Quality & Analysis
```bash
# Generate lint report
./gradlew lint

# Check Kotlin code style (if detekt is added)
./gradlew detekt

# Run all checks before committing
./gradlew check
```

## Architecture Overview

### High-Level Architecture
ProgressPal follows **MVP (Model-View-Presenter)** architecture with **Repository pattern** for data management:

```
Presentation Layer (MVP)
├── Activities/Fragments (Views)
├── Presenters (Business Logic)
└── Contracts (Interface definitions)

Domain Layer
├── Models (Business entities)
├── Use Cases (Business logic)
├── Insights (Analytics & calculations)
└── Premium (Monetization logic)

Data Layer
├── Database (Room SQLite)
├── Repositories (Data abstraction)
└── Entities (Database models)
```

### Key Architectural Patterns

**MVP Pattern**: Each screen has a Contract interface defining View and Presenter responsibilities. Views handle UI, Presenters contain business logic, Models represent data.

**Repository Pattern**: Repositories abstract data sources (Room database) and provide clean APIs to presenters. All database operations go through repositories.

**Dependency Flow**: Views → Presenters → Repositories → DAOs → Database

### Core Systems

**Analytics Engine**: `domain/insights/` contains sophisticated mathematical analysis:
- `InsightsCalculator`: Linear regression, pattern detection, trend analysis
- `PlateauIdentifier`: Advanced plateau detection with severity classification
- Mathematical algorithms using statistical variance and moving averages

**Premium System**: `domain/premium/` implements freemium monetization:
- `PremiumManager`: Feature flags, usage tracking, subscription management
- `PremiumHelper`: Simplified API for access checks and upgrade flows
- Behavioral-based upgrade prompts with personalized value propositions

**Photo System**: Complete photo management for progress tracking:
- `PhotoCaptureHelper`: Camera/gallery integration with compression
- Advanced comparison views and gallery management
- FileProvider integration for secure photo sharing

## Database Architecture

### Core Entities
```kotlin
// Primary data models
UserEntity      // User profile and settings
WeightEntity    // Weight entries with timestamps
MeasurementEntity // Body measurements (waist, chest, etc.)
GoalEntity      // User goals and targets  
PhotoEntity     // Progress photos linked to entries
```

### Repository Layer
Each entity has a corresponding repository that:
- Provides async/sync data access methods
- Handles data transformations between entities and domain models
- Abstracts Room database operations
- Uses Kotlin coroutines for background operations

### Database Migrations
When modifying entities:
1. Update the entity class
2. Increment `DATABASE_VERSION` in `Constants.kt`
3. Create migration strategy in `ProgressPalDatabase.kt`
4. Test migration with existing data

## Key Components

### MVP Contracts
All screens use contract interfaces in `domain/contracts/`:
```kotlin
interface [Feature]Contract {
    interface View : BaseContract.View {
        // UI methods
    }
    interface Presenter : BaseContract.Presenter<View> {
        // Business logic methods
    }
}
```

### Photo Capture System
`PhotoCaptureHelper` handles:
- Runtime permission requests (PermissionX library)
- Camera capture with FileProvider security
- Gallery selection and image compression
- Automatic image optimization for mobile storage

### Insights & Analytics
Mathematical analysis system:
- Linear regression for trend detection
- Statistical variance for plateau identification  
- Pattern recognition for user behavior analysis
- Predictive modeling for goal achievement estimates

### Premium Features
Freemium model with intelligent upgrade timing:
- Feature flags with usage limits
- Behavioral analysis for optimal upgrade prompts
- Trial management and subscription tiers
- Personalized value propositions based on usage

## Development Guidelines

### Adding New Features
1. **Create Contract**: Define View and Presenter interfaces in `domain/contracts/`
2. **Implement Presenter**: Business logic in `presentation/[feature]/`
3. **Create Repository**: Data access in `data/repository/` if needed
4. **Update Database**: Add entities/DAOs if data storage required
5. **Build UI**: Activities/Fragments with ViewBinding
6. **Add Navigation**: Update bottom navigation or intent flows

### Working with Charts
Uses MPAndroidChart library:
- `WeightChartView`: Line chart for weight trends
- `WeightMarkerView`: Custom markers for data points
- Chart data is prepared in presenters and passed to views
- Configuration constants in `Constants.kt`

### Photo Feature Integration
When adding photo features:
- Use `PhotoCaptureHelper` for camera/gallery access
- Store photos in app-specific external storage
- Link photos to entries via `PhotoEntity.weightId`
- Use Glide for image loading and display
- Implement proper cleanup for temporary files

### Premium Feature Development
When adding premium features:
1. Define feature in `PremiumFeature` enum
2. Add access check in `PremiumManager.hasFeatureAccess()`
3. Track usage with `PremiumManager.trackFeatureUsage()`
4. Add upgrade prompts using `PremiumHelper.checkFeatureAccess()`
5. Update `PremiumBenefit` list for upgrade screens

### Testing Strategy
- **Unit Tests**: Calculator utilities, business logic, data transformations
- **Integration Tests**: Repository operations, database migrations
- **UI Tests**: Critical user flows (onboarding, add entry, view history)

### Code Style
- Use ViewBinding for all layouts
- Implement proper lifecycle handling in presenters
- Use sealed classes for insight cards and result types
- Follow Material Design 3 patterns
- Use Kotlin coroutines for async operations
- Store all user-facing text in `strings.xml`

## Common Development Tasks

### Adding New Measurement Type
1. Add to `Constants.MEASUREMENT_TYPES`
2. Update UI in measurement fragments
3. Update database schema if needed
4. Add calculation logic if required

### Adding New Insight Type
1. Create new `InsightCard` sealed class variant
2. Add corresponding ViewHolder in `viewholders/`
3. Update `InsightCardsAdapter` to handle new type
4. Create layout file for the new card
5. Add generation logic in `InsightsCalculator`

### Implementing New Premium Feature
1. Add feature to `PremiumFeature` enum
2. Implement access logic in `PremiumManager`
3. Add usage tracking and limits
4. Create upgrade flow integration points
5. Update benefits list and upgrade screens

## Key Dependencies

- **Room**: Local database with coroutines support
- **MPAndroidChart**: Interactive charts and graphs  
- **Glide**: Image loading and caching for photos
- **PermissionX**: Runtime permission handling
- **MaterialDateTimePicker**: Date/time selection dialogs
- **WorkManager**: Background reminders and notifications

## Project Status

**Current Phase**: Phase 3 Complete (Enhanced Features)
- ✅ MVP Foundation (Basic tracking)
- ✅ Core Features (Charts, statistics, notifications)  
- ✅ Enhanced Features (Photos, insights, premium system)

**Next Phase**: Advanced features (goal setting, social features, health integration)

The codebase is production-ready with comprehensive error handling, Material Design UI, and scalable architecture supporting future feature development.