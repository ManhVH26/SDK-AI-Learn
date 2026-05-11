# Prompt — Build Android base project

> Paste the section below as-is into Claude / any AI assistant inside an empty Android Studio project (Compose template, single `:app` module already created). Replace the two placeholders before pasting.

---

## Inputs (fill in before sending)

- `<APP_NAMESPACE>` — reverse-DNS, e.g. `com.example.myapp`
- `<APP_NAME>` — PascalCase, used for `<APP_NAME>Application` class, e.g. `MyApp`

---

## Prompt to paste

Build the base for this Android project following these exact requirements. Do not invent extra features. Do not create documentation files. After every change, run `./gradlew assembleDebug` to verify; fix until BUILD SUCCESSFUL.

### 1. Stack & versions

Use a `gradle/libs.versions.toml` version catalog with at least:

- Kotlin **2.1.21+** (Kotlin 2.0.x cannot parse JDK 25 — required if dev machine runs JDK 25)
- AGP 8.x (whatever the template ships with)
- Jetpack Compose via BOM, Material3
- `androidx.navigation:navigation-compose` **2.8.4+** (type-safe routes via `kotlinx.serialization`)
- `org.jetbrains.kotlinx:kotlinx-serialization-json` 1.7.x
- `org.jetbrains.kotlinx:kotlinx-coroutines-core` + `-android` 1.9.x
- `androidx.lifecycle:lifecycle-viewmodel-compose` + `lifecycle-runtime-compose` 2.8.x
- Koin **4.0.0**: `koin-core`, `koin-core-coroutines` (REQUIRED for `lazyModule`/`lazyModules`), `koin-android`, `koin-androidx-compose`, `koin-androidx-compose-navigation`

Plugins to enable: `kotlin-android`, `kotlin-compose`, `kotlin-serialization`. Enable `buildFeatures { compose = true; buildConfig = true }`.

### 2. Module structure (single `:app` module)

```
<APP_NAMESPACE>/
├── <APP_NAME>Application.kt
├── MainActivity.kt
├── core/
│   ├── common/      Result.kt, DispatcherProvider.kt
│   └── di/          EagerModule.kt, LazyModule.kt
├── data/repository/                     <Feature>RepositoryImpl
├── domain/
│   ├── model/
│   ├── repository/                      <Feature>Repository (interface)
│   └── usecase/                         UseCase.kt, <X>UseCase
├── designsystem/
│   ├── theme/       AppColors, AppTypography, AppShapes, AppSpacing, AppTheme
│   └── component/   AppButton, AppText, AppLoading
└── presentation/
    ├── base/        MviContract.kt (UiState/UiEvent/UiEffect), BaseViewModel.kt, EffectCollector.kt
    ├── navigation/  AppNavHost.kt
    └── feature/<x>/ <X>Contract.kt, <X>ViewModel.kt, <X>Screen.kt, <X>Navigation.kt
```

Set `namespace` and `applicationId` in `app/build.gradle.kts` to `<APP_NAMESPACE>`.

### 3. Architecture

- **Clean Architecture**: `data` depends on `domain`, `presentation` depends on `domain`. `domain` is framework-free.
- **MVI** for presentation: state/event/effect contract per feature.
- **Single module** (no multi-module split).

### 4. Design System (custom, bridged to Material3)

Each design token is an `@Immutable data class` exposed via `staticCompositionLocalOf`. `AppTheme {}` provides them and constructs a `MaterialTheme` from them so Material components keep working.

- `AppColors` (primary/onPrimary/secondary/onSecondary/background/onBackground/surface/onSurface/surfaceVariant/onSurfaceVariant/outline/error/onError/success/warning/isLight) — provide `LightAppColors` and `DarkAppColors`.
- `AppTypography` (display Large/Medium, headline Large/Medium, title Large/Medium, body Large/Medium/Small, label Large/Medium/Small).
- `AppShapes` (none/small/medium/large/extraLarge/pill) — fields typed as `CornerBasedShape` (Material3 `Shapes()` requires it).
- `AppSpacing` (none/xxs/xs/s/m/l/xl/xxl/xxxl, in `Dp`).
- `AppTheme` exposes `AppTheme.colors / typography / shapes / spacing` via `@Composable @ReadOnlyComposable` getters.

Components (minimal): `AppButton(text, onClick, style: Primary|Secondary|Text, enabled, loading)`, `AppText`, `AppLoading`.

### 5. MVI base

