# Sealed Classes & When Expression

## Purpose

Sealed classes represent restricted class hierarchies, perfect for modeling states and results. When expressions provide exhaustive type checking.

---

## Sealed Classes

Restrict inheritance to a fixed set of subclasses:

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: String) : UiState()
    data class Error(val message: String) : UiState()
}
```

All subclasses must be in the same file or package.

---

## When Expression

Pattern matching with exhaustive checking:

```kotlin
fun handleState(state: UiState) {
    when (state) {
        is UiState.Loading -> showLoading()
        is UiState.Success -> showData(state.data)
        is UiState.Error -> showError(state.message)
        // No else needed - compiler knows all cases
    }
}
```

---

## Common Use Cases

### Use Case 1: UI State
```kotlin
sealed class ScreenState {
    object Initial : ScreenState()
    object Loading : ScreenState()
    data class Content(val users: List<User>) : ScreenState() 
    data class Empty(val message: String) : ScreenState()
    data class Error(val error: Throwable) : ScreenState()
}

class ViewModel : ViewModel() {
    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state
}
```

### Use Case 2: Network Result
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

suspend fun fetchUser(): Result<User> {
    return try {
        val user = api.getUser()
        Result.Success(user)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

### Use Case 3: Navigation Events
```kotlin
sealed class NavigationEvent {
    object Back : NavigationEvent()
    data class ToDetails(val id: String) : NavigationEvent()
    data class ToWeb(val url: String) : NavigationEvent()
    object ToSettings : NavigationEvent()
}
```

---

## When Expression Features

### With Return Value
```kotlin
val message = when (state) {
    is UiState.Loading -> "Loading..."
    is UiState.Success -> "Success: ${state.data}"
    is UiState.Error -> "Error: ${state.message}"
}
```

### Multiple Conditions
```kotlin
when (x) {
    0, 1 -> print("x is 0 or 1")
    in 2..10 -> print("x is between 2 and 10")
    else -> print("x is something else")
}
```

### Type Checking
```kotlin
when (obj) {
    is String -> print(obj.length)
    is Int -> print(obj * 2)
    else -> print("Unknown type")
}
```

### As Statement
```kotlin
when {
    x > 0 -> print("positive")
    x < 0 -> print("negative")
    else -> print("zero")
}
```

---

## Practical Example - Complete Flow

```kotlin
// 1. Define sealed class
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

// 2. Use in ViewModel
class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading

            val result = authRepository.login(email, password)

            _state.value = when (result) {
                is Result.Success -> LoginState.Success(result.data)
                is Result.Error -> LoginState.Error(result.exception.message ?: "Unknown error")
            }
        }
    }
}

// 3. Handle in Composable
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    when (state) {
        is LoginState.Idle -> {
            LoginForm(onLogin = { email, password ->
                viewModel.login(email, password)
            })
        }
        is LoginState.Loading -> {
            CircularProgressIndicator()
        }
        is LoginState.Success -> {
            val user = (state as LoginState.Success).user
            Text("Welcome ${user.name}")
        }
        is LoginState.Error -> {
            val message = (state as LoginState.Error).message
            ErrorView(message)
        }
    }
}
```

---

## Sealed Class vs Enum

| Feature | Sealed Class | Enum |
|---------|--------------|------|
| Data | Can hold different data | Same structure |
| Instances | Multiple instances | Fixed instances |
| Type | Different types | Same type |
| Use | Complex states | Simple constants |

### Enum Example
```kotlin
enum class Status {
    LOADING,
    SUCCESS,
    ERROR
}
```

### Sealed Class (Better)
```kotlin
sealed class Status {
    object Loading : Status()
    data class Success(val data: String) : Status()
    data class Error(val message: String) : Status()
}
```

---

## Sealed Interfaces (Kotlin 1.5+)

```kotlin
sealed interface Response
data class Success(val data: String) : Response
data class Error(val error: String) : Response
object Loading : Response
```

---

## Common Patterns

### Pattern 1: Loading/Content/Error
```kotlin
sealed class ViewState<out T> {
    object Loading : ViewState<Nothing>()
    data class Content<T>(val data: T) : ViewState<T>()
    data class Error(val throwable: Throwable) : ViewState<Nothing>()
}
```

### Pattern 2: API Response
```kotlin
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
    object NetworkError : ApiResponse<Nothing>()
    object Unauthorized : ApiResponse<Nothing>()
}
```

### Pattern 3: Form Validation
```kotlin
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}
```

---

## When Expression Tips

### Exhaustive Checking
```kotlin
// Sealed class - no else needed
val result = when (state) {
    is UiState.Loading -> "loading"
    is UiState.Success -> "success"
    is UiState.Error -> "error"
}

// Regular class - else required
val result = when (obj) {
    is String -> "string"
    is Int -> "int"
    else -> "other"  // Required
}
```

### Smart Casting
```kotlin
when (state) {
    is UiState.Success -> {
        // state is smart cast to Success
        println(state.data)  // No need to cast
    }
    is UiState.Error -> {
        // state is smart cast to Error
        println(state.message)  // No need to cast
    }
}
```

---

## Common Questions

**Q: When to use sealed class vs enum?**
A: Use sealed class when different types need different data. Use enum for simple constants.

**Q: Can sealed classes be in different files?**
A: Subclasses must be in same file or package (Kotlin 1.5+).

**Q: Do I need else in when with sealed class?**
A: No, compiler knows all possible types.

**Q: Can sealed class have abstract members?**
A: Yes, subclasses can override abstract members.

---

## Best Practices

1. Use for modeling states (Loading/Success/Error)
2. Use object for stateless types
3. Use data class for types with data
4. Avoid else in when with sealed classes
5. Keep all subclasses in one file for clarity

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`SealedClassesExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/SealedClassesExampleScreen.kt)
- **Domain Error State Hierarchy**: [`ErrorState.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/domain/model/ErrorState.kt)

