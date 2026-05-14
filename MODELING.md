# Modeling

## Table of Contents

- [Modeling](#modeling)
  - [Table of Contents](#table-of-contents)
  - [Use Case Diagrams](#use-case-diagrams)
  - [Sequence Diagrams](#sequence-diagrams)
  - [Class Diagrams](#class-diagrams)
  - [Activity \& State Diagrams](#activity--state-diagrams)

---

## Use Case Diagrams

> Document the use case diagrams here, illustrating the interactions between actors and the system.

---

## Sequence Diagrams

> Document the sequence diagrams here, showing how objects and components interact over time for key flows.

```mermaid
sequenceDiagram
    actor User
    participant SS as SearchSection
    participant SVM as SearchViewModel
    participant LR as LocationRepository
    participant LDS as LocationDataSource
    participant KV as Kartverket API
    participant AN as AppNavigation
    participant MSH as MapStateHolder
    participant MBV as MapboxView
    participant MR as MapRoute
    participant MS as MapScreen
    participant RVM as RiskViewModel
    participant GADUC as GetAvalancheDangerUseCase
    participant WR as WeatherRepository
    participant FDS as FrostV0DataSource
    participant SARA as SlabAvalancheRiskAssessor
    participant ARR as AvalancheRiskRepository
    participant ARDS as AvalancheRiskDataSource
    participant NVE as NVE API
    participant Frost as FrostV0 API

    User->>SS: 1. Starts typing location
    SS->>SVM: 2. onQueryChange(query)
    SVM->>SVM: 3. debounce query changes
    SVM->>LR: 4. getNamedLocations(query)
    LR->>LDS: 5. getNamesResponse(query)
    LDS->>KV: 6. query API
    KV-->>LDS: 7. JSON response / error
    LDS-->>LR: 8. List<LocationDto>?
    LR-->>SVM: 9. List<NamedLocation>
    SVM-->>SS: 10. SearchUiState.searchResults updated
    SS->>User: 11. displays results

    User->>SS: 12. Selects location from list

    SS->>AN: 13. onNavigateToMap(coordinate)
    AN->>MSH: 14. flyToSearch(Point)
    MSH->>MSH: 15. update selected map point
    MSH-->>MBV: 16. selected point / camera move
    MBV->>MBV: 17. move camera and display pin

    MSH->>MR: 18. observe selected map point
    MR-->>RVM: 19. updateDangerLevel(lat, lon)
    RVM->>GADUC: 20. invoke(Coordinate)
    GADUC->>ARR: 21. getGeoJsonStatus(lat, lon, buffer)
    ARR->>ARDS: 22. getGeoJson(lat, lon, buffer)
    ARDS->>NVE: 23. query avalanche zone
    NVE-->>ARDS: 24. GeoJSON response / error
    ARDS-->>ARR: 25. GeoJsonResponseDto
    ARR-->>GADUC: 26. isDangerZone: Boolean

    alt location not in danger zone
        GADUC-->>RVM: 27.1 DangerLevel.LOW
    end

    GADUC->>WR: 28. getWeatherRecordsForLocation(coordinate)
    WR->>FDS: 29. getNearestSource(coordinate)
    FDS->>Frost: 30. query nearest weather station
    Frost-->>FDS: 31. station JSON / error / empty response
    FDS-->>WR: 32. source: FrostV0SourceDto?

    opt station found
        WR->>FDS: 33. getObservations(sourceId, elementsCsv, referenceTime)
        FDS->>Frost: 34. query weather observations
        Frost-->>FDS: 35. observation JSON / error / empty response
        FDS-->>WR: 36. observations: FrostV0ObservationResponseDto?
    end

    alt weather data missing or unusable
        WR-->>GADUC: 37.1 throws exception / no usable records
        GADUC->>GADUC: 37.2 catch exception or detect too few records
        GADUC-->>RVM: 37.3 DangerLevel.UNKNOWN
    end

    WR-->>GADUC: 38. records: List<DailyWeatherRecord>
    GADUC->>SARA: 39. assess(records)
    SARA-->>GADUC: 40. DangerLevel
    GADUC-->>RVM: 41. DangerLevel

    RVM->>RVM: 42. update RiskUiState
    RVM-->>MS: 43. observe riskState
    MS->>User: 44. display DangerLevel

```

```mermaid
sequenceDiagram

actor User

participant MS as MapScreen
participant MV as MapboxView
participant MSH as MapStateHolder
participant MR as MapRoute
participant MVM as MapViewModel
participant CR as ClimateRepository
participant FDS as FrostV0DataSource
participant FAPI as FrostV0 API

User ->> MV: Tap location on map
MV ->> MSH: placePin(point)
MSH -->> MR: observes pinnedPoint
MR ->> MVM: onMapClick(lat,lon)
MVM ->> CR: getClimateDataForLocation(..)
CR ->> FDS: getNearestDataSource(coordinate)
FDS ->> FAPI: query

alt No station found
    FAPI-->>FDS: null
    FDS-->>CR: null
    CR-->>MVM: throws NoStationFoundException
    MVM-->>MR: observes climateState as Error
    MR-->>MS: observes climateArea as null
    MS->>User: displays error
end

FAPI-->>FDS: JSON objects
FDS-->>CR: FrostV0SourceDto
CR->>FDS: getObservations(..)
FDS->>FAPI: query

alt No observations found
    FAPI-->>FDS: null
    FDS-->>CR: null
    CR-->>MVM: throws NoObservationsException
    MVM-->>MR: observes climateState as Error
    MR-->>MS: observes climateArea as null
    MS->>User: displays error
end

FAPI-->>FDS: JSON objects
FDS-->>CR: FrostV0ObservationResponseDto
CR-->>MVM: LocationClimateData
MVM-->>MR: observes climateState as Success
MR-->>MS: observes climateArea
MS->>User: displays results
```

---

## Class Diagrams

> Document the class diagrams here, depicting the structure of the codebase including classes, attributes, methods, and relationships.

Class diagram representing the relevant data layer classes:
1. DataSources used to query Frost v0, NVE SnøskredAktsomhet and Kartverket Stedsnavn. 
2. Repositories responsible for transforming the data from the DataSources and returning domain objects used by the UI and domain layers, as well as managing preferences stored locally.
3. The interfaces which the DataSources and Repositories have implemented (where applicable.)

```mermaid
classDiagram
    class ClimateRepository {
        <<interface>>
        +getClimateDataForLocation(...) LocationClimateData
    }
    class AvalancheRiskRepository {
        <<interface>>
        +getWeatherRecordsForLocation(...) List~DailyWeatherRecord~
    }
    class FrostV0DataSource {
        <<interface>>
        +getNearestSource(coordinate: Coordinate) FrostV0SourceDto
        +getObservations(...) FrostV0ObservationResponseDto
    }
    class FrostClimateRepository {
        -frostV0DataSource: FrostV0DataSource
        +getClimateDataForLocation(...) LocationClimateData
    }
    class FrostAvalancheRiskRepository {
        -frostV0DataSource: FrostV0DataSource
        +getWeatherRecordsForLocation(...) List~DailyWeatherRecord~
    }
    class KtorFrostV0DataSource {
        -client: HttpClient
        +getNearestSource(coordinate: Coordinate)
        +getObservations(...) FrostV0ObservationResponseDto
    }
    class LocationRepository {
        -dataSource: LocationDataSource
        +getNamedLocations(query: String) List~NamedLocation~
    }
    class LocationDataSource {
        -client: HttpClient
        +getNamesResponse(query: String)
        +buildLocationQuery(searchQuery: String) String
    }
    class NveRepository {
        -nveDataSource: NveDataSource
        +getGeoJsonStatus(...) Boolean
    }
    class NveDataSource {
        -client: HttpClient
        +getGeoJson(lat: Double, lon: Double, buffer: Int)
    }
    class SearchRepository {
        -prefs: SharedPreferences
        +saveFavorites(list: List~NamedLocation~)
        +loadFavorites() List~NamedLocation~
        +saveRecentSearches(list: List~NamedLocation~)
        +loadRecentSearches() List~NamedLocation~
    }

    class NoObservationsException {
        +stationName: String?
        +elements: String?
        +referenceTime: String?
    }


    ClimateRepository <|.. FrostClimateRepository : realizes
    AvalancheRiskRepository <|.. FrostAvalancheRiskRepository : realizes
    FrostV0DataSource <|.. KtorFrostV0DataSource : realizes
    KtorFrostV0DataSource --> NoObservationsException : throws
    FrostClimateRepository --> FrostV0DataSource : uses
    FrostAvalancheRiskRepository --> FrostV0DataSource : uses
    LocationRepository --> LocationDataSource : uses
    NveRepository --> NveDataSource : uses
```
Class diagram representing the relevant domain layer classes:
1. Slab avalanche risk assessor, a business logic class which calculates estimated risk based on weather records returned by an AvalancheRiskRepository.
2. Domain models used by both the UI layer and business logic on the domain layer.

```mermaid
classDiagram
    class SlabAvalancheRiskAssessor {
        +assess(days: List~DailyWeatherRecord~) DangerLevel
        +calcStructural(allDays: List~DailyWeatherRecord~) Double
        +calcTrigger(recentDays: List~DailyWeatherRecord~) Double
        +calcRecentLoading(recentDays: List~DailyWeatherRecord~) Double
        +calcRapidWarming(recentDays: List~DailyWeatherRecord~) Double
        +normalize(value: Double, low: Double, high: Double) Double
    }
    class NamedLocation {
        +name: String
        +coordinate: Coordinate
        +locationTypeNo: String
        +locationTypeEn: String
    }
    class Coordinate {
        +latitude: Double
        +longitude: Double
    }
    class LocationClimateData {
        +coordinate: Coordinate
        +stationName: String?
        +stationDistanceKm: Double?
        +stationElevationMasl: Double?
        +periodStart: MonthDay
        +periodEnd: MonthDay
        +yearsBack: Int
        +dataPointCount: Int
        +yearlyAverages: List~LocationClimateDataPoint~
    }
    class LocationClimateDataPoint {
        +year: Int
        +type: ElementType
        +unit: MeasurementUnit
        +avgValue: Double
    }
    class ElementType {
        <<enumeration>>
        TEMPERATURE_MEAN_P1D
        PRECIPITATION_SUM_P1D
        WIND_SPEED_MEAN_P1D
        SNOW_DEPTH_MAX_P1D
        +frostElementId: String
        +defaultUnit: MeasurementUnit
    }
    class MeasurementUnit {
        <<enumeration>>
        CELSIUS
        MILLIMETERS
        METERS_PER_SECOND
        CENTIMETERS
    }
    class DailyWeatherRecord {
        +date: LocalDate
        +tempC: Double?
        +precipMm: Double?
    }
    class LocationSlabAvalancheRiskData {
        +coordinate: Coordinate
        +dangerLevel: DangerLevel
    }
    class DangerLevel {
        <<enumeration>>
        UNKNOWN
        LOW
        MODERATE
        CONSIDERABLE
        HIGH
        VERY_HIGH
    }

    SlabAvalancheRiskAssessor ..> DailyWeatherRecord : uses
    SlabAvalancheRiskAssessor --> DangerLevel : returns
    NamedLocation "1" --> "1" Coordinate : has
    LocationClimateData "1" --> "1" Coordinate : has
    LocationClimateData "1" --> "0..*" LocationClimateDataPoint : contains
    LocationClimateDataPoint "1" --> "1" ElementType : has
    LocationClimateDataPoint "1" --> "1" MeasurementUnit : has
    ElementType "1" --> "1" MeasurementUnit : has
    LocationSlabAvalancheRiskData "1" --> "1" Coordinate : has
    LocationSlabAvalancheRiskData "1" --> "1" DangerLevel : has
```

---

## Activity & State Diagrams

> Document the activity and/or state diagrams here, capturing the flow of control and state transitions within the app.

```mermaid
flowchart
    A[User selects search bar on HomeScreen]
    A --> B[Keyboard opens, user types query]
    B --> C[Fetch location results via Kartverket API]
    C --> D[Show results list]
    D --> E[User selects a result]
    E --> F[Clear search results list]
    E --> X[Save location as recent search]
    F --> Y[Navigate to map screen with the selected coordinate]
    Y --> Z[Map opens, camera flies to location]
```

### Risk assessment

> This reflects the second part of Use Case #1, after a location has been selected and the map has been opened.

```mermaid
flowchart
    A[Receive selected coordinates]
    A --> B[Fetch GeoJSON risk areas]
    B --> C{Inside danger zone polygon?}
    C -->|No| LOW[Return DangerLevel.LOW]
    C -->|Yes| D0[Fetch daily weather records]
    D0 --> WF{Weather data available?}
    WF -->|No| E[Return DangerLevel.UNKNOWN]
    WF -->|Yes| D1[Assess slab avalanche risk]
    D1 --> F[Return calculated DangerLevel]
    
    F --> G[Show DangerLevel to user]
    E --> G
    LOW --> G
```

### Historical climate data

> This reflects Use Case #2. The user starts from the map screen and taps the map to see an overview of historical climate data.

```mermaid
flowchart
    A[User taps a location on the map]
    A --> B[Fetch nearest weather station via Frost API]
    B --> C{Station found?}
    C -->|No| ERR[Display error to user: no station available]
    C -->|Yes| D[Fetch observations at station via Frost API]
    D --> E{Observations found?}
    E -->|No| ERR2[Display error to user: no observations]
    E -->|Yes| F[Return LocationClimateData]
    F --> G[Update MapScreenState.climateState to Success]
    G --> H[MapScreen displays climate data]
 
```

