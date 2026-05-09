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

## 3. Class Diagram

> Document the class diagrams here, depicting the structure of the codebase including classes, attributes, methods, and relationships.

Class diagram of the data layer, depicting the following:
1. DataSources used to query Frost v0, NVE SnøskredAktsomhet and Kartverket Stedsnavn. 
2. Repositories responsible for transforming the data from the DataSources into domain objects used by the UI and domain layers, as well as managing preferences stored locally.
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
    +getNearestSource(coordinate: Coordinate): FrostV0SourceDto
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

  class LocationRepository {
    -dataSource: LocationDataSource
    +getNamedLocations(query: String) List~NamedLocation~
  }

  class NveRepository {
    -nveDataSource: NveDataSource
    +getGeoJsonStatus(...) Boolean
  }

  class SearchRepository {
    -prefs: SharedPreferences
    +saveFavorites(list: List~NamedLocation~)
    +loadFavorites() List~NamedLocation~
    +saveRecentSearches(list: List~NamedLocation~)
    +loadRecentSearches() List~NamedLocation~
  }

  class KtorFrostV0DataSource {
    -client: HttpClient
    +getNearestSource(coordinate: Coordinate)
    +getObservations(...) FrostV0ObservationResponseDto
  }

  class LocationDataSource {
    -client: HttpClient
    +getNamesResponse(query: String)
    +buildLocationQuery(searchQuery: String) String
  }

  class NveDataSource {
    -client: HttpClient
    +getGeoJson(lat: Double, lon: Double, buffer: Int)
  }

  ClimateRepository <|.. FrostClimateRepository : realizes
  AvalancheRiskRepository <|.. FrostAvalancheRiskRepository : realizes
  FrostV0DataSource <|.. KtorFrostV0DataSource : realizes

  FrostClimateRepository --> FrostV0DataSource : uses
  FrostAvalancheRiskRepository --> FrostV0DataSource : uses
  LocationRepository --> LocationDataSource : uses
  NveRepository --> NveDataSource : uses
```

---

## 4. Activity & State Diagrams

> Document the activity and/or state diagrams here, capturing the flow of control and state transitions within the app.