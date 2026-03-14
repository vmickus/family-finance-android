# CLAUDE.md

Android client for Financas da Casa -- a family finance manager built with Kotlin, Jetpack Compose, and Material 3.

## Build

```bash
export JAVA_HOME=/home/mickus/.sdkman/candidates/java/17.0.13-tem
./gradlew assembleDebug
```

Min SDK 26, target/compile SDK 35. Package: `com.financasdacasa.app`.

## Architecture

MVVM with unidirectional data flow:

```
Compose Screen -> ViewModel (StateFlow) -> Repository -> Retrofit API
```

- **Screens**: `ui/screens/<feature>/` -- composable functions + ViewModel per feature
- **ViewModels**: `@HiltViewModel`, expose `StateFlow<UiState>`, use `viewModelScope.launch`
- **Repositories**: `data/repository/` -- `@Singleton`, injected with API interface, suspend functions only
- **API interfaces**: `data/api/` -- Retrofit interfaces
- **DI**: `di/NetworkModule.kt` -- provides Moshi, OkHttpClient, Retrofit, and all API instances
- **Navigation**: `ui/navigation/AppNavGraph.kt` -- Compose Navigation with `Routes` object constants
- **Session**: `data/local/SessionManager.kt` -- EncryptedSharedPreferences for tokens and house selection
- **Theme**: `ui/theme/` -- teal primary color scheme (light + dark), Material 3

## Code conventions

- **DI**: Hilt everywhere. `@HiltViewModel` for ViewModels, `@Singleton @Inject constructor` for repositories
- **JSON**: Moshi with KSP codegen (`@JsonClass(generateAdapter = true)`). Never reflection-based adapters
- **State**: `MutableStateFlow` + `.asStateFlow()` for UI state. Data class per screen (e.g., `HomeUiState`)
- **Networking**: OkHttp with `AuthInterceptor` (token) + `ResponseInterceptor`. Logging in debug only
- **API base URL**: `BuildConfig.API_BASE_URL` (default `http://10.0.2.2:8080/api` for emulator)
- **Icons**: Lucide icons library (`compose-lucide`)
- **Charts**: Vico (`vico-compose-m3`)
- **Image loading**: Coil 3 with Compose integration
- **Drag reorder**: `sh.calvin.reorderable` library

## Commit conventions

- Conventional Commits format: `feat:`, `fix:`, `style:`, `chore:`, etc.
- Single-line subject only, no body, no Co-Authored-By
- Max 80 characters, in English

## Rules

- Never use `LiveData` -- always `StateFlow`
- Never use Moshi reflection (`moshi-kotlin`). Only KSP codegen (`moshi-kotlin-codegen` via `ksp()`)
- All network calls go through a Repository class -- ViewModels never call API interfaces directly
- Screens are composable functions in `ui/screens/<feature>/`
- UiState is a data class co-located with its ViewModel
- Error strings in UiState are keys (e.g., `"LOAD_FAILED"`), not user-facing text
- Debounce search inputs (300ms delay in coroutine)
