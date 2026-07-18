
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

- Profile header: avatar, username, full name
- Stats: followers, following, public repos
- Bio/description
- Repository list (paginated, 100 items/page)
- Fork filter toggle
- Pull-to-refresh
- Tap repo → View details

**1.5 User Repository List Screen**
**Feature ID:** 1.5

- List of repositories for a specific user
- Repository cards with name, description, stats
- Stars and forks count display
- Language indicator
- Navigation to repository details

**1.4 Repository Details Screen**
**Feature ID:** 1.4

- WebView showing GitHub repo page
- Loading indicator
- Error handling
- Back navigation

**APIs Used:**
- API-1: Search Users
- API-2: Get User Profile
- API-3: Get User Repositories

---

#### 2. Repository Discovery Flow
**Status:** Android TODO | iOS Done | Flutter TODO

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

- Full repository information
- Statistics grid
- Action buttons (open browser, copy clone URL, share)

**APIs Used:**
- API-4: Search Repositories

---

### Priority 2: Advanced Features (Nice to Have)

#### 3. Favorites Screen
**Feature ID:** 3.0

**Status:** All TODO

**Requirements:**
- Screen showing saved favorite users/repos
- Add/Remove favorites from profile/repo screens
- Persistent storage across app restarts
- Empty state when no favorites
- Navigate to saved user/repo details

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

**See detailed API documentation:** [GITHUB_API_SPECIFICATION.md](./GITHUB_API_SPECIFICATION.md)

### Currently Used APIs

| API | Endpoint | Android | iOS | Flutter |
|-----|----------|---------|-----|---------|
| Search Users | `GET /search/users` | Done | TODO | Done |
| Get User Profile | `GET /users/{username}` | Done | TODO | TODO |
| Get User Repositories | `GET /users/{username}/repos` | Done | TODO | Done |
| Search Repositories | `GET /search/repositories` | TODO | Done | TODO |

### TODO: APIs Available for Implementation

- Get Repository Details
- Get Repository Issues
- Get Repository Commits
- Get User Followers
- Get User Following
- Get Public Events
- Get User Gists
- Get Organization Repositories

**Full details in:** [GITHUB_API_SPECIFICATION.md](./GITHUB_API_SPECIFICATION.md)

**Note:** Gap analysis (what features are missing) should be maintained in each project's README.md so each platform team knows what they need to implement.

---

*End of Document*
