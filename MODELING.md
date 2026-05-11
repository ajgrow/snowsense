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

---

## 4. Activity & State Diagrams

> Document the activity and/or state diagrams here, capturing the flow of control and state transitions within the app.

```mermaid
stateDiagram-v2
  direction TB
  [*] --> Idle

  Idle --> SearchActive : setSearchActive(true)
  SearchActive --> SearchActive : searchResults updated
  SearchActive --> LocationSelected : addRecentSearch(NamedLocation)

  state "Location selected" as LocationSelected

  state "Danger level check" as DangerLevelCheck {
    [*] --> FetchingNveGeoJson
    FetchingNveGeoJson --> AssessingSlabAvalancheRisk : isDangerZone resolved
    AssessingSlabAvalancheRisk --> DangerLevelResolved : DangerLevelUiModel set\n(LOW / MODERATE /\nCONSIDERABLE / HIGH / VERY_HIGH)
  }

  LocationSelected --> DangerLevelCheck : avalancheChecker(lat, lon)
  DangerLevelCheck --> LocationSelected : go back

  state "Historical climate data" as HistoricalClimateData {
    [*] --> ClimateIdle
    ClimateIdle --> ClimateLoading : fetchClimateData()\nstartDate / endDate set
    ClimateLoading --> ClimateSuccess : LocationClimateData received
    ClimateLoading --> ClimateError : NoObservationsException
    ClimateSuccess --> ClimateLoading : onStartDateChange /\nonEndDateChange / onYearChange
  }

  LocationSelected --> HistoricalClimateData : onMapClick → isSheetVisible = true
  HistoricalClimateData --> LocationSelected : onSheetDismissed

  state "Favoriting location" as FavoritingLocation {
    [*] --> AwaitingFavoriteName : addPendingFavorite(coordinate)
    AwaitingFavoriteName --> FavoriteSaved : nameFavorite(name)\n→ addFavoriteLocation(NamedLocation)
    AwaitingFavoriteName --> [*] : cancelPendingFavorite()
  }

  LocationSelected --> FavoritingLocation : addPendingFavorite(coordinate)
  FavoritingLocation --> LocationSelected : favorite saved / cancelled
```