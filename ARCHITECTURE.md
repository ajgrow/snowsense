# Architecture

>This document is here to assist developers that are interested in familiarizing themselves with,
>maintaining or further developing the SnowSense-app. it describes the project`s parent structure
>and other architectural choices that have been made and which principles and patterns the code follows.


## Table of Contents

1. [Code Structure & Architectural Choices](#1-code-structure--architectural-choices)
2. [Object-Oriented Principles](#2-object-oriented-principles)
3. [Technical Specifications](#3-technical-specifications)
4. [Package Structure](#4-package-structure)

---

## 1. Code Structure & Architectural Choices

### Overall Structure & Patterns

> Document the high-level structure and design patterns used throughout the codebase.

SnowSense is a frontend Android application built using Jetpack Compose. Its high-level architecture consists of:
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



## 2. Object-Oriented Principles

### Low Coupling, High Cohesion

> Explain how the codebase achieves low coupling between modules and high cohesion within them.

#### Low Coupling
DataSource- and Repository-layers are always exposed as **interfaces**:
- `FrostV0DataSource` --> Implemented by `KtorFrostV0DataSource`
- `ClimateRepository` --> Implemented by `FrostClimateRepository`
- `AvalancheRiskRepository` --> Implemented by `FrostAvalancheRiskRepository`

ViewModels only depend on repository interfaces, never on datasource implementations directly.

Binding takes place in `AppModule.kt` via **Koin**
```kotlin
    single<FrostV0DataSource> { KtorFrostV0DataSource(get()) }
    single<ClimateRepository> { FrostClimateRepository(get()) }
    viewModel { MapViewModel(get()) }
