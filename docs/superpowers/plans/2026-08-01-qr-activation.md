# QR Activation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect the Android TV QR screen and a mobile browser page through one expiring activation session, while retaining a deterministic offline mock.

**Architecture:** The existing Node static server owns an in-memory development session store and exposes create, status, and activate endpoints. The Android client uses its existing `ActivationApi` contract against configurable endpoints; the mobile `activate.html` page validates the session and confirms activation. Production deployment must replace the in-memory store with persistent server-side storage and authenticated payment confirmation.

**Tech Stack:** Android TV, Kotlin, Compose, coroutines, ZXing, Node.js built-ins, vanilla HTML/CSS/JavaScript, `node:test`.

## Global Constraints

- Poll the TV session every 3 seconds.
- Sessions expire after 15 minutes.
- A session may move from `waiting` to `activated` only once.
- The mobile page requires an explicit confirmation; development authentication is mocked.
- Do not treat the browser as authoritative for a real payment; production activation must happen after server-side payment confirmation.

---

### Task 1: Development activation session store

**Files:**
- Create: `website/activation-session-store.js`
- Create: `website/test/activation-session-store.test.js`
- Modify: `website/serve.js`

**Interfaces:**
- Produces: `createSession(deviceId)`, `getStatus(sessionId)`, and `activateSession(sessionId)`.

- [ ] Write tests for creation, activation, unknown sessions, expiry, and one-way transitions.
- [ ] Run `node --test website/test/activation-session-store.test.js` and confirm failure because the module is missing.
- [ ] Implement the in-memory store and JSON request parsing.
- [ ] Add `POST /api/create-activation-session.php`, `GET /api/check-activation-session.php`, `GET /api/activation-session.php`, `POST /api/activate-session.php`, and `GET /api/check-subscription.php`.
- [ ] Re-run the focused Node tests and confirm they pass.

### Task 2: Mobile QR destination

**Files:**
- Create: `website/activate.html`
- Create: `website/js/activate.js`
- Modify: `website/css/styles.css`

**Interfaces:**
- Consumes: `GET /api/activation-session.php?session=...` and `POST /api/activate-session.php`.
- Produces: mobile states for loading, invalid/expired, confirmation, and success.

- [ ] Add an HTTP integration test for the activation flow and confirm it fails before the route/page exists.
- [ ] Build an accessible responsive confirmation page that reads the `session` parameter.
- [ ] Validate the session and activate it only after explicit form submission.
- [ ] Run the integration test and confirm it passes.

### Task 3: Android client alignment

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/kotlin/com/svoysport/tv/data/remote/activation/RealActivationApi.kt`
- Modify: `app/src/main/kotlin/com/svoysport/tv/data/remote/activation/MockActivationApi.kt`
- Modify: `app/src/main/kotlin/com/svoysport/tv/di/AppModule.kt`
- Modify: `app/src/main/kotlin/com/svoysport/tv/ui/screens/activation/ActivationViewModel.kt`
- Modify: `app/src/main/kotlin/com/svoysport/tv/ui/screens/activation/ActivationScreen.kt`

**Interfaces:**
- Consumes: `BuildConfig.ACTIVATION_API_BASE_URL` and the activation endpoint contract.
- Produces: Figma-aligned QR modal, deterministic polling, expiry handling, and retry.

- [ ] Add focused unit tests for status parsing and session URL encoding; run them and confirm failure.
- [ ] Make endpoint base URLs configurable and URL-encode parameters.
- [ ] Keep the mock deterministic and isolated per session.
- [ ] Add the 15-minute client timeout and align TV copy/layout with the Figma QR screen.
- [ ] Run Android unit tests and a debug build.

### Task 4: End-to-end verification and documentation

**Files:**
- Modify: `docs/data_layer_sport_tv.md`

**Interfaces:**
- Documents local startup, device URL configuration, production PHP/database responsibilities, and payment webhook boundary.

- [ ] Start the local server and create a session through HTTP.
- [ ] Load `activate.html?session=...`, confirm activation, and verify status changes to `activated`.
- [ ] Run the full Node test suite and Android build/tests.
- [ ] Review `git diff` for security, accessibility, performance, and unintended changes.
