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

---

## Activity & State Diagrams

> Document the activity and/or state diagrams here, capturing the flow of control and state transitions within the app.