# Play Store Publishing - Design Spec

## Overview

Prepare the Finanças da Casa Android app for Google Play Store publication. The approach combines full release readiness (signing, proguard, production config, billing, OAuth) with an Internal Testing track for validation before public launch.

## 1. Release Build Configuration

### Signing

- Generate upload keystore (`upload-keystore.jks`) via `keytool`
- Add `signingConfigs.release` in `app/build.gradle.kts`, reading credentials from `~/.gradle/gradle.properties` (never committed to repo)
- Use Google Play App Signing (Google manages the final distribution key; we hold the upload key)
- Add `*.jks` and `*.keystore` to `.gitignore` to prevent accidental commit of signing keys

### Production Build Config

In `app/build.gradle.kts`:

- `API_BASE_URL` (release) → `https://app.financasdacasa.com.br/api` (must be added as `buildConfigField` inside the `release {}` block, since `defaultConfig` falls back to the emulator URL)
- `GOOGLE_CLIENT_ID` → read from `gradle.properties` (set after creating OAuth credential in GCP)
- `GP_MONTHLY_PRODUCT_ID` = `financas_monthly` (already set)
- `GP_ANNUAL_PRODUCT_ID` = `financas_annual` (already set)

### ProGuard/R8 Rules

Create `app/proguard-rules.pro` with keep rules for:

- Retrofit (service interfaces, annotations)
- Moshi (`@JsonClass` annotated models, generated adapters)
- Hilt (DI annotations)
- Jetpack Compose
- Coil (image loading)
- Vico (charts)
- Google Play Billing
- OkHttp
- Kotlin coroutines/serialization
- Room (entities, DAOs)
- AndroidX Security / EncryptedSharedPreferences (uses reflection internally)

### Build Output

`./gradlew bundleRelease` produces a signed `.aab` (Android App Bundle) with the upload key.

## 2. AndroidManifest & Security

### cleartext Traffic

- Remove `android:usesCleartextTraffic="true"` from main manifest
- Add `res/xml/network_security_config.xml` allowing cleartext only for debug domains (10.0.2.2, localhost)
- Reference the config via `android:networkSecurityConfig` in manifest
- This is the Google-recommended approach over a debug manifest overlay

### Backup Rules

- Replace `android:allowBackup="true"` with `android:dataExtractionRules` (Android 12+)
- Create `res/xml/data_extraction_rules.xml` to exclude sensitive data (tokens, session)
- Create `res/xml/backup_rules.xml` for pre-Android 12

### Permissions

Already declared and correct:

- `INTERNET` - required for API calls
- `VIBRATE` - optional
- `CAMERA` - declared with `required="false"`

To add:

- `READ_MEDIA_IMAGES` (Android 13+) - for accessing photo gallery
- `READ_EXTERNAL_STORAGE` (Android 12 and below, with `maxSdkVersion="32"`) - legacy gallery access

Both are runtime permissions requested at point of use.

## 3. Google OAuth for Android

### Google Cloud Console (manual steps)

- Create an **Android OAuth Client ID** in GCP project `financas`
  - Package name: `com.financasdacasa.app`
  - SHA-1: from the upload keystore (generated after signing setup)
- Verify/locate the existing **Web Client ID** (used by backend for token validation)

### App Configuration

- `GOOGLE_CLIENT_ID` in BuildConfig = the **Web Client ID** (backend validates against this)
- Android Client ID is used implicitly by Google Play Services / Credential Manager API

### Flow

1. App requests Google Sign-In via Credential Manager
2. Google returns ID token
3. App sends token to backend (`/google-login`)
4. Backend validates token with Web Client ID
5. Backend returns session JWT

Email/password login remains fully functional as an alternative.

## 4. Deep Links / Digital Asset Links

The manifest already declares `android:autoVerify="true"` for `app.financasdacasa.com.br/invite/*`. For App Links verification to work on Play Store installs:

