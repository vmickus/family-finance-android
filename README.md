# Family Finance Android

Native Android client for [Finanças da Casa](https://app.financasdacasa.com.br) — a collaborative household finance management system built with Kotlin and Jetpack Compose.

## Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| State | ViewModel + StateFlow + Compose State |
| DI | Hilt (Dagger) |
| HTTP | Retrofit 2 + OkHttp |
| JSON | Moshi (codegen via KSP) |
| Auth | EncryptedSharedPreferences |
| Images | Coil 3 |
| Charts | Vico |
| Date/Time | kotlinx-datetime |
| Navigation | Navigation Compose (type-safe) |
| Min SDK | 26 (Android 8.0) |

## Setup

### Prerequisites

- Android Studio Ladybug (2024.2+)
- JDK 17
- Backend running locally (see [family-finance-api](https://github.com/vmickus/family-finance-api))

### Run locally

1. Open this project in Android Studio
2. Sync Gradle (Android Studio creates `local.properties` automatically)
3. Start the backend API on port 8080
4. Run on emulator or connected device

The default `API_BASE_URL` is `http://10.0.2.2:8080/api` (Android emulator loopback).

**Physical device via USB:**

```bash
adb reverse tcp:8080 tcp:8080
```

Then change `API_BASE_URL` to `http://localhost:8080/api` in `app/build.gradle.kts`.

**Physical device via Wi-Fi:**

Set `API_BASE_URL` in `app/build.gradle.kts` to your machine's local IP (phone must be on the same network).

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease

# Android Lint
./gradlew lint

# Unit tests
./gradlew testDebugUnitTest
```

## API headers

All requests include:
- `Authorization: Bearer <jwt>` (when logged in)
- `X-House-ID: <uuid>` (when a house is selected)
- `X-Platform: android` (always)

## Types (OpenAPI contracts)

Kotlin DTOs are currently hand-written in `data/model/` and `data/api/`. Generated Kotlin types are available from the contracts repo:

```bash
git clone git@github.com:vmickus/family-finance-contracts.git
cd family-finance-contracts
make gen-kotlin
```

Migration from manual DTOs to generated types is tracked separately.

## Project structure

```
app/src/main/java/com/financasdacasa/app/
  FinancasApp.kt              # @HiltAndroidApp Application
  MainActivity.kt             # Single-activity Compose host
  di/
    NetworkModule.kt           # OkHttpClient, Retrofit, Moshi providers
  data/
    api/                       # Retrofit interfaces (one per feature)
    model/                     # Data classes (API DTOs)
    repository/                # Data access layer
    interceptor/
      AuthInterceptor.kt      # Bearer token + X-House-ID + X-Platform
      ResponseInterceptor.kt  # 401/403/402 global error handling
    local/
      TokenManager.kt         # EncryptedSharedPreferences wrapper
    billing/
      BillingManager.kt       # Google Play Billing integration
  ui/
    theme/                     # Material 3 theme (teal primary)
    navigation/                # Nav graph
    screens/                   # Feature screens (one package per feature)
    components/                # Shared Compose components
  util/                        # Formatters, mappers
```

## Related repositories

- [family-finance-contracts](https://github.com/vmickus/family-finance-contracts) — OpenAPI spec and generated types
- [family-finance-api](https://github.com/vmickus/family-finance-api) — Go REST API backend
- [family-finance-web](https://github.com/vmickus/family-finance-web) — React web frontend
