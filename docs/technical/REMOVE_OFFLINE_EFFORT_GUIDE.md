# Remove Offline Feature - Action Guide

**Purpose:** Complete guide to remove offline caching and load data directly from API only.

**Last Updated:** 2026-08-19

---

## Summary

| Category | Action Required |
|----------|-----------------|
| **Repository Files** | Modify 3 files |
| **Database Files** | Delete 11 files |
| **DI Module** | Delete or modify 1 file |
| **Gradle Dependencies** | Remove 4 dependencies |
| **Test Files** | Delete 2 files, modify 2 files |
| **ProGuard Rules** | Remove Room rules |
| **Documentation** | Update 3 files |
| **TOTAL** | **22 files affected** |

---

## Part 1: Modify Repository Files

### 1.1 SearchRepositoryImpl.kt ⚠️ CRITICAL

**File:** `app/src/main/java/com/jetpack/compose/github/github/cruise/data/repository/search/SearchRepositoryImpl.kt`

**Remove:**
- Lines 44-56: Cache loading logic
- Lines 63-68: Cache insertion logic
- Lines 71-82: Cache fallback logic
- Constructor parameter: `searchUserDao`

**Keep:**
- Lines 59-60: Network API call
- Line 70: emit(searchResult)

**After simplification:**
```kotlin
class SearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SearchRepository {

    override fun searchUsers(
        userName: String,
        page: Int,
        pageSize: Int,
    ): Flow<SearchUser> = flow {
        val searchResult = networkDataSource.searchUser(userName, page, pageSize)
        emit(searchResult)
    }.flowOn(ioDispatcher)
}
```

---

### 1.2 UserRepositoryImpl.kt ⚠️ CRITICAL

**File:** `app/src/main/java/com/jetpack/compose/github/github/cruise/data/repository/user/UserRepositoryImpl.kt`

**Remove:**
- Constructor parameters: `userDao`, `repositoryDao`
- All cache loading logic in `getUserProfile()`
- All cache logic and cleanup in `getUserRepositories()`

**Keep:**
- Direct network calls only
- emit() statements for network data

**Simplified getUserProfile():**
```kotlin
override fun getUserProfile(userName: String): Flow<User> = flow {
    val user = networkDataSource.getUserProfile(userName)
    emit(user)
}.flowOn(ioDispatcher)
```

**Simplified getUserRepositories():**
```kotlin
override fun getUserRepositories(userName: String, page: Int, pageSize: Int): Flow<List<Repository>> = flow {
    val repositories = networkDataSource.getUserRepositories(userName, page, pageSize)
    emit(repositories)
}.flowOn(ioDispatcher)
```

---

### 1.3 RepositorySearchRepositoryImpl.kt ⚠️ CRITICAL

**File:** `app/src/main/java/com/jetpack/compose/github/github/cruise/data/repository/repositorysearch/RepositorySearchRepositoryImpl.kt`

**Remove:**
- Constructor parameter: `searchRepositoryDao`
- All cache loading logic
- All cache insertion logic
- All error fallback logic

**Keep:**
- Network API call
- emit(networkResult)

---

## Part 2: Delete Database Files

### 2.1 Delete Entity Files (5 files)

**Location:** `app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/`

```bash
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/UserEntity.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/RepositoryEntity.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/SearchUserEntity.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/SearchRepositoryEntity.kt
```

**⚠️ Optional:** Keep `FavoriteEntity.kt` if you want to retain the Favorites feature

---

### 2.2 Delete DAO Files (5 files)

**Location:** `app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/`

```bash
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/UserDao.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/RepositoryDao.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/SearchUserDao.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/SearchRepositoryDao.kt
```

**⚠️ Optional:** Keep `FavoriteDao.kt` if you want to retain the Favorites feature

---

### 2.3 Delete Database File

**Location:** `app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/`

```bash
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/GithubCruiseDatabase.kt
```

**Alternative:** If keeping Favorites, modify instead of delete:

```kotlin
@Database(
    entities = [
        FavoriteEntity::class  // Keep only this
    ],
    version = 3,  // Increment version
    exportSchema = false
)
abstract class GithubCruiseDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao  // Keep only this
}
```

---

### 2.4 Delete Entire Local Folder (If removing all offline)

```bash
rm -rf app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/
```

---

## Part 3: Update Dependency Injection

