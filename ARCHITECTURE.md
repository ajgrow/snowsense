# Architecture

>This document is here to assist developers that are interested in familiarizing themselves with, maintaining or further developing the SnowSense-app. it describes the project's architectural choices, packages structure, and which design patterns and principles are followed.


## Table of Contents

1. [Code Structure & Architectural Choices](#1-code-structure--architectural-choices)
2. [Object-Oriented Principles](#2-object-oriented-principles)
3. [Technical Specifications](#3-technical-specifications)
4. [Package Structure](#4-package-structure)

---

## 1. Code Structure & Architectural Choices

### Overall Structure & Patterns

SnowSense is an Android application built using Jetpack Compose. Its high-level architecture consists of:
- DataSources responsible for querying APIs for weather, avalanche risk and location data
- Repositories which map data to domain models
- ViewModels managing UI state and interacting with repositories
- UI components passing user events to ViewModels and observing and displaying UI state

The application's architecture primarily applies these design patterns, among others:
- MVVM (Model-View-ViewModel)
- UDF (Unidirectional Data Flow)
- Repository pattern
- Dependency injection
- State hoisting
- Clean Architecture

### Architecture Choices

We chose to build the application's architecture around the clean architecture pattern. This pattern can typically include the following layers:

1. **Data layer** - Purely fetches data from external sources and returns domain models.
2. **Domain layer** - Business logic which does not have platform-specific dependencies, as well as the definitions of domain models.
3. **Application layer** - Concrete use cases which orchestrate domain logic to fulfill specific user needs.
4. **Presentation layer** - State management of the UI data
5. **UI layer** - UI graphics components, only for displaying observed state from the presentation layer

Due to the relatively small scope of our app, we chose to merge the **application layer** into the **domain layer**, and the **presentation layer** into the **ui** layer. We do not have enough use cases to justify a separate layer for them, and our UI is not complex enough to require layer separation between state and graphics.

Furthermore, we have omitted some standard components which were not required for our scope and would have introduced uneccessary boilerplate:
- Mappers
- Data Stores
- Use Case interfaces (we only use implementations directly)

## 2. Object-Oriented Principles

### Low Coupling

DataSources and Repositories are always exposed as **interfaces**:
- `FrostV0DataSource` --> Implemented by `KtorFrostV0DataSource`
- `ClimateRepository` --> Implemented by `FrostClimateRepository`
- `AvalancheRiskRepository` --> Implemented by `FrostAvalancheRiskRepository`

ViewModels only depend on Repository interfaces, never on Repository implementations directly.

Binding takes place in `AppModule.kt` via **Koin**
```kotlin
    single<FrostV0DataSource> { KtorFrostV0DataSource(get()) }
    single<ClimateRepository> { FrostClimateRepository(get()) }
    viewModel { MapViewModel(get()) }
```
This is important because it makes it trivial to, for example, replace Ktor with another HTTP library, as only the AppModule would need to be updated.

It also makes it easy to test Repositories with one `FakeFrostV0DataSource` without touching production code.

### High Cohesion

When attempting to attain high cohesion, everything within a package should hava a single, well-defined responsibility. SnowSense's architecture aims at this goal as often as possible.

As an example, under the `ui` package, everything is grouped by their UI responsibility:

> The `screen` package *only* contains packages which each serve to display a single screen, and no screens live outside the `screen` package.

Any functionality which needs to be shared or reused across *different* screens, lives in separate packages next to `screen`, named and grouped by their own area of responsibility:

> The `search` package contains the reusable `SearchSection` Composable and the `SearchViewModel`.

The functionality within these packages can change and evolve independently, therefore they should live separately. 

In practice this makes it easier to work on or identify issues with one feature/area of the app, as you would mainly have to look at the files within its corresponding package.

### Separation of concerns

Low coupling and high cohesions are in a sense two sides of the same coin; separation of concerns. By making sure both are followed to the highest reasonable degree, the architecture is kept easily maintainable and testable.

### MVVM & Unidirectional Data Flow (UDF)

The app follows MVVM pattern combined with UDF resulting in the following pattern:

### Screen
- **Observer:** consumes `screenState` via `StateFlow`
- **Sender:** emits events to ViewModel through lambda callbacks

↓ *events (`onMapClick`, `onYearChange`, etc.)*

---

### ViewModel
- Holds `MutableStateFlow<ScreenState>`
- Updates state through `.copy()`
- Calls repository via `suspend` functions

↓ *suspend calls (`getClimateDataForLocation()`, `getNamedLocations()`, etc.)*

---

### Repository
- Aggregates data from datasources
- Returns domain objects

↓ *suspend calls (`getNearestSource()`, `getObservations()`, `fetchNamesResponse()`, etc.)

---

### DataSource
- Communicates with API via a Ktor HttpClient
- Returns DTOs

---

## 3. Technical Specifications

### Technologies

| Technology | Type | Usage | Version |
| ---------- | ---- | ----- | ------- |
| Jetpack Compose | UI Framework | Used to build MVVM-friendly UI components called Composables | BOM 2025.05.00 |
| Mapbox Maps for Android | SDK | Used to integrate a Mapbox Map which supports navigation, interaction, and custom layers. | 11.19.0 |
| MET Frost |  API endpoint    |  Used to fetch nearby weather stations and fetch historical climate data from them. | v0 |
| NVE SnøskredAktsomhet | API endpoint | Used to display and query snow avalanche risk areas calculated from terrain steepness, tree cover, etc. | Unversioned REST service |
| Kartverket Stedsnavn | API endpoint | Used to fetch location search results |  Unversioned REST service |
| Vico | Library | Used to render trend graphs | 1.13.1 |

### API Level

| Configuration | Value | Reason |
| --- | --- | --- |
| `minSdk` | 24 | Approximately 99% of Android users use Android 7 (API 24) or higher. Though minSdk API level 26 was at one point used due to support for `java.time`, it was discovered that simply using desugaring to backport support for `java.time` on API 24 was the more inclusive choice. |
| `targetSdk` | 36 | minimal stable SDK for play Store |
| `compileSdk` | 36 | Compiles with the same SDK version as target to avoid warnings |

## 4. Package Structure
```
│   MainActivity.kt
│   MapboxAccessToken.kt
│   SnowSenseApplication.kt
│
├───data/
│   ├───datasource/
│   │   │   AvalancheRiskDataSource.kt
│   │   │   FrostV0DataSource.kt
│   │   │   KtorAvalancheRiskDataSource.kt
│   │   │   KtorFrostV0DataSource.kt
│   │   │   KtorLocationDataSource.kt
│   │   │   LocationDataSource.kt
│   │   │
│   │   └───dto/
│   │           FrostV0ObservationResponseDto.kt
│   │           FrostV0SourceResponseDto.kt
│   │           GeoJsonResponseDto.kt
│   │           LocationResponseDto.kt
│   │
│   ├───model/
│   │   └───exceptions/
│   │           NoObservationException.kt
│   │           NoStationFoundException.kt
│   │
│   └───repository/
│           FrostClimateRepository.kt
│           FrostReferenceTime.kt
│           FrostWeatherRepository.kt
│           KartverketLocationRepository.kt
│           LocationTypeTranslations.kt
│           NveAvalancheRiskRepository.kt
│           SharedPreferencesSearchRepository.kt
│
├───di/
│       AppModule.kt
│
├───domain
│   │   SlabAvalancheRiskAssessor.kt
│   │
│   ├───model/
│   │   │   ClimateArea.kt
│   │   │   Coordinate.kt
│   │   │   DailyWeatherRecord.kt
│   │   │   LocationClimateData.kt
│   │   │   LocationClimateDataPoint.kt
│   │   │   NamedLocation.kt
│   │   │
│   │   └───enums/
│   │           DangerLevel.kt
│   │           ElementType.kt
│   │           MeasurementUnit.kt
│   │
│   ├───repository/
│   │       AvalancheRiskRepository.kt
│   │       ClimateRepository.kt
│   │       LocationRepository.kt
│   │       SearchRepository.kt
│   │       WeatherRepository.kt
│   │
│   └───usecase
│           GetAvalancheDangerUseCase.kt
│
├───navigation/
│       AppNavigation.kt
│       NavigationActions.kt
│       Screen.kt
│
├───network/
│       FrostClientId.kt
│       KtorClient.kt
│
└───ui/
    ├───approot/
    │       AppRoot.kt
    │
    ├───common/
    │       BottomBar.kt
    │       TopBar.kt
    │
    ├───emergency/
    │       EmergencySection.kt
    │
    ├───graph/
    │       ClimateGraph.kt
    │       DatePicker.kt
    │
    ├───model/
    │       DangerLevelUiModel.kt
    │
    ├───risk/
    │       DangerLevelItems.kt
    │       RiskViewModel.kt
    │
    ├───screen/
    │   ├───home/
    │   │       HomeScreen.kt
    │   │
    │   ├───information/
    │   │       ExternalResources.kt
    │   │       InformationScreen.kt
    │   │       InfoViewModel.kt
    │   │       SafetyAdviceSection.kt
    │   │
    │   ├───map/
    │   │       MapboxView.kt
    │   │       MapOverlayComponents.kt
    │   │       MapScreen.kt
    │   │       MapScreenState.kt
    │   │       MapStateHolder.kt
    │   │       MapViewModel.kt
    │   │       TextInputDialog.kt
    │   │
    │   └───settings/
    │           AppLanguageMode.kt
    │           SettingScreen.kt
    │           SettingsViewModel.kt
    │           ThemeMode.kt
    │
    ├───search/
    │       SearchSection.kt
    │       SearchViewModel.kt
    │
    └───theme/
            Color.kt
            Theme.kt
            Type.kt
            TypographyExtension.kt
```
