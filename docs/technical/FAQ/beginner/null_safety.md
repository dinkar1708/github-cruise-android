# Kotlin Null Safety

## Purpose

Kotlin's null safety prevents null pointer exceptions at compile time, one of the most common causes of Android app crashes.

---

## Nullable vs Non-Nullable Types

### Non-Nullable (Default)
```kotlin
var name: String = "John"
name = null  // ERROR: Null cannot be a value of a non-null type String
```

### Nullable (with ?)
```kotlin
var name: String? = "John"
name = null  // OK
```

---

## Safe Call Operator (?.)

Access properties/methods safely when object might be null:

```kotlin
val name: String? = null
val length = name?.length  // Returns null instead of crashing

// Chaining
val city = user?.address?.city
```

---

## Elvis Operator (?:)

Provide default value when null:

```kotlin
val name: String? = null
val display = name ?: "Guest"  // Use "Guest" if name is null

// With function
fun getUserName(user: User?): String {
    return user?.name ?: "Unknown"
}
```

---

## Not-Null Assertion (!!)

Force unwrap - CRASHES if null:

```kotlin
val name: String? = getName()
val nonNull = name!!  // CRASH if name is null

// Use only when 100% certain not null
```

---

## Safe Casts (as?)

Safe type casting:

```kotlin
val obj: Any = "Hello"
val str: String? = obj as? String  // Safe cast
val num: Int? = obj as? Int  // Returns null (not Int)

// Unsafe cast (crashes if wrong type)
val str2: String = obj as String  // OK
val num2: Int = obj as Int  // CRASH
```

---

## let Function

Execute code only if not null:

```kotlin
val name: String? = "John"
name?.let {
    println("Name is $it")
    // $it is non-null String here
}

// Chaining
user?.let { u ->
    u.address?.let { addr ->
        println(addr.city)
    }
}
```

---

## lateinit

For non-null properties initialized later:

```kotlin
class MyActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
    }
}

// Check if initialized
if (::binding.isInitialized) {
    // Use binding
}
```

---

## Nullable Collections

```kotlin
// Nullable list
val list: List<String>? = null

// List of nullables
val list2: List<String?> = listOf("A", null, "B")

// Filter null values
val nonNulls = list2.filterNotNull()  // List<String>
```

---

## Practical Examples

### Example 1: Safe UI Update
```kotlin
class ProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun updateUI(context: Context) {
        _user.value?.let { user ->
            Log.d("Profile", "User: ${user.name}")
            // Update UI safely
        } ?: run {
            Log.d("Profile", "No user data")
        }
    }
}
```

### Example 2: Safe Navigation
```kotlin
fun navigateToDetails(userId: String?) {
    userId?.let {
        navController.navigate("details/$it")
    } ?: Log.e("Nav", "User ID is null")
}
```

### Example 3: Safe Data Access
```kotlin
@Composable
fun UserProfile(user: User?) {
    Text(text = user?.name ?: "Loading...")
    Text(text = user?.email ?: "No email")
}
```

---

## Common Patterns

### Pattern 1: Safe Property Access
```kotlin
val userName = user?.profile?.name ?: "Guest"
```

### Pattern 2: Safe Method Call
```kotlin
val result = repository?.getUserData()?.firstOrNull()
```

### Pattern 3: Null Check with let
```kotlin
data?.let { safeData ->
    processData(safeData)
} ?: showError()
```

---

## Common Questions

**Q: When to use ? vs !!?**
A: Use ? for safe handling. Avoid !! - only use when 100% certain not null.

**Q: What's the difference between lateinit and nullable?**
A: lateinit is for non-null properties you'll initialize later. Nullable (?) allows null values.

**Q: How to convert nullable to non-null?**
A: Use !! (unsafe) or ?: with default value (safe).

**Q: Can I use lateinit with nullable types?**
A: No, lateinit only works with non-null types.

---

## Best Practices

1. Prefer nullable types over !!
2. Use ?: for default values
3. Use let for null-safe operations
4. Use lateinit for properties initialized in onCreate/init
5. Avoid multiple !! in same expression

---

## Anti-Patterns

```kotlin
// BAD - multiple !!
val result = user!!.address!!.city!!

// GOOD - safe access
val result = user?.address?.city ?: "Unknown"

// BAD - unnecessary null check
if (name != null) {
    println(name.length)
}

// GOOD - use safe call
name?.let { println(it.length) }
```

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`NullSafetyExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/NullSafetyExampleScreen.kt)
- **Input Validation**: [`SearchInputValidator.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/domain/validation/SearchInputValidator.kt)
- **Safe Data Handling in Repositories**: [`UserRepositoryImpl.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/data/repository/user/UserRepositoryImpl.kt)