```kotlin
interface UiState; interface UiEvent; interface UiEffect

abstract class BaseViewModel<S : UiState, E : UiEvent, F : UiEffect>(initialState: S) : ViewModel() {
    // state: MutableStateFlow exposed as StateFlow<S>
    // event: MutableSharedFlow(extraBufferCapacity=64, DROP_OLDEST) exposed as Flow<E>
    // effect: Channel(BUFFERED) exposed as Flow<F> via receiveAsFlow()
    // init { viewModelScope.launch { _event.collect { handleEvent(it) } } }
    // fun sendEvent(E); protected fun setState(reducer: S.() -> S); protected fun sendEffect(F)
    protected abstract fun handleEvent(event: E)
}
```

Provide a `CollectEffect(effect: Flow<F>, onEffect: suspend (F) -> Unit)` composable that uses `LocalLifecycleOwner` + `repeatOnLifecycle(STARTED)` + `collectLatest`.

### 6. Per-screen navigation file (mandatory pattern for every screen)

```kotlin
@Serializable
object <Route>

fun NavGraphBuilder.<route>Screen(
    onNavigateNext: () -> Unit,
    /* other nav callbacks */
) {
    composable<<Route>> {
        <Route>Screen(
            viewModel = koinViewModel(),
            onNavigateNext = onNavigateNext,
        )
    }
}
```

`AppNavHost` is the only place that knows route wiring; it calls each feature's `NavGraphBuilder.<route>Screen(...)` extension. `MainActivity` hosts `AppTheme { Scaffold { AppNavHost() } }`.

### 7. Koin DI — eager + lazy split, all in `core/di/`

Two files only:

- `EagerModule.kt` → `val eagerModule = module { ... }` — registered at `startKoin { modules(eagerModule) }`. Cross-cutting deps + features on the launch path.
- `LazyModule.kt` → `val lazyModule = lazyModule { ... }` (import `org.koin.dsl.lazyModule as koinLazyModule` to avoid name clash). Returns `Lazy<Module>`. Registered via `startKoin { lazyModules(lazyModule) }` (`org.koin.core.lazyModules` from `koin-core-coroutines`) — Koin loads it in a background coroutine.

Both modules **must** be split into the same labelled sections for scannability:

```kotlin
// ============ Core / cross-cutting ============   (eagerModule only)
// ============ Data module ============
// ============ Repository module ============
// ============ UseCase module ============
// ============ ViewModel module ============
```

Use constructor DSL: `singleOf(::Impl) bind Interface::class`, `factoryOf(::UseCase)`, `viewModelOf(::ViewModel)`. **Never** call `createdAtStart()` unless the binding must run at process launch (crash reporter SDK init etc.).

Decision rule for every new binding:
1. Cross-cutting (used everywhere)? → `eagerModule`
2. Reachable on launch path (start destination + immediate next screens)? → `eagerModule`
3. Otherwise → `lazyModule`

### 8. Application class

```kotlin
class <APP_NAME>Application : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@<APP_NAME>Application)
            modules(eagerModule)
            lazyModules(lazyModule)
        }
    }
}
```

Register `android:name=".<APP_NAME>Application"` in `AndroidManifest.xml`.

### 9. Firebase setup (optional — skip the whole section if Firebase is not needed)

Use this when the project needs Analytics / Crashlytics / Remote Config. Match versions to current stable.

**9.1. Plugins**

In `gradle/libs.versions.toml` `[plugins]` section:

```toml
google-services    = { id = "com.google.gms.google-services", version = "4.4.x" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version = "3.0.x" }
```

In project-level `build.gradle.kts`: `alias(libs.plugins.google.services) apply false` (and the Crashlytics one if used).
In `app/build.gradle.kts`: `alias(libs.plugins.google.services)` (and Crashlytics if used).

**9.2. SDKs (Firebase BoM — keeps versions in sync)**

In `libs.versions.toml`:

```toml
[versions]
firebaseBom = "33.x"
coroutinesPlayServices = "1.9.x"

[libraries]
firebase-bom         = { module = "com.google.firebase:firebase-bom", version.ref = "firebaseBom" }
firebase-analytics   = { module = "com.google.firebase:firebase-analytics" }   # version managed by BoM
firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics" }
firebase-config      = { module = "com.google.firebase:firebase-config" }
kotlinx-coroutines-play-services = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services", version.ref = "coroutinesPlayServices" }
```

In `app/build.gradle.kts` `dependencies`:

```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.analytics)
implementation(libs.firebase.crashlytics)
implementation(libs.firebase.config)
implementation(libs.kotlinx.coroutines.play.services)  // needed for `Task.await()` on RC fetch
```

**9.3. `google-services.json`**

Download from Firebase Console → place at `app/google-services.json`. For per-flavor configs use `app/src/<flavor>/google-services.json`. Add the file to `.gitignore` only if the team has a separate sharing channel; otherwise commit it (it is not a secret — the API key in it is restricted by SHA-1 + package name on the Firebase side).

**9.4. DI — `core/di/FirebaseModule.kt`**

