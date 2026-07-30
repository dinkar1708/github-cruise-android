# Offline Cache Implementation Status

**Last Updated:** July 30, 2026

## Overview

This document tracks which data is cached locally using Room Database for offline-first architecture.

---

## ✅ Cached Data (Implemented)

### 1. User Search Results
**Entity:** `SearchUserEntity`
**Location:** `data/local/entity/SearchUserEntity.kt`
**DAO:** `SearchUserDao`

**What's Cached:**
- Search query results
- User ID, login, avatar, score
- Query string (for cache lookup)

**Cache Strategy:**
- Load from cache instantly (page 1 only)
- Fetch from network and update cache
- Fallback to cache on network error

**Retention:** 24 hours (old results auto-deleted)

**Implementation:** `SearchRepositoryImpl.kt:44-56`

---

### 2. User Profiles
**Entity:** `UserEntity`
**Location:** `data/local/entity/UserEntity.kt`
**DAO:** `UserDao`

**What's Cached:**
- User ID, login, avatar
- Name, followers, following
- Public repos count
- Full profile data

**Cache Strategy:**
- Load from cache instantly if available
- Fetch from network and update cache
- Fallback to cache on network error

**Retention:** 7 days (old profiles auto-deleted)

**Implementation:** `UserRepositoryImpl.kt:40-59`

---

### 3. User Repositories
**Entity:** `RepositoryEntity`
**Location:** `data/local/entity/RepositoryEntity.kt`
**DAO:** `RepositoryDao`

**What's Cached:**
- Repository ID, name, full name
- Owner login, description
- Stars, language, HTML URL

**Cache Strategy:**
- Load from cache instantly (page 1 only)
- Fetch from network and update cache
- Clear old cache when fetching page 1
- Fallback to cache on network error

**Retention:** 7 days (old repos auto-deleted)

**Implementation:** `UserRepositoryImpl.kt:65-100`

---

### 4. Favorites
**Entity:** `FavoriteEntity`
**Location:** `data/local/entity/FavoriteEntity.kt`
**DAO:** `FavoriteDao`

**What's Cached:**
- Favorite users and repositories
- ID, type (USER/REPOSITORY)
- Name, description, avatar URL
- Timestamp when favorited

**Cache Strategy:**
- Direct database operations (no network)
- Permanent storage until user removes

**Retention:** Permanent (until user deletes)

**Implementation:** `FavoritesRepositoryImpl.kt:40-80`

---

## ❌ NOT Cached (Network-Only)

### 1. Repository Search Results
**Status:** ❌ NOT IMPLEMENTED
**File:** `RepositorySearchRepositoryImpl.kt`
**Current:** Network-only, no local caching

**What Needs Caching:**
- Repository search query results
- Repository ID, name, description
- Owner info, stars, language
- Query string (for cache lookup)

**Suggested Entity:**
```kotlin
@Entity(tableName = "search_repositories")
data class SearchRepositoryEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val ownerLogin: String,
    val ownerAvatarUrl: String?,
    val htmlUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val query: String, // Search query
    val cachedAt: Long = System.currentTimeMillis()
)
```

**Impact:**
- Repository search doesn't work offline
- No instant results display
- More network usage

---

## Summary

### Coverage

| Feature | Cached | Status |
|---------|--------|--------|
| User Search | ✅ Yes | Complete |
| User Profile | ✅ Yes | Complete |
| User Repositories | ✅ Yes | Complete |
| Favorites | ✅ Yes | Complete |
| **Repository Search** | ❌ No | **TODO** |

### Statistics

- **Cached:** 4 out of 5 data types (80%)
- **Network-Only:** 1 out of 5 data types (20%)
- **Offline Support:** Partial (main user flows work offline, repository search requires network)

---

## User Experience Impact

### ✅ Works Offline:
1. View previously searched users
2. View user profiles (if visited before)
3. Browse user repositories (if loaded before)
4. Access favorites
5. Search users (shows cached results)

### ❌ Requires Network:
1. **Repository Search** - Cannot search repos offline
2. Fresh data (after cache expires)
3. Pagination beyond page 1 (for some features)

---

## Recommendation

**Implement Repository Search Caching** to achieve 100% offline coverage:

1. Create `SearchRepositoryEntity`
2. Create `SearchRepositoryDao`
3. Update `RepositorySearchRepositoryImpl` to use cache-first strategy
4. Add ProGuard rules for new entity
5. Update tests

**Estimated Effort:** 1-2 hours

**Benefits:**
- 100% offline support
- Faster repository search
- Better user experience
- Complete offline-first architecture

---

*For implementation details, see: `docs/technical/features.md`*