```
The reason this is important is because it means that it is rather trivial to replace for example Ktor with another HTTP library due to only the implementation class and AppModule being affected.

It also makes it easy to test the repository layer with one `FakeFrostV0DataSource` without touching production code.

#### High Cohesion
Every Package has one clear responsability
| package | Responsability |
| --- | --- |
| `data.datasource`| Raw communication with an external source |
| `data.datasource.dto`| Data transfer objects that mirror API-contract |
| `data.repository` | Data transformation into domain models |
| `data.model.exceptions` | Exceptions thrown by DataSources |
| `navigation` | Route definition and navigation logic |
| `domain.model` | Domain models |
| `ui.screens.<screen_name>` | Screens and their dedicated ViewModels and components |
| `ui.components` | Reusable, stateless composables for use on any screen |
| `ui.model` | UI models, mapping from domain data to UI content |
| `ui.viewmodels` | Shared ViewModels used by multiple screens |
| `network` | HTTP-client configuration |
| `di` | Dependency injection |


### MVVM & Unidirectional Data Flow (UDF)

> Describe how the MVVM pattern and UDF are applied across the app.

The app follows MVVM pattern combined with UDF resulting in the following pattern:

### UI Components
- **Observer:** consumes `screenState` via `StateFlow`
- **Sender:** emits events to ViewModel through lambda callbacks

↓ *events (`onMapClick`, `onYearChange`, etc.)*

---

### ViewModels
- Holds `MutableStateFlow<ScreenState>`
- Updates state through `.copy()`
- Calls repository via `suspend` functions

↓ *suspend calls (`getClimateDataForLocation()`, `getNamedLocations()`, etc.)*

---

### Repositories
- Aggregates data from datasources
- Returns domain objects

↓ *suspend calls (`getNearestSource()`, `getObservations()`, `fetchNamesResponse()`, etc.)

---

### DataSources
- Communicates with API via a Ktor HttpClient
- Returns DTOs

### Exceptions & Justifications

> Document any deliberate exceptions to the above principles.

---

## 3. Technical Specifications

### Architecture

> High-level architectural overview — layers, modules, and how they interact.

The **data layer** holds a Ktor API client with data sources fetching raw data from the various APIs in use (Frost, NVE, Kartverket). The repositories aggregate and transform this data from raw JSON objects containing daily weather measurements, location search responses and GeoJson polygons, returning domain objects and lists of computed averages which the rest of the app can display or use for further calculations. 

The **domain layer** holds classes used for complex logic such as slab avalanche risk estimation based on recent weather averages and selected time ranges.

The **UI layer** consists of the screens and UI components the app uses to display climate data and risk evaluations based on locations and time ranges by querying repositories and domain-layer logic through ViewModels. This data is presented through a map, trend graphs, risk level descriptions, as well as search results and saved user preferences.

### Technologies

> List and briefly describe the key technologies, frameworks, and libraries used.

| Technology | Type | Usage |
| ---------- | ---- | ----- |
| Jetpack Compose | UI Framework | Used to build MVVM-friendly UI components called Composables |
| Mapbox Maps | SDK | Used to integrate a Mapbox Map which supports navigation, interaction, and custom layers. |
| MET Frost v0     |  API endpoint    |  Used to fetch nearby weather stations and fetch historical climate data from them. |
| NVE SnøskredAktsomhet | API endpoint | Used to display and query snow avalanche risk areas calculated from terrain steepness, tree cover, etc. |
| Kartverket Stedsnavn | API endpoint | Used to fetch location search results |
| Vico | Library | Used to render trend graphs |

### API Level

> Document API-level details — endpoints, contracts, versioning, and any relevant integration specifics.

| Configuration | Value | Reason |
| --- | --- | --- |
| `minSdk` | 26 | Gives access to `java.time`-API. and useable by over 95% of devices |
| `targetSdk` | 36 | minimal stable SDK for play Store |
| `compileSdk` | 36 | Compiles with the same SDK version as target to avoid warnings |

## 4. Package Structure
```
in2000.prosjekt.in2000project/
|
|--- data/
|    |--- model/                                
|    |    |---exceptions
|    |    |    |--- NoObservationException.kt
|    |    |    |--- NoStationFoundException 
|    |
|    |--- datasource/                           # Communication with the API (Frost, NVE)
|    |    |--- dto/                             # Data objects which mirror API-response
|    |    |    |---FrostV0ObservasjonsDto.kt
|    |    |    |---FrostV0SourceDto.kt
|    |    |    |---LocationDto.kt
|    |    |--- FrostV0DataSource                # Interface
|    |    |--- KtorFrostV0DataSource            # Implementation
|    |    |--- NveDataSource
|    |    |--- LocationDataSource
|    |    |--- LocationQueryBuilder.kt
|    |    
|    |--- repository/                           # Transformation and aggregation of data
|    |    |--- ClimateRepository.kt             # Interface
|    |    |--- FrostClimateRepository.kt        # Implementation
|    |    |--- WeatherRepository.kt             # Interface
|    |    |--- FrostWeatherRepository.kt        # Implementation
|    |    |--- NveRepository.kt 
|    |    |--- SearchRepository.kt
|    |    |--- FrostReferenceTime.kt            # Helper object
|    |    |--- LocationRepository
|    
|--- domain/
|    |--- model/                               # Domain models
|    |    |---enums/
|    |    |    |--- DangerLevel
|    |    |    |--- ElementType
|    |    |    |--- MeasurementUnit
|    |    |--- DailyWeatherRecord
|    |    |--- Coordinates.kt
|    |    |--- LocationClimateData.kt
|    |    |--- LocationRiskData.kt
|    |    |--- SkiArea
|    |    |--- GeoJson
|    |    |--- LocationClimateDataPoint
|    |    |--- NamedLocation 
|    |--- SlabAvalancheRiskAssessor.kt          
|    
|--- di/
|    |--- AppModule.kt
|    | 
|--- navigation/
|    |--- Screen.kt            
|    |--- AppNavigation.kt                      # NavHost
|    |--- NavigationActions.kt
|
|--- network/
|    |---KtorClient.kt 
|    
|---ui/
|    |--- approot/                             
|    |    |--- AppRoot.kt
|    |--- components/                           # Reusable Composables
|    |--- screens/                            
|    |    |--- home/                            # HomeScreen
|    |    |--- map/                             # MapScreen relevant
|    |    |--- information/                     # InfoScreen relevant
|    |    |--- settings/                        # SettingScreen relevant
|    |--- model/ 
|    |    |--- DangerLevelUiModel.kt     
|    |--- viewmodels/                           # Shared ViewModels
|    |    |--- RiskViewModel.kt        
|    |    |--- SearchViewModel.kt    
|    |    |--- SettingsViewModel.kt                         
│    
|--- MainActivity.kt
|----SnowSenseApplication.kt
```
