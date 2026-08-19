# NavHost & NavController Architecture Under the Hood

This document explains how **`NavController`**, **`NavHost`**, and the **Jetpack Compose Navigation BackStack** work under the hood from initial launch to screen transitions.

---

## 1. The 4 Core Navigation Building Blocks

```
┌────────────────────────────────────────────────────────────────────────┐
│ NavHostController (rememberNavController())                            │
│ ├── Manages the Navigation BackStack                                  │
│ └── Coordinates push (navigate) and pop (popBackStack) events          │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│ NavHost (NavHost(navController, startDestination))                    │
│ ├── Container Composable that observes the current BackStack entry    │
│ └── Swaps & animates Composable screens based on the current route     │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│ NavGraph (NavGraphBuilder.composable)                                  │
│ └── Map of Route Definitions ──► Composable destinations               │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│ NavBackStackEntry                                                      │
│ ├── Represents a single screen instance on the stack                   │
│ ├── Holds Route Arguments (`SavedStateHandle`)                        │
│ └── Holds dedicated `ViewModelStore` & `LifecycleOwner`                │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Initial Launch: How the First Composable is Pushed

When the app starts in [`MainActivity.kt`](../../app/src/main/java/com/jetpack/compose/github/github/cruise/MainActivity.kt):

```kotlin
setContent {
    val navController = rememberNavController()
    NavGraph(navController = navController)
}
```

And in [`NavGraph.kt`](../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/NavGraph.kt):

```kotlin
NavHost(
    navController = navController,
    startDestination = MainDestinations.HOME_SCREEN_ROUTE
) {
    composable(MainDestinations.HOME_SCREEN_ROUTE) {
        HomeScreen(navController = navController)
    }
    // other destinations...
}
```

### What Happens Step-by-Step Under the Hood:

```
1. rememberNavController()
   └── Allocates NavHostController and binds it to the Activity lifecycle.

2. NavHost receives startDestination ("home_screen")
   └── Looks up "home_screen" in the NavGraphBuilder definitions.

3. Initial NavBackStackEntry is Created
   └── NavController creates a new NavBackStackEntry for "home_screen".
   └── Attaches a new ViewModelStoreOwner and SavedStateRegistryOwner.

4. Pushed to BackStack
   └── NavController pushes this entry onto its internal MutableStateFlow<List<NavBackStackEntry>>.

5. NavHost Recomposes & Renders
   └── NavHost collects the latest backstack entry.
   └── Invokes HomeScreen(navController) inside the Compose layout tree.
```

---

## 3. Pushing a New Screen (`navController.navigate()`)

When a user taps an item to open a details screen:

```kotlin
navController.navigate("user_repo_screen/octocat")
```

### Lifecycle & BackStack Pipeline:

```
[User Action] ──► navController.navigate("user_repo_screen/octocat")
                        │
                        ▼
             1. Route Matching & Argument Parsing
                └── Matches "user_repo_screen/{login}" pattern
                └── Extracts argument: login = "octocat"
                        │
                        ▼
             2. Create New NavBackStackEntry
                └── New unique Entry ID generated
                └── Bundles arguments into SavedStateHandle
                └── Allocates dedicated ViewModelStore
                        │
                        ▼
             3. Push to BackStack
                └── BackStack: [HomeEntry] ──► [HomeEntry, UserRepoEntry]
                        │
                        ▼
             4. Screen Transition Animation
                └── Home Composable moves to STOPPED lifecycle state
                └── UserRepoScreen Composable is composed and MOUNTED (RESUMED)
```

---

## 4. Popping a Screen (`navController.popBackStack()`)

When the user presses the top-bar Back button or the system back gesture:

```kotlin
navController.popBackStack()
```

```
[Back Press] ──► navController.popBackStack()
                        │
                        ▼
             1. Pop Top Entry
                └── BackStack: [HomeEntry, UserRepoEntry] ──► [HomeEntry]
                        │
                        ▼
             2. Resource & Memory Cleanup
                └── UserRepoScreen's ViewModelStore is CLEARED
                └── Calls onCleared() on all ViewModels scoped to UserRepoScreen
                └── Cancels all active viewModelScope coroutines
                        │
                        ▼
             3. Restore Previous Screen
                └── HomeEntry becomes the top entry again (RESUMED)
                └── Compose restores scroll position and rememberSaveable states
```

---

## 5. BackStack Navigation Flags (`NavOptionsBuilder`)

When navigating, you can customize how the backstack is managed:

| Option | Code | Behavior | Use Case |
| :--- | :--- | :--- | :--- |
| **`launchSingleTop`** | `launchSingleTop = true` | Prevents pushing duplicate copies if already on top. | Tab switching, search queries. |
| **`popUpTo`** | `popUpTo("home") { inclusive = false }` | Pops all entries up to "home" before navigating. | Login ➔ Home flow (clearing login screen). |
| **`saveState` / `restoreState`** | `saveState = true; restoreState = true` | Saves & restores scroll and UI state of popped screens. | Bottom navigation bar tabs. |

---

## 6. How ViewModels Are Scoped to BackStack Entries

When you call `hiltViewModel()` inside a Composable:

```kotlin
@Composable
fun UserRepoScreen(
    viewModel: UserRepoScreenViewModel = hiltViewModel()
)
```

1. Hilt looks at `LocalViewModelStoreOwner.current`, which provides the **current `NavBackStackEntry`**.
2. If `UserRepoScreenViewModel` already exists for this entry, it is returned.
3. If not, Hilt instantiates it and stores it inside that `NavBackStackEntry`'s `ViewModelStore`.
4. When the user navigates back and the entry is popped, the entire `ViewModelStore` is destroyed automatically, preventing memory leaks!

---

## Code Reference in Our Project

- **Main Navigation Host & Graph**: [`NavGraph.kt:L40-L135`](../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/NavGraph.kt#L40-L135)
- **Nested Feature Sub-Graph**: [`SamplesNavGraph.kt:L25-L160`](../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesNavGraph.kt#L25-L160)
- **Activity Host Setup**: [`MainActivity.kt`](../../app/src/main/java/com/jetpack/compose/github/github/cruise/MainActivity.kt)
- **Navigation Type Safety FAQ**: [type_safe_navigation.md](./FAQ/intermediate/type_safe_navigation.md)
- **Passing Data Between Screens FAQ**: [passing_data_between_screens.md](./FAQ/intermediate/passing_data_between_screens.md)
