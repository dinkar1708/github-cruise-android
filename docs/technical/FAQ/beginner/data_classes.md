# Data Classes vs Regular Classes

## Purpose

Data classes simplify creating classes that hold data with automatic implementations of equals, hashCode, toString, and copy.

---

## Data Class

```kotlin
data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

Automatically generates:
- `equals()` - Compare by properties
- `hashCode()` - Hash based on properties
- `toString()` - Readable string representation
- `copy()` - Create modified copies
- `componentN()` - Destructuring

---

## Regular Class

```kotlin
class User(
    val id: Int,
    val name: String,
    val email: String
)
```

You must manually implement equals, hashCode, toString, copy.

---

## Automatic Features

### equals() - Value Equality
```kotlin
val user1 = User(1, "John", "john@example.com")
val user2 = User(1, "John", "john@example.com")

println(user1 == user2)  // true (compares properties)
```

### toString() - Readable Output
```kotlin
val user = User(1, "John", "john@example.com")
println(user)  // User(id=1, name=John, email=john@example.com)
```

### copy() - Immutable Updates
```kotlin
val user = User(1, "John", "john@example.com")
val updated = user.copy(email = "john.doe@example.com")

println(user.email)  // john@example.com (unchanged)
println(updated.email)  // john.doe@example.com
```

### Destructuring
```kotlin
val user = User(1, "John", "john@example.com")
val (id, name, email) = user

println(name)  // John
```

---

## When to Use Data Classes

### Use for:
- API response models
- Database entities
- UI state holders
- Configuration objects

```kotlin
// API Response
data class RepoResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val stars: Int
)

// UI State
data class UiState(
    val isLoading: Boolean = false,
    val data: List<String> = emptyList(),
    val error: String? = null
)
```

### Don't use for:
- Classes with behavior/logic
- Classes with mutable state
- Classes with many properties (20+)
- Singleton objects

---

## Practical Examples

### Example 1: API Model
```kotlin
data class Repository(
    val id: Int,
    val name: String,
    val owner: String,
    val stars: Int,
    val language: String?
)

// Usage
val repo = Repository(1, "MyRepo", "john", 100, "Kotlin")
Log.d("API", "Repo: $repo")
```

### Example 2: UI State
```kotlin
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }
}
```

### Example 3: Room Entity
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val avatar: String?
)
```

---

## copy() Function

Create modified copies while keeping original unchanged:

```kotlin
val user = User(1, "John", "john@example.com")

// Update one property
val renamed = user.copy(name = "Jane")

// Update multiple properties
val updated = user.copy(
    name = "Jane",
    email = "jane@example.com"
)
```

---

## Nested Data Classes

```kotlin
data class Address(
    val street: String,
    val city: String
)

data class User(
    val name: String,
    val address: Address
)

// Deep copy required for nested
val user = User("John", Address("Main St", "NYC"))
val moved = user.copy(
    address = user.address.copy(city = "SF")
)
```

---

## Requirements for Data Classes

1. Primary constructor must have at least one parameter
2. All primary constructor parameters must be val or var
3. Cannot be abstract, open, sealed, or inner

```kotlin
// Valid
data class User(val name: String)

// Invalid - no parameters
// data class Empty()

// Invalid - parameter not val/var
// data class User(name: String)
```

---

## Common Questions

**Q: When to use data class vs regular class?**
A: Use data class for holding data. Use regular class for behavior/logic.

**Q: Can data classes have methods?**
A: Yes, you can add methods, but primary purpose is holding data.

**Q: Is copy() a deep copy?**
A: No, it's shallow. Nested objects are not copied.

**Q: Can data classes be extended?**
A: Data classes are final by default. Use sealed classes for inheritance.

---

## Best Practices

1. Use for pure data holders
2. Keep properties immutable (val not var)
3. Use copy() for updates, not mutation
4. Add default values when appropriate
5. Use sealed classes for inheritance needs

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`DataClassesExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/DataClassesExampleScreen.kt)
- **Domain Models**:
  - [`SearchUser.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/domain/model/SearchUser.kt)
  - [`RepositoryDetails.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/domain/model/RepositoryDetails.kt)
- **Room Database Entities**:
  - [`UserEntity.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/UserEntity.kt)
  - [`RepositoryEntity.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/RepositoryEntity.kt)

