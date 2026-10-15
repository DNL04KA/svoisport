# Svoy Sport TV

Android TV client for the Belarusian sports broadcaster [sport-tv.by](https://sport-tv.by):
live matches, a day-by-day schedule, a recordings archive, favourites, match
reminders, and subscription activation from the TV by QR code.

Built for the leanback launcher — the whole UI is driven by the remote control
(D-pad), with no touch input assumed.

## Screenshots

<!-- Add screenshots here, e.g.:
![Home](docs/screenshots/home.png)
![Player](docs/screenshots/player.png)
![Activation](docs/screenshots/activation.png)
-->

## Features

- **Home** — a hero banner plus horizontal rows (live now, upcoming, and a row
  per sport), filtered by sport from the left sidebar.
- **Player** — Media3/ExoPlayer with HLS, a custom overlay (play/pause, ±10 s,
  live/archive timeline), and a configurable maximum video height.
- **Schedule and archive** — matches by date from the broadcaster's feed; the
  archive plays DVR recordings with known durations.
- **Search and favourites** — search across title, description and competition,
  and a favourites list stored on the device.
- **Reminders** — a notification before the start of a match you follow,
  scheduled through `AlarmManager` and delivered by a `BroadcastReceiver`.
- **Subscription activation** — the TV opens an activation session, shows a QR
  code, and polls its status; the viewer finishes payment on a phone through the
  companion web page. Linked devices can be reviewed and disconnected from the
  TV (up to three per subscription).

## Architecture

MVVM with a thin domain layer, one `ViewModel` per screen exposing a single
immutable `UiState` via `StateFlow`; Compose screens are stateless renderers.

```
ui/ (Compose for TV, screens + components)
  └─ ViewModel (StateFlow<UiState>)
        └─ domain/ (models, MatchRepository interface)
              └─ data/ (SportTvMatchRepository, ActivationRepository, mappers)
                    └─ remote/ (SportTvApi, ActivationApi)
```

- `domain/repository/MatchRepository` is the only contract the UI knows about.
  `SportTvMatchRepository` is the production implementation; `MockMatchRepository`
  is kept for offline development and is swapped in one line in `di/AppModule`.
- `SportTvApi` is a small `HttpURLConnection` + Gson client over the public
  sport-tv.by JSON feeds (showcase, schedule by date, archive). DTOs are mapped
  to domain models in `data/mappers`.
- `ActivationApi` has a real implementation and a mock one behind the same
  interface; the base URL comes from `BuildConfig.ACTIVATION_API_BASE_URL`.
- Session state that must outlive navigation (device id, subscription,
  favourites, settings) lives in the `session/` managers backed by
  `SharedPreferences`.
- Remote-control focus is handled by helpers in `ui/components/focus` and is
  covered by unit tests, because focus order is the main source of regressions
  in a TV UI.

`website/` holds the companion activation page (static front end plus a Vercel
serverless function on Neon Postgres) that the QR code points to.

## Stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.0 |
| UI | Compose, `androidx.tv` tv-material and tv-foundation, Navigation Compose |
| DI | Hilt (KSP) |
| Playback | Media3 ExoPlayer with the HLS extension |
| Async | Coroutines and Flow |
| Networking | `HttpURLConnection` + Gson |
| Images | Coil 3 |
| QR | ZXing core |
| Build | Gradle 8.13 (Kotlin DSL), AGP 8.13, compileSdk 35, minSdk 23 |
| Tests | JUnit 4 JVM unit tests |

## Getting started

Requirements: JDK 17 for Gradle (the project compiles to JVM 11 bytecode),
Android SDK 35, and an Android TV device or emulator (a TV AVD, API 23+).

```bash
git clone https://github.com/DNL04KA/svoisport.git
cd svoisport
./gradlew installDebug
```

Then launch "Свой Спорт" from the TV launcher, or:

```bash
adb shell am start -n com.svoysport.tv/.MainActivity
```

Unit tests:

```bash
./gradlew test
```

The activation API defaults to the deployed companion page. To point a debug
build at a local server:

```bash
./gradlew installDebug -PactivationApiBaseUrl=http://10.0.2.2:3000/api
```

## Notes

- The app reads public sport-tv.by endpoints; playback requires a `User-Agent`
  and `Referer` header pair, which is why the player builds its own
  `DefaultHttpDataSource.Factory`.
- `docs/` contains design notes and architecture diagrams produced while
  building the app.
