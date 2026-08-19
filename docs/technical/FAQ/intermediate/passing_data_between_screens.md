# Passing Data Between Screens

## Purpose

Learn multiple approaches to pass data between screens in Jetpack Compose navigation, from simple primitives to complex objects.

---

## Overview

There are 5 main approaches for passing data between screens:

1. **Route Arguments** - For primitives (String, Int, Boolean, etc.)
2. **Type-Safe Navigation** - Modern approach with @Serializable (Navigation 2.8.0+)
3. **SharedViewModel** - For complex/persistent data
4. **JSON Serialization** - For complex objects
5. **BackStackEntry** - For transient data

---

## Method 1: Route Arguments (Primitives)

### Simple String Argument

```kotlin
// Define route with placeholder (See SamplesDestinations.PASSING_DATA_DETAILS_ROUTE)
composable(
    route = SamplesDestinations.PASSING_DATA_DETAILS_ROUTE, // "passing_data/details/{itemId}"
    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
    PassingDataDetailsScreen(itemId = itemId, onBackClick = { navController.popBackStack() })
}

// Navigate with argument
navController.navigate("passing_data/details/$itemId")
```

### Multiple Arguments

```kotlin
// Route definition (See SamplesDestinations.PASSING_DATA_PROFILE_ROUTE)
composable(
    route = SamplesDestinations.PASSING_DATA_PROFILE_ROUTE, // "passing_data/profile/{userId}/{userName}"
    arguments = listOf(
        navArgument("userId") { type = NavType.StringType },
        navArgument("userName") { type = NavType.StringType }
    )
) { backStackEntry ->
    val userId = backStackEntry.arguments?.getString("userId") ?: ""
    val userName = backStackEntry.arguments?.getString("userName") ?: ""
    PassingDataProfileScreen(userId, userName, onBackClick = { navController.popBackStack() })
}

// Navigate
navController.navigate("passing_data/profile/123/John")
```


### Optional Arguments

```kotlin
composable(
    route = "search?query={query}",
    arguments = listOf(
        navArgument("query") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
) { backStackEntry ->
    val query = backStackEntry.arguments?.getString("query")
    SearchScreen(query)
}

// Navigate with optional
navController.navigate("search?query=kotlin")
navController.navigate("search") // query is null
```

---

## Method 2: Type-Safe Navigation (Modern Approach)

### Navigation 2.8.0+ with @Serializable

```kotlin
// 1. Define destinations as serializable classes
@Serializable
object Home

@Serializable
data class Profile(
    val userId: String,
    val userName: String
)

@Serializable
data class Details(
    val id: Int,
    val title: String
)

// 2. Create NavHost
NavHost(
    navController = navController,
    startDestination = Home
) {
    composable<Home> {
        HomeScreen(
            onNavigateToProfile = { userId, userName ->
                navController.navigate(Profile(userId, userName))
            }
        )
    }

    composable<Profile> { backStackEntry ->
        val profile = backStackEntry.toRoute<Profile>()
        ProfileScreen(
            userId = profile.userId,
            userName = profile.userName
        )
    }

    composable<Details> { backStackEntry ->
        val details = backStackEntry.toRoute<Details>()
        DetailsScreen(
            id = details.id,
            title = details.title
        )
    }
}
```

### Benefits of Type-Safe Navigation

- Compile-time safety
- No string manipulation
- Auto-serialization
- IDE autocomplete
- Refactoring support

---

## Method 3: SharedViewModel

For complex or persistent data shared between multiple screens:

```kotlin
// Shared ViewModel
class SharedDataViewModel : ViewModel() {
    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem.asStateFlow()

    fun selectItem(item: Item) {
        _selectedItem.value = item
    }
}

// Screen A - Set data
@Composable
fun ListScreen(
    navController: NavController,
    sharedViewModel: SharedDataViewModel = hiltViewModel()
) {
    Button(onClick = {
        sharedViewModel.selectItem(selectedItem)
        navController.navigate("details")
    }) {
        Text("View Details")
    }
}

// Screen B - Get data
@Composable
fun DetailsScreen(
    sharedViewModel: SharedDataViewModel = hiltViewModel()
) {
    val item by sharedViewModel.selectedItem.collectAsState()

    item?.let {
        Text("Item: ${it.name}")
    }
}
```

### When to Use SharedViewModel

- Data persists across multiple screens
- Complex objects
- Data needs to survive configuration changes
- State management across navigation

---

## Method 4: JSON Serialization

For complex objects that can't be passed directly:

```kotlin
// 1. Data class
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val profile: Profile
)

// 2. Convert to JSON
val userJson = Json.encodeToString(user)
val encodedJson = URLEncoder.encode(userJson, "UTF-8")
navController.navigate("details/$encodedJson")

// 3. Decode in destination
composable("details/{userJson}") { backStackEntry ->
    val userJson = backStackEntry.arguments?.getString("userJson")
    val decodedJson = URLDecoder.decode(userJson, "UTF-8")
    val user = Json.decodeFromString<User>(decodedJson)
    DetailsScreen(user)
}
```

### Limitations

- URL length limits
- Performance overhead
- Not recommended for large objects
- Use SharedViewModel instead

---

## Method 5: BackStackEntry (Transient Data)

For temporary data that doesn't need to persist:

```kotlin
// Set data in previous entry
navController.previousBackStackEntry
    ?.savedStateHandle
    ?.set("result", result)

navController.popBackStack()

// Get data in previous screen
val result = navController.currentBackStackEntry
    ?.savedStateHandle
    ?.get<String>("result")
```

