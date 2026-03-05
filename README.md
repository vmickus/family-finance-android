# Finanças da Casa — Android

Native Android client for Finanças da Casa, built with Kotlin and Jetpack Compose. Shares the same Go + PostgreSQL backend as the web app.

## Architecture

- **Pattern**: MVVM (ViewModel + StateFlow) with Compose UI
- **DI**: Hilt (Dagger)
- **Networking**: Retrofit 2 + OkHttp + Moshi (codegen)
- **Navigation**: Jetpack Navigation Compose (type-safe)
- **Auth storage**: EncryptedSharedPreferences
- **Image loading**: Coil 3
- **Minimum SDK**: 26 (Android 8.0)

### Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| State | ViewModel + StateFlow + Compose State |
| DI | Hilt |
| HTTP | Retrofit 2 + OkHttp |
| JSON | Moshi (codegen) |
| Auth | EncryptedSharedPreferences |
| Images | Coil 3 |
| Date/Time | kotlinx-datetime |
| Navigation | Navigation Compose |

### Project Structure

```
app/src/main/java/com/financasdacasa/app/
  FinancasApp.kt              # @HiltAndroidApp Application
  MainActivity.kt             # Single-activity Compose host
  di/
    NetworkModule.kt           # OkHttpClient, Retrofit, Moshi providers
  data/
    api/                       # Retrofit interfaces (one per feature)
    model/                     # Data classes (API DTOs)
    interceptor/
      AuthInterceptor.kt      # Bearer token + X-House-ID + X-Platform
      ResponseInterceptor.kt  # 401/403/402 global error handling
    local/
      TokenManager.kt         # EncryptedSharedPreferences wrapper
  ui/
    theme/                     # Material 3 theme (teal primary)
    navigation/                # Nav graph
    screens/                   # Feature screens (one package per feature)
```

## Local Development Setup

### Prerequisites

- Android Studio Ladybug (2024.2+)
- JDK 17
- Backend running locally (`cd server && go run ./cmd/api`)

### Option 1: ADB Port Forwarding (recommended)

Connect your phone via USB and forward the backend port:

```bash
adb reverse tcp:8080 tcp:8080
```

The default `API_BASE_URL` in the app is `http://10.0.2.2:8080/api` (Android emulator loopback). For a physical device with ADB reverse, change it to `http://localhost:8080/api` in `app/build.gradle.kts`.

### Option 2: Wi-Fi (same network)

1. Find your machine's local IP:
   ```bash
   hostname -I | awk '{print $1}'
   ```
2. Set `API_BASE_URL` in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:8080/api\"")
   ```
3. Phone must be on the same Wi-Fi network as the dev machine.

### Running

1. Open the `android/` directory in Android Studio
2. Sync Gradle
3. Run on emulator or connected device

## API Headers

All requests include:
- `Authorization: Bearer <jwt>` (when logged in)
- `X-House-ID: <uuid>` (when a house is selected)
- `X-Platform: android` (always)

## Phase Roadmap

| Phase | Features | Depends On |
|-------|----------|------------|
| **1** | Project setup + Auth (login/register) + House selection | Backend X-Platform |
| **2** | Home dashboard + Transaction CRUD + Bottom nav | Phase 1 |
| **3** | Categories (CRUD + reorder) + Budget limits | Phase 2 |
| **4** | Garden (goals + allocations) + Recurring transactions | Phase 2 |
| **5** | Analytics/Dashboard + Subscription (Mercado Pago) | Phase 2 |
| **6** | Polish: invites, avatar upload, offline cache, notifications, widgets | Phase 5 |

### Phase 1 — Auth + House Selection
- Login / Register screens (email + password)
- Google Sign-In
- Email verification notice screen
- House list + create / join house
- Persistent auth (EncryptedSharedPreferences)
- Bottom navigation shell

### Phase 2 — Home + Transactions
- Monthly summary dashboard (income/expense/balance)
- Transaction list with search
- Create/edit/delete transaction dialogs
- Category picker
- Date picker (native Android)
- Pull-to-refresh

### Phase 3 — Categories + Budgets
- Category list (expense/income toggle)
- Create/edit category (icon + color pickers)
- Drag-to-reorder categories
- Budget limit per category
- Budget progress indicators

### Phase 4 — Garden + Recurring
- Garden page with goal cards + plant illustrations
- Goal detail page with allocation history
- Water garden flow (distribute by priority)
- Recurring transaction CRUD
- Frequency configuration

### Phase 5 — Analytics + Subscription
- Annual report with charts
- Monthly history trends
- Subscription status + paywall
- Google Play Billing Library integration
  - Product IDs: `financas_monthly` (R$16.90/mo), `financas_annual` (R$169.90/yr)
  - Purchase token validated server-side via Google Play Developer API
  - Fallback: Mercado Pago checkout (WebView) if Play Billing unavailable

### Phase 6 — Polish
- House invites (deep links)
- Avatar upload (camera + gallery)
- Offline cache (Room)
- Push notifications (FCM)
- Home screen widget