### 3.1 DatabaseModule.kt

**File:** `app/src/main/java/com/jetpack/compose/github/github/cruise/di/DatabaseModule.kt`

**Option A - Delete entire file** (if removing all offline features)

**Option B - Modify file** (if keeping Favorites):

**Remove these @Provides methods:**
```kotlin
// DELETE:
@Provides fun provideUserDao(...)
@Provides fun provideRepositoryDao(...)
@Provides fun provideSearchUserDao(...)
@Provides fun provideSearchRepositoryDao(...)
```

**Keep only:**
```kotlin
// KEEP:
@Provides fun provideDatabase(...) // Updated with only FavoriteEntity
@Provides fun provideFavoriteDao(...)
```

---

## Part 4: Update Gradle Dependencies

### 4.1 Remove Room Dependencies

**File:** `app/build.gradle.kts`

**Remove these lines (131-134):**

```kotlin
// DELETE if removing all offline:
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
```

**Remove from test dependencies:**
```kotlin
// DELETE:
testImplementation(libs.androidx.room.testing)
```

**⚠️ Note:** If keeping Favorites, keep all Room dependencies

---

## Part 5: Delete Test Files

### 5.1 Delete Offline Cache Tests

```bash
rm app/src/test/java/com/jetpack/compose/github/github/cruise/repository/search/SearchRepositoryOfflineCacheTest.kt
rm app/src/test/java/com/jetpack/compose/github/github/cruise/repository/user/UserRepositoryOfflineCacheTest.kt
```

---

### 5.2 Modify Repository Test Files

**Files to modify:**
- `SearchRepositoryImplTest.kt`
- `UserRepositoryImplTest.kt`

**Actions:**
- Remove all cache-related test cases
- Remove DAO mocks from test setup
- Remove DAO injection from constructor
- Keep only network-related tests

---

## Part 6: Update ProGuard Rules

### 6.1 Remove Room ProGuard Rules

**File:** `app/proguard-rules.pro`

**Delete these lines:**
```proguard
# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**
```

**⚠️ Note:** If keeping Favorites, keep these rules

---

## Part 7: Update Documentation

### 7.1 Update OFFLINE_CACHE_STATUS.md

**File:** `docs/technical/OFFLINE_CACHE_STATUS.md`

**Actions:**
- Add header: "⚠️ DEPRECATED - Offline feature removed"
- Add date of removal
- Keep as historical reference

---

### 7.2 Update features.md

**File:** `docs/technical/features.md`

**Actions:**
- Remove section: "### 4. Offline-First Architecture (Room Database)"
- Update test count from 107 to 88 tests
- Remove offline-related feature descriptions

---

### 7.3 Update README.md

**File:** `README.md`

**Actions:**
- Update FAQ "Does the app work offline?" → Answer: "No"
- Remove offline-related stats from metrics
- Remove "Offline-first architecture" from features list
- Update OFFLINE_CACHE_STATUS.md link to indicate deprecation

---

## Part 8: Code Impact Summary

### Files Deleted

| Category | Count |
|----------|-------|
| Entity files | 4-5 files |
| DAO files | 4-5 files |
| Database file | 1 file |
| Test files | 2 files |
| DI Module (optional) | 1 file |
| **TOTAL** | **12-14 files** |

---

### Files Modified

| File | Changes |
|------|---------|
| SearchRepositoryImpl.kt | Remove ~58 lines of cache logic |
| UserRepositoryImpl.kt | Remove ~100 lines of cache logic |
| RepositorySearchRepositoryImpl.kt | Remove ~65 lines of cache logic |
| SearchRepositoryImplTest.kt | Remove cache test cases |
| UserRepositoryImplTest.kt | Remove cache test cases |
| app/build.gradle.kts | Remove 4 Room dependencies |
| proguard-rules.pro | Remove Room rules |
| 3 documentation files | Update to reflect removal |

---

## Part 9: Step-by-Step Removal Process

### Step 1: Backup Code
```bash
git checkout -b remove-offline-feature
git add .
git commit -m "Backup before removing offline feature"
```

---

### Step 2: Delete Database Files

