# Architecture - SnowSense

## Table of Contents

1. [Code Structure & Architectural Choices](#1-code-structure--architectural-choices)
2. [Object-Oriented Principles](#2-object-oriented-principles)
3. [Technical Specifications](#3-technical-specifications)
4. [Package Structure](#4-package-structure)

---

## 1. Code Structure & Architectural Choices

### Overall Structure & Patterns

SnowSense is an Android application built using Jetpack Compose. It is structured in 3 layers (data, domain, UI). It follows the MVVM and UDF patterns, and uses Koin for dependency injection. Ktor is used as a network client.

### Architecture Choices

We chose to use layered architecture as it contributes to separation of concerns, which in practice makes maintainability and testing easier. Due to the relatively small scope of our app, we chose to merge the **application layer** into the **domain layer**, and the **presentation layer** into the **ui** layer. We do not have enough use cases to justify a separate **application** layer, and our UI is not complex enough to require separating state and graphics into separate layers.

This is the structure we ended up with:

1. **Data layer** - Repository implementations return domain models, which are mapped from DTOs returned by DataSources. 
2. **Domain layer** - Business logic which does not have platform-specific dependencies, Repository interfaces, as well as domain models.
3. **UI layer** - ViewModels update UI state based on UI events and domain models returned by repositories. Composables invoke UI event, and observe and display UI state.

Furthermore, we have omitted some standard components which were not required for our scope and would have introduced unneccessary boilerplate:
- Dedicated Mapper classes (Repositories handle mapping DTOs to domain models instead)
- Use Case interfaces (we only use Use Case implementations directly, partly due to time constraints)


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

When attempting to attain high cohesion, everything within a package should have a single, well-defined responsibility. SnowSense's architecture aims at this goal as often as possible.

As an example, under the `ui` package, everything is grouped by their UI responsibility:

> The `screen` package *only* contains packages which each serve to display a single screen, and no screens live outside the `screen` package.

Any functionality which needs to be shared or reused across *different* screens, lives in separate packages next to `screen`, named and grouped by their own area of responsibility:

> The `search` package contains the reusable `SearchSection` Composable and the `SearchViewModel`.

The functionality within these packages can change and evolve independently, therefore they should live separately. 

In practice this makes it easier to work on or identify issues with one feature/area of the app, as you would mainly have to look at the files within its corresponding package.

### MVVM & Unidirectional Data Flow (UDF)

The app follows MVVM pattern combined with UDF resulting in the following pattern:

#### Screen
- **Observer:** consumes `screenState` via `StateFlow`
- **Sender:** emits events to ViewModel through lambda callbacks

↓ *events (`onMapClick`, `onYearChange`, etc.)*

---

#### ViewModel
- Holds `MutableStateFlow<ScreenState>`
- Updates state through `.copy()`
- Calls repository via `suspend` functions

↓ *suspend calls (`getClimateDataForLocation()`, `getNamedLocations()`, etc.)*

---

#### Repository
- Aggregates data from datasources
- Returns domain objects

↓ *suspend calls* (`getNearestSource()`, `getObservations()`, `fetchNamesResponse()`, etc.)

---

#### DataSource
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
| `minSdk` | API 25 | Minimum SDK which works with MET Frost v0 API* |
| `targetSdk` | API 36 | Android recommends targeting the newest API level to ensure users get the latest security and performance updates. |
| `compileSdk` | API 36 | Compiles with the same SDK version as target to avoid warnings. |

\*Initially we set the minSdk to API 24, but quickly realized we wanted to use `java.time` which was only natively supported from API 26 and onward. We could either upgrade to 26 (which we did for a while, though it could exclude a small portion of users), or use desugaring to backport `java.time` to API 24. 

However, when we tried to test the project with API 24, we ran into issues fetching data from FROST due to certificate problems on older Android versions. We discovered that API 24 was not compatible with the Frost API (at least not without major workarounds). This was most likely due to a TLS/SSL issue, and we did not see it as worthwhile to troubleshoot this further. Instead we landed on API 25, where this issue was not present.

The number of users on API 25 or above is about 98.7%, and we deemed this to be acceptable.

## 4. Package Structure
```
│   MainActivity.kt
│   MapboxAccessToken.kt
│   SnowSenseApplication.kt
│
├───data                                                 # Data layer
│   ├───datasource                                       # DataSource interfaces and implementations
│   │   │   AvalancheRiskDataSource.kt
│   │   │   FrostV0DataSource.kt
│   │   │   KtorAvalancheRiskDataSource.kt
│   │   │   KtorFrostV0DataSource.kt
│   │   │   KtorLocationDataSource.kt
│   │   │   LocationDataSource.kt
│   │   │
│   │   └───dto        
│   │           FrostV0ObservationResponseDto.kt
│   │           FrostV0SourceResponseDto.kt
│   │           GeoJsonResponseDto.kt
│   │           LocationResponseDto.kt
│   │
│   └───repository                                       # Repository implementations
│           FrostClimateRepository.kt
│           FrostReferenceTime.kt
│           FrostWeatherRepository.kt
│           KartverketLocationRepository.kt
│           LocationTypeTranslations.kt
│           NveAvalancheRiskRepository.kt
│           SharedPreferencesSearchRepository.kt
│
├───di                                                   # Dependency injection
│       AppModule.kt
│
├───domain                                               # Domain layer
│   │   SlabAvalancheRiskAssessor.kt
│   │
│   ├───model                                            # Domain models
│   │   │   ClimateArea.kt
│   │   │   Coordinate.kt
│   │   │   DailyWeatherRecord.kt
│   │   │   LocationClimateData.kt
│   │   │   LocationClimateDataPoint.kt
│   │   │   NamedLocation.kt
│   │   │
│   │   ├───enums
│   │   │       DangerLevel.kt
│   │   │       ElementType.kt
│   │   │       MeasurementUnit.kt
│   │   │
│   │   └───exceptions
│   │           NoObservationException.kt
│   │           NoStationFoundException.kt
│   │
│   ├───repository                                      # Repository interfaces
│   │       AvalancheRiskRepository.kt
│   │       ClimateRepository.kt
│   │       LocationRepository.kt
│   │       SearchRepository.kt
│   │       WeatherRepository.kt
│   │
│   └───usecase
│           GetAvalancheDangerUseCase.kt
│
├───navigation
│       AppNavigation.kt
│       NavigationActions.kt
│       Screen.kt
│
├───network
│       FrostClientId.kt
│       KtorClient.kt
│
└───ui                                                 # UI layer
    ├───approot
    │       AppRoot.kt
    │
    ├───common
    │       BottomBar.kt
    │       TopBar.kt
    │
    ├───emergency
    │       EmergencySection.kt
    │
    ├───graph
    │       ClimateGraph.kt
    │       DatePicker.kt
    │
    ├───model
    │       DangerLevelUiModel.kt
    │
    ├───risk
    │       DangerLevelItems.kt
    │       RiskViewModel.kt
    │
    ├───screen
    │   ├───home
    │   │       HomeScreen.kt
    │   │
    │   ├───information
    │   │       ExternalResources.kt
    │   │       InformationScreen.kt
    │   │       InfoViewModel.kt
    │   │       SafetyAdviceSection.kt
    │   │
    │   ├───map
    │   │       MapboxView.kt
    │   │       MapOverlayComponents.kt
    │   │       MapScreen.kt
    │   │       MapScreenState.kt
    │   │       MapStateHolder.kt
    │   │       MapViewModel.kt
    │   │       TextInputDialog.kt
    │   │
    │   └───settings
    │           AppLanguageMode.kt
    │           SettingScreen.kt
    │           SettingsViewModel.kt
    │           ThemeMode.kt
    │
    ├───search
    │       SearchSection.kt
    │       SearchViewModel.kt
    |       SearchDismissScrim.kt
    │
    └───theme
            Color.kt
            Theme.kt
            Type.kt
            TypographyExtension.kt
```
