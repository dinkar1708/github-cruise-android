# Training Samples

Interactive examples for Android training covering Kotlin, Jetpack Compose, and Android architecture patterns.

## Structure

```
samples/
├── SamplesListScreen.kt          # Navigation hub
├── SamplesNavGraph.kt            # Navigation graph
├── beginner/
│   ├── NullSafetyExampleScreen.kt
│   ├── StateRecompositionExampleScreen.kt
│   ├── DataClassesExampleScreen.kt
│   ├── SealedClassesExampleScreen.kt
│   ├── CoroutinesExampleViewModel.kt
│   └── CoroutinesExampleScreen.kt
└── intermediate/
    ├── LaunchedEffectExampleScreen.kt
    ├── ViewModelFlowExampleViewModel.kt
    ├── ViewModelFlowExampleScreen.kt
    └── HiltDIExampleScreen.kt
```

## Integration

### Step 1: Add to NavGraph

Add the samples navigation graph to your main `NavGraph.kt`:

```kotlin
NavHost(
    navController = navController,
    startDestination = SPLASH_SCREEN_ROUTE
) {
    // Existing routes...
    composable(HOME_SCREEN_ROUTE) { HomeScreen(...) }

    // Add samples navigation
    samplesNavGraph(navController)
}
```

### Step 2: Navigate from anywhere

From any screen, navigate to samples:

```kotlin
Button(onClick = {
    navController.navigate(SamplesDestinations.SAMPLES_LIST_ROUTE)
}) {
    Text("Training Examples")
}
```

### Step 3: Add to HomeScreen or Settings

Example adding to HomeScreen:

```kotlin
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(SamplesDestinations.SAMPLES_LIST_ROUTE)
                }
            ) {
                Icon(Icons.Default.School, "Training")
            }
        }
    ) {
        // ...
    }
}
```

Or add to Settings screen as a menu item.

## Examples Included

### Beginner (5)

1. **Null Safety** - Nullable types (?), safe call (?.), Elvis (?:), !!, let, lateinit
2. **State & Recomposition** - remember, state hoisting, rememberSaveable
3. **Data Classes** - Auto-generated functions, copy(), destructuring
4. **Sealed Classes** - When expressions, state modeling, navigation events
5. **Coroutines Basics** - launch, async, viewModelScope, Dispatchers

### Intermediate (4)

1. **LaunchedEffect** - Side effects, keys, timer, debounce
2. **ViewModel & Flow** - StateFlow, SharedFlow, Channel, lifecycle
3. **Hilt DI** - @HiltViewModel, modules, @Provides, @Binds
4. **Flow Types** - Cold vs Hot flows, stateIn, shareIn

## Features

- All examples are interactive
- Timber logging for console output
- Clean, professional UI
- No symbols/emojis
- References to actual project code

## Usage

All examples include:
- Live demonstrations
- Code snippets
- Timber logs (check Logcat)
- Real-world patterns

## Documentation

See `docs/technical/FAQ/` for detailed markdown documentation for each topic.

## Adding New Examples

1. Create screen in `beginner/` or `intermediate/`
2. Add route to `SamplesDestinations`
3. Add composable to `samplesNavGraph()`
4. Add item to `SamplesListScreen` samples list
5. Create corresponding MD doc in `docs/technical/FAQ/`

## Console Logs

All examples use Timber for logging. Filter Logcat by:
- Tag: Sample screen name
- Example: `NullSafetyExampleScreen`

## Notes

- Examples demonstrate project-standard code patterns
- All ViewModels use Hilt injection
- StateFlow for state management
- Proper error handling
- Lifecycle awareness
