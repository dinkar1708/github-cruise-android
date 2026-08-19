# Jetpack Compose - State & Recomposition

## Purpose

Understanding state and recomposition is fundamental to building reactive UIs in Jetpack Compose.

---

## What is State?

State is any value that can change over time. When state changes, Compose automatically updates (recomposes) the UI.

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }

    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

---

## Recomposition

When state changes, Compose re-executes composables to update the UI.

```kotlin
@Composable
fun Greeting(name: String) {
    Text("Hello $name")  // Recomposes when name changes
}
```

---

## State Hoisting

Move state up to make composables stateless and reusable:

### Before (Stateful)
```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }

    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

### After (Stateless)
```kotlin
@Composable
fun Counter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}

// Usage
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }
    Counter(count = count, onIncrement = { count++ })
}
```

---

## remember

Preserve state across recompositions:

```kotlin
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }

    TextField(
        value = query,
        onValueChange = { query = it }
    )
}
```

**Without remember**:
```kotlin
// BAD - state lost on recomposition
var query = ""  // Resets to "" every recomposition
```

---

## State Types

### mutableStateOf
```kotlin
var count by remember { mutableStateOf(0) }
count++  // Triggers recomposition
```

### mutableStateListOf
```kotlin
val items = remember { mutableStateListOf<String>() }
items.add("New")  // Triggers recomposition
```

### mutableStateMapOf
```kotlin
val map = remember { mutableStateMapOf<String, Int>() }
map["key"] = 10  // Triggers recomposition
```

---

## ViewModel State

For state that survives configuration changes:

```kotlin
class MainViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun increment() {
        _count.value++
    }
}

@Composable
fun CounterScreen(viewModel: MainViewModel = hiltViewModel()) {
    val count by viewModel.count.collectAsState()

    Button(onClick = { viewModel.increment() }) {
        Text("Count: $count")
    }
}
```

---

## Recomposition Scope

Only changed composables recompose:

```kotlin
@Composable
fun Screen() {
    var count by remember { mutableStateOf(0) }

    Column {
        Text("Static")  // Does NOT recompose
        Text("Count: $count")  // Recomposes when count changes
        Button(onClick = { count++ }) { Text("Add") }
    }
}
```

---

## Recomposition Optimization

### Use key for stable identity
```kotlin
LazyColumn {
    items(
        items = users,
        key = { user -> user.id }  // Stable key
    ) { user ->
        UserItem(user)
    }
}
```

### Avoid creating objects in composition
```kotlin
// BAD
@Composable
fun MyComposable() {
    val data = Data()  // New object every recomposition
}

// GOOD
@Composable
fun MyComposable() {
    val data = remember { Data() }  // Preserved across recompositions
}
```

---

## Practical Examples

### Example 1: Form State
```kotlin
@Composable
fun LoginForm() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Button(onClick = { login(email, password) }) {
            Text("Login")
        }
    }
}
```

### Example 2: List State
```kotlin
@Composable
fun TodoList() {
    val todos = remember { mutableStateListOf<String>() }
    var input by remember { mutableStateOf("") }

    Column {
        TextField(
            value = input,
            onValueChange = { input = it }
        )
        Button(onClick = {
            todos.add(input)
            input = ""
        }) {
            Text("Add")
        }
        LazyColumn {
            items(todos) { todo ->
                Text(todo)
            }
        }
    }
}
```

### Example 3: Toggle State
```kotlin
@Composable
fun ToggleSwitch() {
    var isChecked by remember { mutableStateOf(false) }

    Switch(
        checked = isChecked,
        onCheckedChange = { isChecked = it }
    )
}
```

---

## Common Questions

**Q: When does recomposition happen?**
A: When state changes that a composable reads.

**Q: Do all composables recompose when state changes?**
A: No, only composables that read the changed state.

**Q: What's the difference between remember and rememberSaveable?**
A: remember survives recomposition, rememberSaveable also survives configuration changes.

**Q: How to prevent unnecessary recomposition?**
A: Use stable keys, avoid creating objects in composition, use derivedStateOf.

---

## Best Practices

1. Hoist state to lowest common ancestor
2. Use remember for UI state
3. Use ViewModel for business logic state
4. Keep composables stateless when possible
5. Use stable keys in lists

---

## Console Logs

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Log.d("Compose", "Counter recomposed with count: $count")

    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// Output:
// Counter recomposed with count: 0
// Counter recomposed with count: 1
// Counter recomposed with count: 2
```

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`StateRecompositionExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/StateRecompositionExampleScreen.kt)
- **Stateful & Stateless Component Example**: [`FavoriteButton.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/widgets/FavoriteButton.kt)
- **Feature ViewModel State**: [`RepositorySearchViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repositorysearch/RepositorySearchViewModel.kt)

