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