```bash
# Delete entity files
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/UserEntity.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/RepositoryEntity.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/SearchUserEntity.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/entity/SearchRepositoryEntity.kt

# Delete DAO files
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/UserDao.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/RepositoryDao.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/SearchUserDao.kt
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/dao/SearchRepositoryDao.kt

# Delete database file
rm app/src/main/java/com/jetpack/compose/github/github/cruise/data/local/GithubCruiseDatabase.kt

# Delete DI module
rm app/src/main/java/com/jetpack/compose/github/github/cruise/di/DatabaseModule.kt
```

---

### Step 3: Delete Test Files

```bash
rm app/src/test/java/com/jetpack/compose/github/github/cruise/repository/search/SearchRepositoryOfflineCacheTest.kt
rm app/src/test/java/com/jetpack/compose/github/github/cruise/repository/user/UserRepositoryOfflineCacheTest.kt
```

---

### Step 4: Modify Repository Files

**Manual editing required:**

1. Open `SearchRepositoryImpl.kt`
   - Remove DAO parameter from constructor
   - Remove cache loading (lines 44-56)
   - Remove cache insertion (lines 63-68)
   - Remove error fallback (lines 71-82)
   - Keep only network call and emit

2. Open `UserRepositoryImpl.kt`
   - Remove userDao and repositoryDao from constructor
   - Simplify getUserProfile() to only network call
   - Simplify getUserRepositories() to only network call

3. Open `RepositorySearchRepositoryImpl.kt`
   - Remove searchRepositoryDao from constructor
   - Remove all cache logic
   - Keep only network call

---

### Step 5: Update Gradle Dependencies

**Edit:** `app/build.gradle.kts`

**Remove:**
```kotlin
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
testImplementation(libs.androidx.room.testing)
```

---

### Step 6: Update ProGuard Rules

**Edit:** `app/proguard-rules.pro`

**Remove:**
```proguard
# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**
```

---

### Step 7: Gradle Sync & Build

```bash
# Sync gradle
./gradlew sync

# Clean build
./gradlew clean

# Build debug
./gradlew assembleDebug
```

**⚠️ Fix any import errors for deleted DAO/Entity classes**

---

### Step 8: Update Test Files

**Edit:**
- `SearchRepositoryImplTest.kt`
- `UserRepositoryImplTest.kt`

**Actions:**
- Remove DAO mock setup
- Remove cache-related test cases
- Keep only network tests

---