- Host `/.well-known/assetlinks.json` on `app.financasdacasa.com.br`
- Must contain the SHA-256 fingerprint of the **Play-managed signing certificate** (not the upload key)
- The Play-managed certificate fingerprint is available in Play Console after first `.aab` upload
- Without this, deep links fall back to a disambiguation dialog instead of opening the app directly

## 5. Google Play Billing

### Products (to create in Play Console)

- `financas_monthly` - monthly subscription
- `financas_annual` - annual subscription

Pricing to match the web Mercado Pago plans.

### App Integration

- Verify billing library is integrated and purchase flow is implemented
- Billing only works on apps installed via Play Store (not debug/sideload)
- Internal Testing track is essential for testing real purchases

### Purchase Acknowledgment

- Google Play automatically refunds purchases not acknowledged within 3 days
- Either the client (`BillingClient.acknowledgePurchase`) or server must acknowledge
- Verify that `BillingManager` calls `acknowledgePurchase` after successful purchase; if not, add it

### Server-Side Validation

- Backend needs endpoint to validate Google Play purchase receipts
- Prevents subscription fraud

## 6. Play Store Listing

### Texts (PT-BR)

- **Title**: "Finanças da Casa" (22 chars, max 30)
- **Short description**: ~80 chars summary of the app
- **Long description**: Feature details up to 4000 chars

### Required Graphics

- **App icon**: 512x512 PNG (reuse `logo-512.png` from landing page)
- **Feature graphic**: 1024x500 PNG (create based on landing `og-image.png`)
- **Screenshots**: 4-8 screenshots from the Android app running on emulator

### Privacy Policy (hard prerequisite for Phase 1)

- Public URL required (e.g., `https://app.financasdacasa.com.br/privacidade`)
- Must cover: data collected, usage, third-party sharing, account deletion
- Play Console will not allow submission (even Internal Testing) without this URL if the app requests Camera permission or accesses user data
- Must be live and accessible before first `.aab` upload

### Categorization

- Category: Finance
- Content rating: fill questionnaire in Play Console (expected: "Everyone")
- Type: App (not game)

## 7. Release Strategy

### Version Management

- `versionCode` must be monotonically increasing for every Play Store upload
- Bump `versionCode` in `app/build.gradle.kts` before each upload
- Follow semver for `versionName` (currently `1.0.0`)

### Pre-Upload Verification

- Run `./gradlew bundleRelease` and install on a physical device to verify: no R8 crashes, correct API URL, non-billing flows work
- This catches obfuscation issues before wasting iterations on Play Console uploads

### Phase 1: Internal Testing

- Create app in Play Console, upload `.aab`
- Add up to 100 testers by email
- Testers install via Play Store link
- Validate: Google Sign-In, Play Billing (test purchases), full app flow
- Near-immediate publishing (no full review)

### Phase 2: Closed Testing (optional)

- Expand to more testers if needed
- Google performs review at this stage

### Phase 3: Production

- Promote validated build to production
- Full Google review (1-7 days for first submission)
- Gradual rollout: 20% → 50% → 100%

### Manual Steps in Play Console (not automatable)

1. Create developer account (US$25 one-time fee)
2. Create app and fill listing information
3. Complete content rating questionnaire
4. Configure pricing and distribution
5. Create subscription products (monthly/annual)
6. Upload `.aab` and submit for review

## Scope Boundaries

### In scope (automatable - what we'll implement)

- Release signing configuration in build.gradle.kts
- ProGuard/R8 rules file
- AndroidManifest fixes (cleartext, backup, permissions)
- Production API URL and build config
- Network security config XML
- Data extraction rules XML
- `.gitignore` updates for keystore files
- Play Store description texts (draft)

### Out of scope (manual steps - guided with instructions)

- Google Play Console account creation and app setup
- Keystore generation (security-sensitive, done locally)
- Google Cloud Console OAuth credential creation
- Play Console subscription product creation
- Screenshot capture
- Feature graphic creation
- Privacy policy page creation and hosting
- Upload and submission to Play Store
- Digital Asset Links (`assetlinks.json`) hosting (requires Play-managed certificate fingerprint)
