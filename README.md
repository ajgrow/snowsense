# IN2000 – Android app project

SnowSense is an Android-app designed to help users evaluate avalanche risk and climate conditions for locations around Norway. The app allows users to search for locations, see historical climate data, gives evaluation for danger based on Frost and NVE data, and save locations as favourites for quicker access.

---
# Team-members.

- Alex
- Gustav
- Larissa
- Lisa
- Rukman
- Sondre

---

## App requirements
- **Minimum SDK:** API 25 (Android 8.0)
- **Target SDK:** API 36 (Android 15)
- **Recommended emulator:** Resizable emulator with API 36
- Internet access is required to retrieve data from our different APIs, Frostv0, NVE and Kartverket
- Mapbox map requires a legitimate Mapbox access token (more information below)

---

## Setup

### 1. Download or copy the project
After having either downloaded or copied the project, open it through "Android Studio". 

### 2. Set up API-keys in 'local.properties'

Since the app requires private keys that are not included in the github, you'll have to add them yourself.
Navigate to 'Gradle Scripts/local.properties', before you can click run to launch the app write the following in the local.properties file:

```
FROST_CLIENT_ID=<your Frost API client ID>
MAPBOX_ACCESS_TOKEN=>your Mapbox access token>
```

- **Mapbox token:** To get the mapbox token, you'll have to register on [Mapbox.com](https://www.mapbox.com/)
- **Frost Client ID:** To get the Frost Client ID, you'll have to register on [frost.met.no](https://frost.met.no/index.html)


### 3. Running the App
Click **Run App** in Android Studio. An Android Virtual Device will start up, and the app will both install and run automatically from there.

## Dependencies and permissions
The app requires:
- **Internet access** ('android.permission.INTERNET') - to retrieve data from external APIs

The app does not use your device's GPS or location data. All location choices happen through the app

---

## Project Documentation

- ARCHITECTURE.md: Explains our overall use of MVVM and UDF pattern, general package structure and also low coupling / high cohesion principles
- MODELING.md: Contains all of our diagrams: Use-Case-, Class-, Sequence- and Activity-Diagrams that illustrate our system and also different data flows 

## Libraries

### Used in the course IN2000

| Library | how it's used |
| ------ | --- |
| [Jetpack Compose](https://developer.android.com/develop/ui) | The UI framework for Android. Used throughout the entire UI-layer |
| [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) | Handles the logic behind presentation and communication between the UI-layer and the data-layer |
| [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) | Handles Lifecycle dependent Composables and coroutines |
| [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation) | Handles Navigation between the different screens |
| [Ktor](https://ktor.io/) | Asynchronous HTTP-client to retrieve data from Frost, NVE and Kartverket |
| [Kotlinx Serialization](https://kotlinlang.org/docs/serialization.html#formats) | Handles the JSON-deserialization of the dataclass API responses |
| [Kotlinx Coroutines](https://developer.android.com/kotlin/coroutines) | Handles Asynchronous and reactive data retrieving |

### Not used in IN2000

| Library | how it's used |
| ---- | ---- |
| [Mapbox Compose maps](https://docs.mapbox.com/android/maps/guides/using-jetpack-compose/) | An interactive map that lets users choose locations visually |
| [Koin](https://insert-koin.io/) | Kotlin dependency. Used to connect datasources, repositories and ViewModels |
| [Vico graphs](https://guide.vico.patrykandpatrick.com/) | A graph library used for visualization of historical climate data. We use Vico 1x since newer version breaks the rendering |
| [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) | Easy and reactive local storage. Used to save the users favourite locations | 
| [Desugar](https://developer.android.com/studio/write/java8-support-table) | Makes it possible to use older Java APIs on older Android versions (such as 'java.time') |
| [AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat) | Gives assistance to the language changing through different Android versions |
| [Material Icon Extension](https://developer.android.com/jetpack/androidx/releases/compose-material) | Extension of the icon library from material design. Used for icons not available in the standard version |

---

## API usage

| API | Source | use case |
| --- | --- | --- |
| [Frost API v0](https://frost.met.no/index.html) | MET Norway | Historical climate observations from nearest measuring station: requires Client-ID |
| [NVE Skredaktomshet](https://kart.nve.no/enterprise/rest/services/SnoskredAktsomhet/MapServer) | NVE | GeoJSON-data about avalanche prone areas for chosen location. WMS map layer that shows avalanche risk zones on the map through a raster layer. More details are shown the closer you zoom |
| [Kartverket Stedsnvan](https://www.kartverket.no/api-og-data/stedsnavndata) | Kartverket | Geocoding and location searching - converts location names to coordinates |

## Testing

The project includes Unit tests and integration tests.

- **Running unit tests:** You can run the Unit tests directly in Android Studio by right-clicking the test-folder and choosing *Run Tests*, or alternatively you can do it through the terminal:
  ```
  ./gradlew test
  ```
  - **Integration testing:** done through Frost API therefore requires that the 'FROST_CLIENT_ID' is set up in 'local.properties'), you can run this directly or through the terminal:
    ```
    ./gradlew test --tests "*.FrostApiIntegrationTest"
    ```

    ---

## Accessibility Features

The app was designed with WCAG 2.1 in mind and has the following features:
- Theme adjustment: The app supports a full system-wide dark mode and light mode through the use of Material 3
- Text size adjustment: The app supports a text adjustment range of 50% original size all the way to 200% original size. The chosen standard is 150%
- Screen Reader: The app supports Android's built-in screen reader through content descriptions and no hardcoded strings

---

## Known warnings and restrictions
