# When to Use Coroutine Scopes in Android: Complete Usage Guide

## Overview

A practical decision guide on **which Coroutine Scope to use in Android**, explaining the exact lifecycle, cancellation rules, rotation behavior, and code examples for:
1. `viewModelScope` (In ViewModels)
2. `lifecycleScope` (In MainActivity / Activities)
3. `rememberCoroutineScope()` (In Jetpack Compose)
4. `GlobalScope` (Why to avoid) vs Injected `@ApplicationScope`

---

## 🔗 Code Implementation Reference

* **Interactive Demo Screen**: [`CoroutineScopesUsageScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/CoroutineScopesUsageScreen.kt)
* **Sample ViewModel**: [`CoroutineScopesUsageViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/CoroutineScopesUsageViewModel.kt)
* **Sample Navigation Route**: `SamplesDestinations.COROUTINE_SCOPES_USAGE_ROUTE`

---

## 🎯 Quick Decision Flowchart: Which Scope Should I Use?

```
Are you launching a coroutine?
│
├── 1. Inside a ViewModel (API calls, DB queries, UI business state)?
│      └── 👉 USE: `viewModelScope` (Survives rotation! Cancels on onCleared)
│
├── 2. Inside a Composable responding to a User Click (Button, Scroll, Snackbar)?
│      └── 👉 USE: `rememberCoroutineScope()` (Cancels when Composable leaves screen)
│
├── 3. Inside a Composable loading initial data or observing state?
│      └── 👉 USE: `LaunchedEffect(key) { ... }`
│
├── 4. Inside MainActivity / Fragment observing UI state flows?
│      └── 👉 USE: `lifecycleScope.launch { repeatOnLifecycle(STARTED) { ... } }`
│
└── 5. A Fire-and-Forget background task that MUST finish even if user leaves screen?
       └── 👉 USE: Custom injected `@ApplicationScope` (NEVER use `GlobalScope`!)
```

---

## 📊 Master Scope Comparison Matrix

| Coroutine Scope | Where to Use? | Bound Lifecycle | Survives Screen Rotation? | Auto-Cancelled When? | Best For |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`viewModelScope`** 🏆 | Inside `ViewModel` | ViewModel instance | ✅ **YES** | `ViewModel.onCleared()` (Screen permanently closed) | **95% of App Logic:** Fetching API data, querying Room DB, updating `StateFlow`. |
| **`lifecycleScope`** | Inside `MainActivity` / `Activity` | Host Activity | ❌ **NO** (Destroyed on rotate) | `Activity.onDestroy()` | **Activity-Level UI:** System dialogs, in-app updates, `repeatOnLifecycle` flow collections. |
| **`rememberCoroutineScope()`** | Inside Jetpack Compose Functions | Compose Composition | ❌ **NO** (Recomposed) | Composable leaves UI tree | **UI Event Handlers:** Button clicks, scrolling `LazyColumn`, showing `Snackbar`. |
| **`GlobalScope`** ⚠️ | Anywhere (Static) | Entire App Process | ✅ **YES** | App Process Kill (OS Level) | 🛑 **ANTI-PATTERN:** Bypasses Structured Concurrency, causes memory leaks. |
| **`ApplicationScope`** 🛡️ | Injected via Hilt in Repositories/Services | Application Lifetime | ✅ **YES** | App Process Kill | **Fire-and-Forget:** Analytics event logging, flushing disk cache, offline mutation sync. |

---

## 🛠️ Detailed Code Examples for Each Scope

### 1. `viewModelScope` — When & How to Use
* **Use Case:** Executing API network calls and database queries.
* **Why:** If the user rotates the phone while loading data, `viewModelScope` keeps running and does NOT restart the request!

```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: GithubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun performSearch(query: String) {
        // ✅ Runs on background, survives screen rotation, cancels if user leaves screen
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val results = repository.searchRepositories(query)
                _uiState.value = SearchUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.localizedMessage ?: "Unknown Error")
            }
        }
    }
}
```

---

