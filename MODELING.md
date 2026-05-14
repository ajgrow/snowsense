# Modeling

## Table of Contents

- [Modeling](#modeling)
  - [Table of Contents](#table-of-contents)
  - [Use Case Diagrams](#use-case-diagrams)
  - [Sequence Diagrams](#sequence-diagrams)
  - [Class Diagrams](#class-diagrams)
  - [Activity \& State Diagrams](#activity--state-diagrams)

---

## Use Cases

### Use Case #1

#### Description

**Name:** User wants to check Danger Level for a location \
**Primary Actor:** App user (skier) \
**Secondary Actor:** NVE API, Frost API, Mapbox, Kartverket API \
**Precondition:** User starts from search bar on HomeScreen \
**Postcondition:** The user can see the calculated danger level for the chosen location, or sees dangerlevel unknown. 

#### Main Flow:

1. User starts typing location into the search bar
2. `SearchSection` calls `onQueryChange(query)`
3. `SearchViewModel` watches for query changes, debounces them, and waits for the input to settle before searching
4. `SearchViewModel` calls `LocationRepository.getNamedLocations(query)`, i.e. sends the query to LocationRepository 
5. `LocationRepository` calls `LocationDataSource.getNamesResponse(query)`, i.e. sends the query to `LocationDataSource`. 
6. `LocationsDataSource` queries Kartverket API
7. `Kartverket` returns raw JSON response to `LocationDataSource`
8. `LocationDataSource` deserializes the response into a `List<LocationDto>`
9. `LocationRepository` transforms location DTOs to a `List<NamedLocations>`
10. `SearchViewModel` updates the search results in the `SearchUiState`
11. `SearchSection` displays the results to user (as a dropdown list)
12. User selects a location from the list 
13. `SearchSection` triggers navigation to the map with the selected coordinate by calling `onNavigateToMap(coordinate)`
14. `AppNavigation` converts the coordinate to a Mapbox `Point` and calls `flyToSearch(Point)`, which tells the `MapStateHolder` to “fly” there. 
15. `MapStateHolder` updates the pinned point on the map
16. `MapStateHolder` sends the selected point with a camera-move command (FlyTo) to MapboxView
17. `MapboxView` moves the camera and displays the pin on the map
18. `MapRoute` observes the selected point
19. `MapRoute` calls `RiskViewModel.updateDangerLevel(lat, lon)`, telling `RiskViewModel` to calculate the danger level for the coordinate
20. `RiskViewModel` invokes `GetAvalancheDangerUseCase` with the coordinate
21. `GetAvalancheDangerUseCase` calls `AvalancheRiskRepository.getGeoJsonStatus(lat, lon, buffer)`, to check if whether the location is in a danger zone 
22. `AvalancheRiskRepository` calls `AvalancheRiskDataSource.getGeoJson(lat, lon, buffer)`
23. `AvalancheRiskDataSource` queries NVE API.
24. NVE API returns GeoJSON/JSON response.
25. `AvalancheRiskDataSource` deserializes the response to a `GeoJsonResponseDto`, and returns this to `AvalancheRiskRepository`
26. `AvalancheRiskRepository` returns true/false, depending on whether the response was empty or not, to `GetAvalancheDangerUseCase`.
27. If the location is within a danger zone, then  `GetAvalancheDangerUseCase` calls `WeatherRepository.getWeatherRecordsForLocation(coordinate)`, asking the `WeatherRepository` for weather records at that location
28. `WeatherRepository` calls `FrostV0DataSource.getNearestSource(coordinate)`, i.e. asking it to fetch the nearest weather station 
29. `FrostV0DataSource` queries Frost API 
30. Frost API returns station data as JSON, an error or empty response
31. `FrostV0DataSource` returns `FrostV0SourceDto` or `null` to `WeatherRepository`.
32. If a station is found, `WeatherRepository` calls `FrostV0DataSource.getObservations(…)`.
33. `FrostV0DataSource` queries Frost API for weather observations
34. Frost API returns observation data as JSON, an error or empty response
35. `FrostV0DataSource` returns `FrostV0ObservationResponseDto` or `null` to WeatherRepository
36. WeatherRepository transforms the observations into DailyWeatherRecords and returns `List<DailyWeatherRecord>` to `GetAvalancheDangerUseCase`
37. `GetAvalancheDangerUseCase` calls `SlabAvalancheRiskAssessor.assess(records)`, i.e. passing the records to `SlabAvalancheRiskAssessor` for evaluation
38. `SlabAvalancheRiskAssessor` returns a `DangerLevel` to `GetAvalancheDangerUseCase`.
39. `GetAvalancheDangerUseCase` returns that `DangerLevel` to `RiskViewModel`
40. `RiskViewModel` updates `RiskUiState`
41. `MapScreen` observes `RiskUiState`
42. `MapScreen` displays `DangerLevel` to the user

#### Alternative Flow for Step 26 **Location is not in an avalanche danger zone**

26.1. `GetAvalancheDangerUseCase` returns `DangerLevel.LOW` to `RiskViewModel`. \
26.2. Continue from step 40. \

#### Alternative Flow for Step 35: **Weather data is unavailable**

35.1. `WeatherRepository` throws an exception to `GetAvalancheDangerUseCase` due to missing or unusable weather records \
35.2. `GetAvalancheDangerUseCase` catches the exception and treats the records as empty \
35.3. `GetAvalancheDangerUseCase` returns `DangerLevel.UNKNOWN` to `RiskViewModel` \
35.4. continue from step 40


### Use Case #2

#### Description

**Name:** Tour guide wants to view historical climate data for a manually selected location \
**Primary Actor:** App user (Ski Tour Guide) \
**Secondary Actor:** Mapbox, Frost V0 API \
**Precondition:** User starts on MapScreen \
**Postcondition:** Historical climate data is displayed to the user, or an error message is shown if no station or observations were found.

#### Main Flow:

1. User manually taps a location on the map
2. `MapboxView` calls `mapStateHolder.placePin(point)`
3. `MapStateHolder` updates `pinnedPoint`
4. `MapRoute` observes `pinnedPoint`
5. `MapRoute` calls `mapViewModel.onMapClick(lat, lon)`
6. `MapViewModel` calls `climateRepository.getClimateDataForLocation(..)`
7. `ClimateRepository` calls `frostV0DataSource.getNearestSource(coordinate)`
8. `FrostV0DataSource` queries Frost API
9.  Frost API returns JSON objects to `FrostV0DataSource`
10. `FrostV0DataSource` returns `FrostV0SourceDto` to `ClimateRepository`
11. `ClimateRepository` calls `frostV0DataSource.getObservations(..)`
12. `FrostV0DataSource` queries Frost API
13. Frost API returns JSON objects to `FrostV0DataSource`
14. `FrostV0DataSource` returns `FrostV0ObservationResponse` to `ClimateRepository`
15. `ClimateRepository` returns `LocationClimateData` to `MapViewModel`
16. `MapViewModel` updates `MapScreenState.climateState` to `ClimateState.Success`
17. `MapScreen` observes `MapScreenState.climateState`
18. `MapScreen` displays results to the user

#### Alternative Flow for Step 8:

8.1 Frost API returns `null` to `FrostV0DataSource` \
8.2 `FrostV0DataSource` returns `null` to `ClimateRepository` \
8.3 `ClimateRepository` throws `NoStationFoundException(coordinate)` \
8.4 `MapViewModel` catches `NoStationFoundException(coordinate)` from `ClimateRepository` \
8.5 `MapViewModel` updates `MapScreenState.climateState` to `ClimateState.Error` \
8.6 `MapScreen` observes `MapScreenState.climateState` \
8.7 Return to main flow point 17.

#### Alternative Flow for Step 12:

12.1 Frost API returns `null` to `FrostV0DataSource` \
12.2 `FrostV0DataSource` returns `null` to `ClimateRepository` \
12.3 `ClimateRepository` throws `NoObservationsException(..)` \
12.4 `MapViewModel` catches `NoObservationsException(..)` from `ClimateRepository` \
12.5 `MapViewModel` updates `MapScreenState.climateState` to `ClimateState.Error` \
12.6 `MapScreen` observes `MapScreenState.climateState` \
12.7 Return to main flow point 17.

## Use Case Diagrams

### Use case #1

<img src="docs/use_case_1.svg">

### Use case #2

<img src="docs/use_case_2.svg">

---

## Sequence Diagrams

> Sequence diagram representing Use Case #1

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
    MR->>RVM: 19. updateDangerLevel(lat, lon)
    RVM->>GADUC: 20. invoke(Coordinate)
    GADUC->>ARR: 21. getGeoJsonStatus(lat, lon, buffer)
    ARR->>ARDS: 22. getGeoJson(lat, lon, buffer)
    ARDS->>NVE: 23. query avalanche zone
    NVE-->>ARDS: 24. GeoJSON response / error
    ARDS-->>ARR: 25. GeoJsonResponseDto
    ARR-->>GADUC: 26. isDangerZone: Boolean

    alt location not in danger zone
        GADUC-->>RVM: 26.1 DangerLevel.LOW
    end

    GADUC->>WR: 27. getWeatherRecordsForLocation(coordinate)
    WR->>FDS: 28. getNearestSource(coordinate)
    FDS->>Frost: 29. query nearest weather station
    Frost-->>FDS: 30. station JSON / error / empty response
    FDS-->>WR: 31. source: FrostV0SourceDto?

    opt station found
        WR->>FDS: 32. getObservations(sourceId, elementsCsv, referenceTime)
        FDS->>Frost: 33. query weather observations
        Frost-->>FDS: 34. observation JSON / error / empty response
        FDS-->>WR: 35. observations: FrostV0ObservationResponseDto?
    end

    alt weather data missing or unusable
        WR-->>GADUC: 35.1 throws exception / no usable records
        GADUC->>GADUC: 35.2 catch exception or detect too few records
        GADUC-->>RVM: 35.3 DangerLevel.UNKNOWN
    end

    WR-->>GADUC: 36. records: List<DailyWeatherRecord>
    GADUC->>SARA: 37. assess(records)
    SARA-->>GADUC: 38. DangerLevel
    GADUC-->>RVM: 39. DangerLevel

    RVM->>RVM: 40. update RiskUiState
    RVM->>MS: 41. observe riskState
    MS->>User: 42. display DangerLevel

```

> Sequence diagram representing Use Case #2

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
MSH ->> MSH: updates pinnedPoint
MR ->> MR: observes pinnedPoint
MR ->> MVM: onMapClick(lat,lon)
MVM ->> CR: getClimateDataForLocation(..)
CR ->> FDS: getNearestDataSource(coordinate)
FDS ->> FAPI: query

alt Station found
    FAPI-->>FDS: JSON objects
    FDS-->>CR: FrostV0SourceDto
    CR->>FDS: getObservations(..)
    FDS->>FAPI: query

    alt Observations found
        FAPI-->>FDS: JSON objects
        FDS-->>CR: FrostV0ObservationResponseDto
        CR-->>MVM: LocationClimateData
        MVM->>MVM: climateState = Success
        MR->>MS: observes climateState as Success
        MS->>User: displays results

    else No observations found
        FAPI-->>FDS: null
        FDS-->>CR: null
        CR->>MVM: throws NoObservationsException
        MVM->>MVM: climateState = Error
        MR->>MS: observes climateState as Error
        MS->>User: displays error
    end

else No station found
    FAPI-->>FDS: null
    FDS-->>CR: null
    CR->>MVM: throws NoStationFoundException
    MVM->>MVM: climateState = Error
    MR->>MS: observes climateState as Error
    MS->>User: displays error
end
```

---

## Class Diagrams

Class diagram representing the relevant data layer classes:
1. DataSources used to query Frost v0, NVE SnøskredAktsomhet and Kartverket Stedsnavn. 
2. Repositories responsible for transforming the data from the DataSources and returning domain objects used by the UI and domain layers, as well as managing preferences stored locally.
3. The interfaces which the DataSources and Repositories have implemented (where applicable.)
4. DTOs returned by DataSources to Repositories

```mermaid
classDiagram
    direction TB
    class AvalancheRiskRepository {
        <<interface>>
        +getGeoJsonStatus(lat: Double, lon: Double, buffer: Int) Boolean
    }
    class ClimateRepository {
        <<interface>>
        +getClimateDataForLocation(...) LocationClimateData?
    }
    class WeatherRepository {
        <<interface>>
        +getWeatherRecordsForLocation(...) List~DailyWeatherRecord~
    }
    class LocationRepository {
        <<interface>>
        +getNamedLocations(query: String) List~NamedLocation~
    }
    class SearchRepository {
        <<interface>>
        +saveFavorites(list: List~NamedLocation~)
        +loadFavorites() List~NamedLocation~
        +saveRecentSearches(list: List~NamedLocation~)
        +loadRecentSearches() List~NamedLocation~
    }
    class NveAvalancheRiskRepository {
        -dataSource: AvalancheRiskDataSource
        +getGeoJsonStatus(lat, lon, buffer) Boolean
    }
    class FrostClimateRepository {
        -frostV0DataSource: FrostV0DataSource
        +getClimateDataForLocation(...) LocationClimateData
    }
    class FrostWeatherRepository {
        -frostV0DataSource: FrostV0DataSource
        +getWeatherRecordsForLocation(...) List~DailyWeatherRecord~
    }
    class KartverketLocationRepository {
        -dataSource: LocationDataSource
        +getNamedLocations(query) List~NamedLocation~
    }
    class SharedPreferencesSearchRepository {
        -prefs: SharedPreferences
        +saveFavorites(list)
        +loadFavorites() List~NamedLocation~
        +saveRecentSearches(list)
        +loadRecentSearches() List~NamedLocation~
    }
    class AvalancheRiskDataSource {
        <<interface>>
        +getGeoJson(lat: Double, lon: Double, buffer: Int) GeoJsonResponseDto
    }
    class FrostV0DataSource {
        <<interface>>
        +getNearestSource(coordinate: Coordinate) FrostV0SourceDto?
        +getObservations(sourceId: String, elementsCsv: String, referenceTime: String) FrostV0ObservationResponseDto?
    }
    class LocationDataSource {
        <<interface>>
        +getNamesResponse(query: String) List~LocationDto~?
    }
    class KtorAvalancheRiskDataSource {
        -client: HttpClient
        +getGeoJson(lat, lon, buffer) GeoJsonResponseDto
    }
    class KtorFrostV0DataSource {
        -client: HttpClient
        +getNearestSource(coordinate) FrostV0SourceDto?
        +getObservations(sourceId, elements, time) FrostV0ObservationResponseDto?
    }
    class KtorLocationDataSource {
        -client: HttpClient
        +getNamesResponse(query) List~LocationDto~?
        +buildLocationQuery(searchQuery) String
    }
    %% MET Frost DTOs
    class FrostV0ObservationResponseDto {
        +currentItemCount: Int
        +totalItemCount: Int
    }
    class FrostV0ObservationsAtRefTimeDto {
        +sourceId: String
        +referenceTime: String
    }
    class FrostV0ObservationDto {
        +elementId: String
        +value: Double
        +unit: String
    }
    class FrostV0SourceResponseDto
    class FrostV0SourceDto {
        +id: String?
        +name: String?
        +distance: Double?
        +masl: Double?
    }
    class FrostV0PointDto {
        +type: String
        +coordinates: List~Double~
    }
    class GeoJsonResponseDto {
        +type: String?
    }
    class FeatureDto
    class GeometryDto {
        +type: String?
        +coordinates: List~List~List~Double~~~?
    }
    class PropertiesDto {
        +objekttype: String?
        +skredtype: Int?
    }

    class LocationResponseDto
    class LocationDto {
        +navneobjekttype: String
        +skrivemåte: String
    }
    class CoordinateDto {
        +nord: Double
        +øst: Double
    }

    NveAvalancheRiskRepository --> AvalancheRiskDataSource : uses
    FrostWeatherRepository --> FrostV0DataSource : uses
    FrostClimateRepository --> FrostV0DataSource : uses
    KartverketLocationRepository --> LocationDataSource : uses
    GeoJsonResponseDto "1" --> "0..*" FeatureDto : features
    FeatureDto "1" --> "0..1" GeometryDto : geometry
    FeatureDto "1" --> "0..1" PropertiesDto : properties
    FrostV0ObservationResponseDto "1" --> "0..*" FrostV0ObservationsAtRefTimeDto : data
    FrostV0ObservationsAtRefTimeDto "1" --> "0..*" FrostV0ObservationDto : observations
    FrostV0SourceResponseDto "1" --> "0..*" FrostV0SourceDto : data
    FrostV0SourceDto "1" --> "0..1" FrostV0PointDto : geometry
    LocationResponseDto "1" --> "0..*" LocationDto : navn
    LocationDto "1" --> "1" CoordinateDto : representasjonspunkt
    KtorAvalancheRiskDataSource ..> GeoJsonResponseDto : fetches
    KtorFrostV0DataSource ..> FrostV0ObservationResponseDto : fetches
    KtorFrostV0DataSource "1" ..> "1" FrostV0SourceResponseDto : fetches
    KtorLocationDataSource "1" ..> "1" LocationResponseDto : fetches
    AvalancheRiskRepository "1" <|.. "1" NveAvalancheRiskRepository
    ClimateRepository "1" <|.. "1" FrostClimateRepository
    WeatherRepository "1" <|.. "1" FrostWeatherRepository
    LocationRepository "1" <|.. "1" KartverketLocationRepository
    SearchRepository "1" <|.. "1" SharedPreferencesSearchRepository
    AvalancheRiskDataSource "1" <|.. "1" KtorAvalancheRiskDataSource
    FrostV0DataSource "1" <|.. "1" KtorFrostV0DataSource
    LocationDataSource "1" <|.. "1" KtorLocationDataSource
    
```
Class diagram representing the relevant domain layer classes:
1. Use case for getting a danger level.
2. Slab avalanche risk assessor, a business logic class which calculates estimated risk based on weather records returned by an AvalancheRiskRepository.
3. Domain models used by both the UI layer and business logic on the domain layer.

Repository interfaces have been omitted.

```mermaid
classDiagram
    class WeatherRepository  { <<interface>> }
    class AvalancheRiskRepository { <<interface>> }

    class SlabAvalancheRiskAssessor {
        +assess(days: List~DailyWeatherRecord~) DangerLevel
        ~calcStructural(allDays: List~DailyWeatherRecord~) Double
        ~calcTrigger(recentDays: List~DailyWeatherRecord~) Double
        ~calcRecentLoading(recentDays: List~DailyWeatherRecord~) Double
        ~calcRapidWarming(recentDays: List~DailyWeatherRecord~) Double
        ~normalize(value: Double, low: Double, high: Double) Double
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
    class GetAvalancheDangerUseCase {
        +weatherRepository: WeatherRepository
        +avalancheRiskRepository: AvalancheRiskRepository
        +slabAvalancheRiskAssessor: SlabAvalancheRiskAssessor
        +invoke(coordinate: Coordinate) DangerLevel
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

    GetAvalancheDangerUseCase "1" --> "1" SlabAvalancheRiskAssessor : uses
    GetAvalancheDangerUseCase ..> DangerLevel : returns
    GetAvalancheDangerUseCase "1" --> "1" WeatherRepository : uses
    GetAvalancheDangerUseCase "1" --> "1" AvalancheRiskRepository : uses   
    SlabAvalancheRiskAssessor ..> DailyWeatherRecord : uses
    SlabAvalancheRiskAssessor ..> DangerLevel : returns
    NamedLocation "1" --> "1" Coordinate : has
    LocationClimateData "1" --> "1" Coordinate : has
    LocationClimateData "1" --> "0..*" LocationClimateDataPoint : contains
    LocationClimateDataPoint "1" --> "1" ElementType : has
    LocationClimateDataPoint "1" --> "1" MeasurementUnit : has
    ElementType "1" --> "1" MeasurementUnit : has
```

---

## Activity & State Diagrams

> This reflects the first part of Use Case #1, where the user searches for a location.

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

