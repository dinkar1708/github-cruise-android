# Type-Safe Navigation in Jetpack Compose

## Purpose

Type-Safe Navigation (introduced in Jetpack Navigation 2.8.0) replaces error-prone string URLs and manual bundle parsing with Kotlin Serialization (`@Serializable`) data objects for compile-time safety.

---

## String Routes (Legacy) vs Type-Safe Routes (Modern)

| Feature | String-Based Routes | Type-Safe Routes (`@Serializable`) |
| :--- | :--- | :--- |
| **Route Definition** | `"user/{id}/{name}"` | `@Serializable data class UserRoute(val id: Int, val name: String)` |
| **Navigation Call** | `navController.navigate("user/123/John")` | `navController.navigate(UserRoute(id = 123, name = "John"))` |
| **Argument Parsing** | `arguments?.getString("name")` (Manual) | `backStackEntry.toRoute<UserRoute>()` (Automatic) |
| **Compile-Time Safety** | ❌ None (Runtime crash on typo) | ✅ Full compile-time verification & autocomplete |
| **Refactoring** | ❌ Manual search & replace in strings | ✅ IDE Safe Delete / Rename support |

---

## How It Works

### 1. Define Routes as `@Serializable` Objects
```kotlin
@Serializable
object HomeRoute

@Serializable
data class ProfileRoute(val userId: String, val userName: String)
```

### 2. Configure the `NavHost`
```kotlin
NavHost(navController = navController, startDestination = HomeRoute) {
    composable<HomeRoute> {
        HomeScreen(onNavigateToProfile = { id, name ->
            navController.navigate(ProfileRoute(userId = id, userName = name))
        })
    }

    composable<ProfileRoute> { backStackEntry ->
        // Automatically deserializes arguments
        val profile: ProfileRoute = backStackEntry.toRoute()
        ProfileScreen(userId = profile.userId, userName = profile.userName)
    }
}
```

### 3. Extracting Arguments in ViewModel with `SavedStateHandle`
```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Type-safe argument extraction directly in ViewModel
    private val route: ProfileRoute = savedStateHandle.toRoute()
    val userId: String = route.userId
}
```

---

## Migration & Coexistence in Our Codebase

In existing projects, string-based navigation (like `MainDestinations` in `NavGraph.kt`) and Type-Safe sub-graphs can coexist seamlessly while migrating feature by feature.

- **Current App Graph**: Uses `MainDestinations` constants in [`NavGraph.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/NavGraph.kt#L40) and [`SamplesNavGraph.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesNavGraph.kt#L25).
- **Argument Passing Examples**: See [Passing Data Between Screens](./passing_data_between_screens.md).

---

## Common Pitfalls & Junior Tips

1. **Add Kotlin Serialization Plugin**: Ensure `kotlin("plugin.serialization")` is added in `build.gradle.kts`.
2. **Keep Route Objects Lightweight**: Do not pass huge data objects (e.g. bitmaps or large lists) through navigation arguments; pass IDs and let the repository fetch the full object.
3. **Handle Nullables & Defaults**: Optional parameters should have default values in the `@Serializable` class.

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`PassingDataExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/PassingDataExampleScreen.kt)
- **App Main Navigation Graph & Routes (`MainDestinations`)**: [`NavGraph.kt:L40`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/NavGraph.kt#L40)
- **Samples Sub-Navigation Graph & Routes (`SamplesDestinations`)**: [`SamplesNavGraph.kt:L25`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesNavGraph.kt#L25)
