
# MASTER FEATURE SPECIFICATION
## GitHub API Portfolio Suite - Feature Parity Tracking


---

## Portfolio Projects

| Platform | Repository | URL |
|----------|-----------|-----|
| **Android** | github-cruise-android | [github.com/dinkar1708/github-cruise-android](https://github.com/dinkar1708/github-cruise-android) |
| **iOS** | github-repo-search-ios | [github.com/dinkar1708/github-repo-search-ios](https://github.com/dinkar1708/github-repo-search-ios) |
| **Flutter** | flutter_riverpod_template | [github.com/dinkar1708/flutter_riverpod_template](https://github.com/dinkar1708/flutter_riverpod_template) |

**Sharing This Document:** You can share this specification via:
- **GitHub Gist:** Create a public gist for easy sharing
- **Direct Link:** Link to this file in each project's README
- **Portfolio Site:** Include in your portfolio documentation

---

## Table of Contents

1. [Overview](#overview)
2. [Feature Inventory](#feature-inventory)
3. [API Specifications](#api-specifications)

---

## Overview

### Purpose
Define the **complete feature set** for standardizing three GitHub API portfolio projects across iOS, Flutter, and Android.

### Goal
**Achieve feature parity** across all three platforms.

---

## Feature Inventory

### Priority 1: Core Features (Must Have)

#### 1. User Discovery Flow
**Status:** Android Done | iOS TODO | Flutter Partial (1.2, 1.5 Done)

**User Journey:**
```
Splash Screen → Search Users → View Profile → Browse Repos → View Repo Details
```

**Screens:**

**1.1 Splash Screen**
**Feature ID:** 1.1

- App branding with animated gradient
- 3-second auto-navigate to search
- Platform-appropriate design

**1.2 User Search Screen**
**Feature ID:** 1.2

- Search input field (debounced 300-500ms)
- Paginated user list (30 items/page)
- User cards: avatar, username, score
- Pull-to-refresh
- Loading/Error/Empty states
- Tap user → Navigate to profile

**1.3 User Profile Screen**
**Feature ID:** 1.3

**Implementation:** Native screen with user profile and repositories (not WebView)

- **Profile Header Card:**
  - Avatar image (100dp size)
  - Full name (or username if name not available)
  - Gradient background (primary container colors)
  - Stats display:
    - Followers count
    - Following count
- **Favorite Button:**
  - Star icon in app bar
  - Add/Remove user from favorites
  - Visual indicator of favorite status
- **Repository List Section:**
  - Section header showing total repository count
  - Fork filter toggle switch (show/hide forked repos)
  - Repository cards with:
    - Repository name and description
    - Stars, forks, language indicator
  - Paginated list (100 items/page)
  - Pull-to-refresh functionality
  - Loading/Error/Empty states
  - Tap repo → Navigate to Repository Details (1.4)
- **Navigation:**
  - Back button in app bar
  - Returns to User Search Screen

**1.5 User Repository List Screen**
**Feature ID:** 1.5

- List of repositories for a specific user
- Repository cards with name, description, stats
- Stars and forks count display
- Language indicator
- Navigation to repository details

**1.4 Repository Details Screen**
**Feature ID:** 1.4

**Implementation:** Native screen with enhanced repository details (not WebView)

- Repository header: owner avatar, full name
- Description text
- Statistics grid showing:
  - Stars count
  - Forks count
  - Open issues count
  - Watchers count
- Topics/tags display (if available)
- Repository information:
  - Primary language
  - License type
  - Default branch
  - Homepage link (if available)
- Action buttons:
  - Open in browser (launches external browser)
  - Copy clone URL (copies to clipboard)
  - Share repository (system share sheet)
- Favorite button in app bar to save repository
- Loading indicator while fetching data
- Error handling with retry option
- Back navigation

**APIs Used:**
- API-1: Search Users
- API-2: Get User Profile
- API-3: Get User Repositories

---

#### 2. Repository Discovery Flow
**Status:** Android Done | iOS Done | Flutter TODO

**User Journey:**
```
Search Repositories → View Repo List → View Details
```

**Screens:**

**2.1 Repository Search Screen**
**Feature ID:** 2.1

- Search input (debounced 300-500ms)
- Repository cards showing:
  - Repo name, owner avatar, username
  - Description
  - Stars, forks, watchers
  - Primary language
- Infinite scroll pagination (40 items/page)
- Pull-to-refresh
- Loading/Error/Empty states
- Tap repo → View details

**2.2 Repository Details Screen**
**Feature ID:** 2.2

**Note:** This uses the same enhanced repository details screen as Feature 1.4

- Same implementation as Feature 1.4 (Repository Details Screen)
- Accessible from repository search results
- Shows full repository information with native UI
- All features from 1.4 apply here

**APIs Used:**
- API-4: Search Repositories
- API-5: Get Repository Details (for enhanced information)

---

### Priority 2: Advanced Features (Nice to Have)

#### 3. Favorites System
**Feature ID:** 3.0

**Status:** Android Done | iOS TODO | Flutter TODO

**User Journey:**
```
View User/Repo → Add to Favorites → Access from Favorites Tab → Navigate to Details
```

**3.1 Favorite Users**
**Feature ID:** 3.1

**Requirements:**
- Add/Remove user as favorite from User Profile Screen (Feature 1.3)
- Favorite button with star icon in app bar
- Visual indicator showing favorite status (filled/unfilled star)
- Toast/Snackbar confirmation when added/removed
- Store user data: username, avatar URL, full name, bio
- Persistent storage using local preferences/database
- Favorites persist across app restarts

**3.2 Favorite Repositories**
**Feature ID:** 3.2

**Requirements:**
- Add/Remove repository as favorite from Repository Details Screen (Feature 2.2)
- Favorite button with star icon in app bar
- Visual indicator showing favorite status (filled/unfilled star)
- Toast/Snackbar confirmation when added/removed
- Store repo data: full name (owner/repo), description, avatar URL, HTML URL
- Persistent storage using local preferences/database
- Favorites persist across app restarts

**3.3 Favorites List Screen**
**Feature ID:** 3.3

**Requirements:**
- Dedicated tab in bottom navigation showing all favorites
- Display both favorite users and repositories in one list OR separate tabs
- User cards showing:
  - Avatar, username, bio
  - Tap → Navigate to User Repositories Screen (Feature 1.5)
  - Remove button to unfavorite
- Repository cards showing:
  - Owner avatar, repository name, description
  - Tap → Navigate to Repository Details Screen (Feature 2.2)
  - Remove button to unfavorite
- Empty state with message when no favorites
- "Clear All" option to remove all favorites
- Pull-to-refresh (optional, updates cached data)

**Data Model:**
```kotlin
data class FavoriteItem(
    val id: String,                    // username or "owner/repo"
    val type: FavoriteType,            // USER or REPOSITORY
    val name: String,                  // display name
    val description: String?,          // bio or repo description
    val avatarUrl: String,             // avatar image
    val url: String                    // GitHub URL for navigation
)

enum class FavoriteType {
    USER,
    REPOSITORY
}
```

**Storage:**
- Use SharedPreferences (Android), UserDefaults (iOS), or equivalent
- Store as JSON array for easy serialization
- Example key: `favorites_list`

---

#### 4. Settings Screen
**Feature ID:** 4.0

**Status:** Android Done | iOS TODO | Flutter Done

**Requirements:**
- Settings screen accessible from home/profile
- Theme toggle (Light/Dark/Auto)
- Language selection (EN/JP)
- Clear cache option
- About/Version info
- Open source licenses
- Privacy policy and terms of service links
- Help and support options

---

## API Specifications

**All GitHub API details, endpoints, request/response formats, and implementation status are documented in:**

📄 **[GITHUB_API_SPECIFICATION.md](./GITHUB_API_SPECIFICATION.md)**

This document contains:
- Complete API endpoint definitions
- Request/response schemas
- Platform implementation status (Android/iOS/Flutter)
- Available APIs for future implementation

**Note:** Each platform project's README.md should maintain a gap analysis showing which features still need to be implemented.

---

*End of Document*
