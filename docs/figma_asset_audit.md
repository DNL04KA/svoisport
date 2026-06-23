# Figma Asset Audit — Svoy Sport Android TV

This document summarizes the visual asset inventory, export status, and technical integration strategy for the Svoy Sport Android TV application.

## 1. Exported Assets
These assets have been successfully extracted from Figma and integrated into the project's `app/src/main/res/drawable/` directory.

| Asset Name | Figma Node ID | Format | Role |
|---|---|---|---|
| `logo_icon.svg` | `414:10845` | SVG | Primary brand mark |
| `logo_full.svg` | `414:10898` | SVG | Full logo with typography |
| `ic_search.svg` | `399:10898` | SVG | Navigation drawer search |
| `ic_calendar.svg` | `399:10988` | SVG | Navigation drawer (Upcoming) |
| `ic_archive.svg` | `401:3308` | SVG | Navigation drawer (Archive) |
| `ic_user.svg` | `537:19013` | SVG | Navigation drawer (Profile) |
| `ic_bell.svg` | `710:40312` | SVG | Notifications / Live indicators |
| `ic_sport_football.svg` | `399:10908` | SVG | Category icon |
| `ic_sport_hockey.svg` | `399:10922` | SVG | Category icon |
| `ic_sport_basketball.svg` | `399:10935` | SVG | Category icon |
| `ic_sport_volleyball.svg` | `399:10948` | SVG | Category icon |
| `ic_sport_handball.svg` | `452:7248` | SVG | Category icon |
| `bg_main.png` | `466:18120` | PNG (2x) | Home/Main screen background |
| `splash_bg.png` | `734:11934` | PNG (2x) | Splash screen branded background |
| `app_banner.png` | `734:11934` | PNG (1280x720) | Android TV Launcher Banner (derived from Splash) |
| `ic_launcher.xml` | `414:10845` | XML (Adaptive) | App Icon (Foreground: `logo_icon.svg`) |

## 2. Code-Implemented Elements
The following visual elements are implemented directly in Jetpack Compose to ensure performance and dynamic flexibility.

*   **Focus Ring:** Custom 3dp border with animated blue glow (`#455CEB`).
*   **Linear Gradients:** Page overlays (Top/Bottom) for readability.
*   **Radial Glows:** Branding accents on the Splash and Details screens.
*   **Shadows/Elevation:** Focus scaling effects (1.05x).

## 3. Missing or Mandatory Manual Assets
The following assets were not found in a TV-ready format in the current Figma or require special handling.

| Asset | Reason | Recommendation |
|---|---|---|
| **TV App Icon** | Adaptive icon needed | **Completed.** Set up as adaptive icon using `logo_icon.svg`. |
| **Dynamic Posters** | Remote Content | Match thumbnails are loaded via the API. Use `bg_main.png` as a reliable placeholder. |

## 4. Android TV Readiness Checklist
- [x] All navigation icons are SVG for scaling.
- [x] Branding is high-fidelity and consistent.
- [x] Backgrounds are large enough for 1080p/4K TV screens (exported at 2x).
- [x] Adaptive Icon setup (Integrated via `mipmap-anydpi-v26`).
- [x] Launcher Banner setup (Integrated as `app_banner.png`).

---
**Status:** Audit Complete. Core assets are integrated.