### Step 9: Run Tests

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run UI tests
./gradlew connectedDebugAndroidTest
```

---

### Step 10: Update Documentation

**Edit these files:**
1. `docs/technical/OFFLINE_CACHE_STATUS.md` → Mark deprecated
2. `docs/technical/features.md` → Remove offline sections
3. `README.md` → Update FAQ and features

---

## Part 10: What Changes

### You LOSE

| Feature | Impact |
|---------|--------|
| Offline browsing | Users can't view previously loaded content without network |
| Instant data display | Every screen shows loading spinner (no cache) |
| Reduced network calls | API calls increase 3-5x (no cache hits) |
| Graceful degradation | App errors immediately on network failure |
| Poor network handling | Slow networks = slow app (no cache fallback) |

---

### You GAIN

| Benefit | Impact |
|---------|--------|
| Simpler codebase | ~1,773 fewer lines of code |
| Smaller APK | 500KB-1MB reduction |
| Fewer dependencies | 4 fewer gradle dependencies |
| Faster builds | No Room annotation processing |
| Easier testing | No cache layer to mock |
| Always fresh data | No stale cache issues |

---

## Part 11: Alternative - Keep Favorites Only

### Keep These Files:
- ✅ `FavoriteEntity.kt`
- ✅ `FavoriteDao.kt`
- ✅ `GithubCruiseDatabase.kt` (modified to only include FavoriteEntity)
- ✅ `FavoritesRepositoryImpl.kt`
- ✅ `DatabaseModule.kt` (modified to only provide FavoriteDao)
- ✅ All Room dependencies in build.gradle.kts

### Delete These:
- ❌ `UserEntity.kt`, `RepositoryEntity.kt`, `SearchUserEntity.kt`, `SearchRepositoryEntity.kt`
- ❌ `UserDao.kt`, `RepositoryDao.kt`, `SearchUserDao.kt`, `SearchRepositoryDao.kt`
- ❌ All cache logic from SearchRepositoryImpl, UserRepositoryImpl, RepositorySearchRepositoryImpl

---

## Part 12: Testing Checklist

After removing offline features, test:

| Test Case | Expected Behavior |
|-----------|-------------------|
| Search users with network | Shows results from API |
| Search users without network | Shows error immediately (no cache fallback) |
| View user profile with network | Shows profile from API |
| View user profile without network | Shows error immediately (no cache) |
| Search same user twice | Makes API call both times (no cache reuse) |
| Rotate screen during search | Data reloads from API (no cache) |
| Check APK size | Reduced by ~500KB-1MB |
| Run all unit tests | 88 tests pass (19 cache tests removed) |
| Run all UI tests | 48 UI tests still pass |

---

## Part 13: Troubleshooting

### Build Errors

**Problem:** Import errors for deleted classes
**Solution:** Remove all imports for DAO/Entity classes

**Problem:** Constructor injection errors
**Solution:** Remove DAO parameters from repository constructors

**Problem:** Missing DatabaseModule
**Solution:** Delete or modify based on whether keeping Favorites

---

### Runtime Errors

**Problem:** App crashes on startup
**Solution:** Clear app data
```bash
adb shell pm clear com.jetpack.compose.github.github.cruise.debug
```

**Problem:** Old database still exists
**Solution:** Add migration code or increment database version

---

### Test Failures

**Problem:** Tests fail for removed cache logic
**Solution:** Delete cache test files entirely

**Problem:** Repository tests fail
**Solution:** Remove DAO mocks and cache test cases

---

## Quick Reference

### Files to Delete (13 files)

```
app/src/main/java/.../data/local/
├── entity/
│   ├── UserEntity.kt                    ❌ DELETE
│   ├── RepositoryEntity.kt              ❌ DELETE
│   ├── SearchUserEntity.kt              ❌ DELETE
│   ├── SearchRepositoryEntity.kt        ❌ DELETE
│   └── FavoriteEntity.kt                ⚠️ OPTIONAL (keep for Favorites)
├── dao/
│   ├── UserDao.kt                       ❌ DELETE
│   ├── RepositoryDao.kt                 ❌ DELETE
│   ├── SearchUserDao.kt                 ❌ DELETE
│   ├── SearchRepositoryDao.kt           ❌ DELETE
│   └── FavoriteDao.kt                   ⚠️ OPTIONAL (keep for Favorites)
└── GithubCruiseDatabase.kt              ❌ DELETE or ⚠️ MODIFY

app/src/main/java/.../di/
└── DatabaseModule.kt                    ❌ DELETE or ⚠️ MODIFY

app/src/test/.../repository/
├── search/SearchRepositoryOfflineCacheTest.kt   ❌ DELETE
└── user/UserRepositoryOfflineCacheTest.kt       ❌ DELETE
```

---

### Files to Modify (8 files)

```
MODIFY - Remove cache logic:
├── SearchRepositoryImpl.kt              ⚠️ MODIFY
├── UserRepositoryImpl.kt                ⚠️ MODIFY
└── RepositorySearchRepositoryImpl.kt    ⚠️ MODIFY

MODIFY - Remove DAO mocks:
├── SearchRepositoryImplTest.kt          ⚠️ MODIFY
└── UserRepositoryImplTest.kt            ⚠️ MODIFY

MODIFY - Remove dependencies:
└── app/build.gradle.kts                 ⚠️ MODIFY

MODIFY - Remove Room rules:
└── app/proguard-rules.pro               ⚠️ MODIFY

MODIFY - Update docs:
├── docs/technical/OFFLINE_CACHE_STATUS.md      ⚠️ MODIFY
├── docs/technical/features.md                  ⚠️ MODIFY
└── README.md                                   ⚠️ MODIFY
```

---

## Recommendation

### Think Before Removing

**Offline feature provides:**
- 60% faster initial load
- Works in poor network conditions
- Reduces API calls (important for 60/hour rate limit)
- Better user experience

### Only remove if:
- You need smaller APK urgently
- You have unlimited API rate limits
- Users always have excellent network
- You want simpler codebase for learning

### Keep offline if:
- Users have slow/unreliable network
- You're hitting API rate limits
- You care about perceived performance
- You want better UX

---

**Document Version:** 1.0
**Created:** 2026-08-19

**Related Docs:**
- `OFFLINE_CACHE_STATUS.md` - Current offline implementation
- `ARCHITECTURE_BEST_PRACTICES.md` - Architecture guidelines
- `features.md` - Complete feature list

---

**END OF DOCUMENT**