### 2. `lifecycleScope` — When & How to Use
* **Use Case:** In `MainActivity` when you need to observe flows or trigger Activity-level dialogs.
* **Why:** `repeatOnLifecycle(Lifecycle.State.STARTED)` ensures that when the app is in the background (user pressed Home), flow collection is **automatically paused** to save CPU and battery!

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Pauses collection when app is in background (ON_STOP)
        // and resumes when app comes back to foreground (ON_START)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Handle Activity events
                }
            }
        }
    }
}
```

---

### 3. `rememberCoroutineScope()` — When & How to Use
* **Use Case:** Inside Jetpack Compose UI button clicks, scrolling lists, opening navigation drawers, or showing snackbars.
* **Why:** Composables are functions, not classes. `rememberCoroutineScope()` creates a scope tied directly to that Composable's position in the UI tree.

```kotlin
@Composable
fun RepositoryListScreen(
    items: List<RepositoryEntity>,
    listState: LazyListState = rememberLazyListState()
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // ✅ Launch animation coroutine from UI click
                    coroutineScope.launch {
                        listState.animateScrollToItem(0) // Smooth scroll to top
                        snackbarHostState.showSnackbar("Scrolled to top")
                    }
                }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }
    ) { padding ->
        LazyColumn(state = listState, modifier = Modifier.padding(padding)) {
            items(items, key = { it.id }) { repo ->
                RepositoryCard(repository = repo)
            }
        }
    }
}
```

#### 💡 Can I Call a ViewModel `suspend` Function from `rememberCoroutineScope()`?
**YES!** You can call any `suspend` function on your ViewModel from `rememberCoroutineScope().launch`:

```kotlin
@Composable
fun ItemDetailScreen(viewModel: ItemViewModel, itemId: String) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Button(onClick = {
        coroutineScope.launch {
            // 🌐 1. Call suspend function on ViewModel:
            val success = viewModel.deleteItemApi(itemId)
            
            // 🍞 2. Await result and immediately trigger UI feedback:
            if (success) {
                snackbarHostState.showSnackbar("Item deleted successfully!")
            }
        }
    }) {
        Text("Delete Item")
    }
}
```

#### ⚖️ Architectural Rule: `rememberCoroutineScope` vs `viewModelScope` for API Calls
* **Use `rememberCoroutineScope().launch { viewModel.suspendCall() }`:**  
  When the **UI needs to await the return value immediately** to drive UI side-effects (e.g. showing a Snackbar or animating a list).
* **Use `viewModel.callApi()` with internal `viewModelScope.launch`:**  
  For **critical mutations** (e.g. submitting a payment or posting a comment). If the user clicks submit and immediately presses Back, `rememberCoroutineScope` is cancelled instantly, while `viewModelScope` continues running in the background until the payment finishes safely!

---

### 4. `GlobalScope` vs Custom Injected `@ApplicationScope`

#### ❌ Why `GlobalScope` is Dangerous:
```kotlin
// ⚠️ ANTI-PATTERN: DO NOT USE IN PRODUCTION
GlobalScope.launch {
    // 1. Unstructured: Not bound to any Job hierarchy
    // 2. Memory Leak: If this captures an Activity context, it leaks forever!
    // 3. Testing Nightmare: Cannot be replaced with TestDispatchers in unit tests.
    database.saveLog(event)
}
```

#### ✅ The Production Solution: Inject an `@ApplicationScope` via Hilt
```kotlin
// 1. Define Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

// 2. Provide via Hilt Module
@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopesModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

// 3. Inject into Services for Fire-and-Forget tasks
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val api: AnalyticsApi
) {
    fun trackEvent(event: AnalyticsEvent) {
        // ✅ Safely executes fire-and-forget task without leaking UI references
        appScope.launch {
            api.sendEvent(event)
        }
    }
}
```

---

## 🎙️ Staff Interview Defense Script (30 Seconds)

> *"In modern Android development, we strictly enforce **Structured Concurrency**:
> 
> * **Business Logic:** We encapsulate 95% of asynchronous operations inside **`viewModelScope`**, ensuring network and database jobs survive configuration changes and cancel automatically upon `onCleared()`.
> * **UI Layer:** We isolate UI-driven animations in Compose using **`rememberCoroutineScope()`** and collect flows in Activities via **`lifecycleScope.repeatOnLifecycle`** to prevent background resource waste.
> * **Process-level Work:** We completely ban **`GlobalScope`** to prevent memory leaks, instead injecting an application-scoped `CoroutineScope(SupervisorJob() + Dispatchers.Default)` for fire-and-forget telemetry."*