---

## Comparison Table

| Method | Best For | Pros | Cons |
|--------|----------|------|------|
| Route Arguments | Primitives, IDs | Simple, URL-friendly | Primitives only |
| Type-Safe Navigation | Modern apps | Type safety, clean | Requires Navigation 2.8.0+ |
| SharedViewModel | Complex data | Flexible, survives rotation | More setup |
| JSON Serialization | Medium objects | Works with complex types | URL limits, overhead |
| BackStackEntry | Transient data | Simple for results | Not for forward nav |

---

## Practical Examples

### Example 1: User Profile Flow

```kotlin
// Type-safe approach (recommended)
@Serializable
data class UserProfile(
    val userId: String,
    val isEditMode: Boolean = false
)

// Navigate
navController.navigate(
    UserProfile(userId = "123", isEditMode = true)
)

// Receive
composable<UserProfile> { backStackEntry ->
    val profile = backStackEntry.toRoute<UserProfile>()
    UserProfileScreen(
        userId = profile.userId,
        isEditMode = profile.isEditMode
    )
}
```

### Example 2: List to Details

```kotlin
// List Screen
LazyColumn {
    items(items, key = { it.id }) { item ->
        ItemCard(
            item = item,
            onClick = {
                // Simple ID only
                navController.navigate("details/${item.id}")

                // Or use SharedViewModel for full object
                sharedViewModel.setSelectedItem(item)
                navController.navigate("details")
            }
        )
    }
}

// Details Screen
composable("details/{itemId}") { backStackEntry ->
    val itemId = backStackEntry.arguments?.getString("itemId")

    // Option 1: Fetch from repository
    val item by viewModel.getItem(itemId).collectAsState()

    // Option 2: Get from SharedViewModel
    val item by sharedViewModel.selectedItem.collectAsState()

    ItemDetailsScreen(item)
}
```

### Example 3: Filter Results

```kotlin
// Filter screen returns result
@Composable
fun FilterScreen(
    onApply: (FilterOptions) -> Unit,
    navController: NavController
) {
    Button(onClick = {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("filters", filterOptions)
        navController.popBackStack()
    }) {
        Text("Apply")
    }
}

// Main screen receives result
LaunchedEffect(navController) {
    val filters = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<FilterOptions>("filters")

    filters?.let {
        viewModel.applyFilters(it)
    }
}
```

---

## Best Practices

1. **Use Type-Safe Navigation** for new projects (Navigation 2.8.0+)
2. **Route Arguments** for simple IDs and primitives
3. **SharedViewModel** for complex objects or shared state
4. **Avoid JSON** for large objects (use SharedViewModel)
5. **BackStackEntry** for results from child screens
6. **Always validate** arguments (they can be null)
7. **Use default values** for optional arguments

---

## Common Mistakes

### Mistake 1: Passing Large Objects in URL

```kotlin
// BAD - URL too long
val userJson = Json.encodeToString(largeUser)
navController.navigate("details/$userJson")

// GOOD - Use SharedViewModel
sharedViewModel.setUser(largeUser)
navController.navigate("details")
```

### Mistake 2: Not Handling Null Arguments

```kotlin
// BAD - Crashes if null
val userId = backStackEntry.arguments?.getString("userId")!!

// GOOD - Safe handling
val userId = backStackEntry.arguments?.getString("userId") ?: return
```

### Mistake 3: Using Wrong Type

```kotlin
// BAD - Type mismatch
navArgument("count") { type = NavType.StringType }
val count = backStackEntry.arguments?.getInt("count") // Wrong!

// GOOD - Matching types
navArgument("count") { type = NavType.IntType }
val count = backStackEntry.arguments?.getInt("count")
```

---

## Migration Guide: String Routes → Type-Safe

### Before (String-based)

```kotlin
composable("profile/{userId}") { backStackEntry ->
    val userId = backStackEntry.arguments?.getString("userId")
    ProfileScreen(userId)
}

navController.navigate("profile/123")
```

### After (Type-safe)

```kotlin
@Serializable
data class Profile(val userId: String)

composable<Profile> { backStackEntry ->
    val profile = backStackEntry.toRoute<Profile>()
    ProfileScreen(profile.userId)
}

navController.navigate(Profile(userId = "123"))
```

---

## Setup for Type-Safe Navigation

### 1. Update Dependencies

```gradle
dependencies {
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
```

### 2. Apply Plugin

```gradle
plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}
```

---

## Common Questions

**Q: Which method should I use?**
A: Type-Safe Navigation for new projects, Route Arguments for simple cases, SharedViewModel for complex data.

**Q: Can I pass a list through navigation?**
A: Yes, with Type-Safe Navigation or JSON serialization, but consider SharedViewModel for large lists.

**Q: What's the URL size limit?**
A: ~2000 characters, but varies by device. Use SharedViewModel for large data.

**Q: Does Type-Safe Navigation work with Hilt?**
A: Yes, use hiltViewModel() in composable functions as usual.

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`PassingDataExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/PassingDataExampleScreen.kt)
- **Sample ViewModel (Shared State & Args)**: [`PassingDataExampleViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/PassingDataExampleViewModel.kt)
- **App Main Navigation Graph & Routes (`MainDestinations`)**: [`NavGraph.kt:L40`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/NavGraph.kt#L40)
- **Samples Sub-Navigation Graph & Routes (`SamplesDestinations`)**: [`SamplesNavGraph.kt:L25`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesNavGraph.kt#L25)


