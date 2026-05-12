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

