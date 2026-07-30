# Favorites Implementation

**Status:** ✅ Implemented
**Storage:** Room Database
**Last Updated:** July 30, 2026

---

## What We Built

Favorites feature allows users to save GitHub users and repositories for quick access.

### Database Entity

**File:** `data/local/entity/FavoriteEntity.kt`

```kotlin
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val type: String,           // "USER" or "REPOSITORY"
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val url: String,
    val timestamp: Long
)
```

### DAO (Database Access)

**File:** `data/local/dao/FavoriteDao.kt`

```kotlin
@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()
}
```

### Repository

**File:** `data/repository/favorites/FavoritesRepositoryImpl.kt`

```kotlin
class FavoritesRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoritesRepository {

    override fun getAllFavorites(): Flow<List<FavoriteItem>> =
        favoriteDao.getAllFavorites().map { it.toDomainModel() }

    override suspend fun addFavorite(item: FavoriteItem) {
        favoriteDao.insertFavorite(item.toEntity())
    }

    override suspend fun removeFavorite(id: String) {
        favoriteDao.deleteFavoriteById(id)
    }

    override suspend fun isFavorite(id: String): Boolean {
        return favoriteDao.isFavorite(id)
    }

    override suspend fun clearFavorites() {
        favoriteDao.clearFavorites()
    }
}
```

---

## Features

### Current Features
- ✅ Add users to favorites
- ✅ Add repositories to favorites
- ✅ Remove from favorites
- ✅ Check if item is favorited
- ✅ View all favorites sorted by date
- ✅ Clear all favorites
- ✅ Reactive UI updates (Flow)
- ✅ Persistent storage (survives app restart)

### Storage Details
- **Technology:** Room Database (SQLite)
- **Retention:** Permanent (until user deletes)
- **Reactive:** Flow-based updates (UI auto-updates)
- **Type-safe:** Compile-time query verification

---

## Cache vs Favorites

| Aspect | Cached Data | Favorites |
|--------|-------------|-----------|
| **Purpose** | Speed + Offline browsing | User bookmarks |
| **Lifetime** | 7-24 hours | Permanent |
| **User Control** | Automatic | Manual |
| **Storage** | UserEntity, RepositoryEntity, SearchUserEntity | FavoriteEntity |

Both are needed - they serve different purposes.

---

## Files

**Database Layer:**
- `data/local/entity/FavoriteEntity.kt` - Database entity
- `data/local/dao/FavoriteDao.kt` - Database access

**Repository Layer:**
- `data/repository/favorites/FavoritesRepositoryImpl.kt` - Repository implementation

**Domain Layer:**
- `domain/model/FavoriteItem.kt` - Domain model
- `domain/repository/FavoritesRepository.kt` - Repository interface

**UI Layer:**
- `ui/features/favorites/FavoritesViewModel.kt` - State management
- `ui/features/favorites/FavoritesScreen.kt` - UI screen

---

## Future Enhancements (Optional)

**Search within favorites** (1-2 hours)
```kotlin
@Query("SELECT * FROM favorites WHERE name LIKE '%' || :query || '%'")
fun searchFavorites(query: String): Flow<List<FavoriteEntity>>
```

**Filter by type** (1 hour)
```kotlin
@Query("SELECT * FROM favorites WHERE type = :type")
fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>
```

**Sort options** (2-3 hours)
- By name (A-Z, Z-A)
- By date (newest, oldest)

**Export/Import** (4-6 hours)
- Export to JSON file
- Import from JSON file

---

**Last Updated:** July 30, 2026
**Status:** Production Ready ✅
