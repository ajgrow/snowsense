# Architecture

## Table of Contents

1. [Code Structure & Architectural Choices](#1-code-structure--architectural-choices)
2. [Object-Oriented Principles](#2-object-oriented-principles)
3. [Technical Specifications](#3-technical-specifications)

---

## 1. Code Structure & Architectural Choices

### Overall Structure & Patterns

> Document the high-level structure and design patterns used throughout the codebase.

SnowSense's architecture primarily follows the MVVM (Model-View-ViewModel) pattern, enabling separation of concerns. The application's structure is divided into three layers: a data layer, domain layer and UI layer.

The **data layer** uses a Ktor API client with data sources fetching raw data from the various APIs in use (Frost, NVE, Kartverket). Repositories aggregate and transform this data from raw JSON objects containing daily weather measurements, location search responses and GeoJson polygons, into kotlin objects and computed averages which the rest of the app can display or use for further calculations. 

The **domain layer** is used for complex logic such as avalanche risk estimation based on recent weather averages and selected time ranges.

The **UI layer** consists of the screens and UI components the app uses to display climate data and risk evaluations based on locations and time ranges by querying repositories and domain-layer logic through ViewModels. This data is presented through trend graphs and risk level descriptions, as well as search results and saved user preferences.

#### Patterns used throughout the project


### Functional Decisions

> Describe the choices made when planning and building the app's functionality.

---

## 2. Object-Oriented Principles

### Low Coupling, High Cohesion

> Explain how the codebase achieves low coupling between modules and high cohesion within them.

### MVVM & Unidirectional Data Flow (UDF)

> Describe how the MVVM pattern and UDF are applied across the app.

The application is built around MVVM patterns and unidirectional data flow. As an example:

On the View layer, the SearchBar composable calls "onQueryChanged" which is a lambda passed down through the UI call tree to the SearchBar from the SearchViewModel (on the ViewModel layer). 

This ViewModel then queries the LocationRepository which in turn fetches a raw response from the KartVerket Stedsnavn API through the LocationDataSource and sorts out relevant data from DTOs into a list of usable NamedLocation objects. Both the Repository and DataSource belong to the Model layer.

Once the SearchViewModel has received a list of search results, it updates the SearchUiState. This state is observed by the SearchBar composable, which is then updated to show the search results to the user.

### Exceptions & Justifications

> Document any deliberate exceptions to the above principles.

**`MapStateHolder`** *(if not replaced)*

> This object may deviate from standard patterns. Provide reflection and justification for why this exception exists and what trade-offs were accepted.

---

## 3. Technical Specifications

### Architecture

> High-level architectural overview — layers, modules, and how they interact.

### Technologies

> List and briefly describe the key technologies, frameworks, and libraries used.

### API Level

> Document API-level details — endpoints, contracts, versioning, and any relevant integration specifics.