Firebase is cross-cutting infra → goes in `eagerModule`, or split into a sibling `firebaseModule` registered alongside it.

```kotlin
val firebaseModule = module {
    single<FirebaseAnalytics> { FirebaseAnalytics.getInstance(get()) }
    single<FirebaseCrashlytics> { FirebaseCrashlytics.getInstance() }
    single<FirebaseRemoteConfig> {
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(RemoteConfigHelper.buildSettings())
            setDefaultsAsync(RemoteConfigHelper.DEFAULTS)
        }
    }
}
```

Register in `startKoin { modules(eagerModule, firebaseModule); lazyModules(lazyModule) }`.

**9.5. Remote Config helper — `data/firebase/RemoteConfigHelper.kt`** (only if Remote Config is used)

Single source of truth for keys + defaults so client/server cannot drift.

```kotlin
object RemoteConfigHelper {
    val DEFAULTS: Map<String, Any> = mapOf(/* key -> default value */)

    fun buildSettings() = remoteConfigSettings {
        minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else 3_600L
    }

    suspend fun fetchAndActivate(config: FirebaseRemoteConfig): Boolean = runCatching {
        config.fetchAndActivate().await()
    }.getOrDefault(false)  // swallow + log; callers (Splash) fire-and-forget
}
```

Rules:
- Firebase RC supports only **Boolean / Long / Double / String**. Use `Long` (suffix `L`), never `Int`.
- `DEFAULTS` map must mirror values pushed to Firebase Console — drift between the two is the most common RC bug.
- Debug fetch interval `0L` for QA immediacy; release `3_600L` balances freshness vs the 1M-fetches/day free-tier quota.

**9.6. Application init**

Firebase SDKs auto-initialize via `FirebaseInitProvider` declared by the BoM manifest merge — do **not** call `FirebaseApp.initializeApp()` manually. Just `startKoin { ... }` as in section 8 and the singletons resolve lazily on first inject.

### 10. Sample feature (Home) for verification

Create one feature `home/` (start destination) implementing the full pipeline so the base compiles and runs:

- Domain: `Greeting` model, `GreetingRepository` interface, `GetGreetingUseCase` extending `UseCase<String, Greeting>` (suspend, runs on `dispatcherProvider.io`, wraps in `Result`).
- Data: `GreetingRepositoryImpl` (returns a hardcoded greeting after 300ms `delay`).
- Presentation: `HomeContract` (state with `isLoading`, `greeting`, `errorMessage`; events `LoadGreeting`, `PrimaryClicked`; effects `ShowMessage`, `NavigateNext`), `HomeViewModel` (loads greeting in `init`), `HomeScreen` (renders state + `CollectEffect`), `HomeNavigation` (the `@Serializable object Home` + `NavGraphBuilder.homeScreen(...)`).
- DI: register `GreetingRepositoryImpl` (single, bind), `GetGreetingUseCase` (factory), `HomeViewModel` (viewModel) inside `eagerModule` under the labelled sections.

### 11. Common helpers

- `core/common/Result.kt` — sealed `Result<out T>` with `Success(data)`, `Failure(error)`, `Loading`. Provide `map`, `onSuccess`, `onFailure`, `runCatchingResult { }` (re-throws `CancellationException`).
- `core/common/DispatcherProvider.kt` — `interface DispatcherProvider { val main, io, default }` + `DefaultDispatcherProvider`.
- `domain/usecase/UseCase.kt` — `abstract class UseCase<P, R>(dispatcherProvider)`: `suspend operator fun invoke(P): Result<R> = withContext(dispatcherProvider.io) { runCatchingResult { execute(P) } }`. Plus `NoParamsUseCase<R>` extending `UseCase<Unit, R>`.

### 12. Verification

- `./gradlew assembleDebug` → BUILD SUCCESSFUL.
- App launches, shows greeting "Hello, ColorByNumber!" (or equivalent for the new app), Continue button triggers `NavigateNext` effect (no-op for now).
- If dev machine has JDK 25 installed and the build fails with `IllegalArgumentException: 25.0.2`, point Gradle at JDK 17: `org.gradle.java.home=/path/to/jdk-17` in `gradle.properties`, OR set `JAVA_HOME` to JDK 17 before running.

### 13. What NOT to do

- Do NOT split into multiple Gradle modules.
- Do NOT scatter DI files per-feature (everything DI lives in `core/di/`).
- Do NOT use Hilt — use Koin only, with `koinViewModel()` (not `hiltViewModel()`).
- Do NOT use `createdAtStart()`.
- Do NOT write `*.md` documentation files.
- Do NOT add comments that just restate code; only comment WHY (architectural intent, e.g. the eager-vs-lazy block in `EagerModule.kt`).
