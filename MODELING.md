# Modeling

## Table of Contents

1. [Use Case Diagrams](#1-use-case-diagrams)
2. [Sequence Diagrams](#2-sequence-diagrams)
3. [Class Diagrams](#3-class-diagrams)
4. [Activity & State Diagrams](#4-activity--state-diagrams)

---

## 1. Use Case Diagrams

> Document the use case diagrams here, illustrating the interactions between actors and the system.

---

## 2. Sequence Diagrams

> Document the sequence diagrams here, showing how objects and components interact over time for key flows.

---

## 3. Class Diagrams

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
  
  ClimateRepository <|.. FrostClimateRepository : realizes
  AvalancheRiskRepository <|.. FrostAvalancheRiskRepository : realizes
  FrostV0DataSource <|.. KtorFrostV0DataSource : realizes
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

    SlabAvalancheRiskAssessor ..> DailyWeatherRecord
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

## 4. Activity & State Diagrams

> Document the activity and/or state diagrams here, capturing the flow of control and state transitions within the app.