# Hilt Dependency Injection

## Purpose

Hilt provides a standard way to incorporate Dagger dependency injection into Android apps, automatically managing component lifecycles and eliminating manual factory boilerplate.

---

## Core Setup in Our Codebase

### 1. Application Class (`@HiltAndroidApp`)
Triggers Hilt's code generation and creates the application-level dependency container.

```kotlin
@HiltAndroidApp
class GithubCruiseApplication : Application() {
    @Inject lateinit var localeDataStore: LocaleDataStore
    @Inject lateinit var secureTokenManager: SecureTokenManager
}
```
See implementation in [`App.kt:L18`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/App.kt#L18).

### 2. Activity Entry Point (`@AndroidEntryPoint`)
Marks Android framework classes for dependency injection.

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity()
```
See implementation in [`MainActivity.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/MainActivity.kt).

### 3. ViewModel Injection (`@HiltViewModel`)
Enables constructor injection in ViewModels and retrieval via `hiltViewModel()` in Compose.

```kotlin
@HiltViewModel
class RepositorySearchViewModel @Inject constructor(
    private val repositorySearchUseCase: RepositorySearchUseCase
) : ViewModel()
```
See implementation in [`RepositorySearchViewModel.kt:L30`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repositorysearch/RepositorySearchViewModel.kt#L30).

---

## Hilt Module Patterns in Our Architecture

### 1. `@Provides` for External / Builder Classes
Used when creating instances of classes you don't own (e.g. Room Database, Retrofit).

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideGithubCruiseDatabase(@ApplicationContext context: Context): GithubCruiseDatabase {
        return Room.databaseBuilder(context, GithubCruiseDatabase::class.java, DATABASE_NAME).build()
    }
}
```
See real implementation in [`DatabaseModule.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/di/DatabaseModule.kt).

### 2. `@Binds` for Interface Implementations
More efficient than `@Provides` because it generates no extra factory code; simply maps an interface to its `@Inject`-annotated implementation.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository
}
```
See real implementation in [`RepositoryModule.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/di/RepositoryModule.kt).

### 3. Custom Qualifiers & Dispatcher Modules ([`CoroutinesModule.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/di/CoroutinesModule.kt))

When you have **multiple instances of the exact same return type** (e.g. `Dispatchers.IO` and `Dispatchers.Default` both returning `CoroutineDispatcher`), Hilt cannot differentiate between them by type alone. Custom Qualifiers act as unique tags.

```kotlin
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
```

#### Keyword-by-Keyword Breakdown:

| Keyword / Annotation | What It Does | Why It Is Needed |
| :--- | :--- | :--- |
| **`@Qualifier`** | Marks this custom annotation as a Dagger/Hilt qualifier tag. | Prevents `[Dagger/DuplicateBindings]` compile error when multiple `@Provides` functions return `CoroutineDispatcher`. |
| **`@Retention(AnnotationRetention.RUNTIME)`** | Preserves the annotation in `.class` bytecode at runtime. | Required so Dagger/Hilt code generators and reflection tools can read the annotation during dependency graph resolution. |
| **`annotation class IoDispatcher`** | Declares the custom annotation name. | Creates the `@IoDispatcher` tag used at both `@Provides` and `@Inject constructor(@IoDispatcher dispatcher: ...)` sites. |
| **`@Module`** | Identifies this object as a Dagger module containing provider recipes. | Tells Hilt to inspect this file for `@Provides` or `@Binds` functions. |
| **`@InstallIn(SingletonComponent::class)`** | Hooks this module into the Application-level dependency graph. | Makes the provided dispatchers available application-wide across the entire app lifecycle. |
| **`object CoroutinesModule`** | Defines a Kotlin singleton `object`. | Allows Dagger to call provider methods directly (static calls) without allocating module instances in memory. |
| **`@Provides`** | Tells Hilt how to construct or return the dependency. | Executes the function body (`Dispatchers.IO`) whenever `@IoDispatcher CoroutineDispatcher` is requested. |

#### How to Inject at Usage Site:
```kotlin
class SearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SearchRepository
```


---

## Common Pitfalls & Junior Tips

1. **Prefer `@Binds` over `@Provides`**: When mapping an interface to an implementation class you own, always use an abstract `@Binds` method.
2. **Constructor Injection First**: Only use field injection (`@Inject lateinit var`) when Hilt cannot instantiate the class directly (like `Application` or `BroadcastReceiver`).
3. **Scoped Instances**: Mark DAOs and Repositories with `@Singleton` or `@ViewModelScoped` appropriately to avoid creating duplicate instances.

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`HiltDIExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/HiltDIExampleScreen.kt)
- **Application Class (`@HiltAndroidApp`)**: [`App.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/App.kt#L18)
- **Database Module (`@Provides`)**: [`DatabaseModule.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/di/DatabaseModule.kt)
- **Repository Bindings (`@Binds`)**: [`RepositoryModule.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/di/RepositoryModule.kt)
- **Dispatcher Qualifiers (`@Qualifier`)**: [`CoroutinesModule.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/di/CoroutinesModule.kt)
- **Production `@HiltViewModel`**: [`RepositorySearchViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repositorysearch/RepositorySearchViewModel.kt#L30)
