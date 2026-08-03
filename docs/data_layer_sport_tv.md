# Data Layer Integration — Sport-TV.by

This document describes how the Android TV application consumes and maps sports schedule data.

## 1. External Source: sport-tv.by
The primary schedule source is `https://sport-tv.by/list.php`.

### JSON Structure
```json
{
  "date": "1776726600",
  "date_end": "1776734400",
  "title": "Футбол. Чемпионат Колумбии. Примера А. Онсе Кальдас - Интер Богота",
  "annotation": "Футбол. Чемпионат Колумбии",
  "sport_id": "40535",
  "source": "no",
  "category_id": "1"
}
```

## 2. Mapping Strategy
Data is transformed via `SportTvMappers.kt` at the data layer boundary.

- **Time:** `date` and `date_end` (Unix seconds) are converted to milliseconds for Kotlin `Long` / `Instant`.
- **Title Parsing:** The `title` field is split by `. ` to extract categories and the team match-up (delimited by ` - `).
- **ID:** Synthetic IDs are generated as `${sportId}_${date}` to ensure unique stable IDs for the TV UI.

## 3. Backend Requirements
To keep the TV client lightweight, the backend is expected to:
1. **Aggregate:** Periodically fetch the sport-tv.by schedule.
2. **Filter:** Group matches into "Today", "Live", and "Upcoming" based on server-side time.
3. **Resolve Streams:** Scrape or resolve actual stream URLs (HLS) which are missing in the `list.php` source.
4. **Clean API:** Expose `/api/home` and `/api/match/{id}` endpoints for the TV app.

## 4. Error Handling
The repository uses the Kotlin `Result<T>` pattern. 
- **Network Errors:** Surfaced as `Result.failure` and caught by ViewModels to show the Error State UI.
- **Empty States:** Handled at the domain layer if no matches are returned for a category.

## 5. QR activation development flow

The development website server contains an in-memory activation backend. A session is valid for 15 minutes and moves only from `waiting` to `activated` (or `expired`). Restarting the Node process clears all development sessions.

Start the website so that the QR URL is reachable from a phone on the same network:

```bash
cd website
ACTIVATION_PUBLIC_ORIGIN=http://192.168.1.50:4173 node serve.js 4173
```

Replace `192.168.1.50` with the computer's LAN address. Android Emulator debug builds use `http://10.0.2.2:4173/api` by default. For a physical TV or another emulator address, build with:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew assembleDebug -PactivationApiBaseUrl=http://192.168.1.50:4173/api
```

The flow is:

1. TV calls `POST /api/create-activation-session.php` with `device_id`.
2. The response contains `sessionId`, `qrUrl`, and `expiresAt`.
3. TV polls `GET /api/check-activation-session.php?sessionId=...` every 3 seconds.
4. The phone opens `/activate.html?session=...`, validates the session, and asks for explicit confirmation.
5. The page calls `POST /api/activate-session.php`; the next TV poll receives `activated`.
6. TV calls `GET /api/check-subscription.php?device_id=...` and stores the subscription state.

The e-mail form and subscription grant in this development server are mocks. Production must authenticate the user, bind the session to the originating device, store only a cryptographic session-token hash, enforce expiry transactionally, and change the status only after a verified payment-provider webhook. The browser must never be the authority for a successful payment.
