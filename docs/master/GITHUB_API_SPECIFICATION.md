# GitHub API Specification
## Complete API Reference for Cross-Platform Projects

**API Version:** 2022-11-28
**Base URL:** `https://api.github.com`

---

## Portfolio Projects

| Platform | Repository | URL |
|----------|-----------|-----|
| **Android** | github-cruise-android | [github.com/dinkar1708/github-cruise-android](https://github.com/dinkar1708/github-cruise-android) |
| **iOS** | github-repo-search-ios | [github.com/dinkar1708/github-repo-search-ios](https://github.com/dinkar1708/github-repo-search-ios) |
| **Flutter** | flutter_riverpod_template | [github.com/dinkar1708/flutter_riverpod_template](https://github.com/dinkar1708/flutter_riverpod_template) |

---

## API Summary

### Currently Implemented APIs

| API Name | Endpoint | Android | iOS | Flutter | Priority |
|----------|----------|---------|-----|---------|----------|
| Search Users | `GET /search/users` | Done | TODO | Done | P1 |
| Get User Profile | `GET /users/{username}` | Done | TODO | TODO | P1 |
| Get User Repositories | `GET /users/{username}/repos` | Done | TODO | Done | P1 |
| Search Repositories | `GET /search/repositories` | TODO | Done | TODO | P1 |

### APIs Available to Use (Not Yet Implemented)

| API Name | Endpoint | Use Case | Priority |
|----------|----------|----------|----------|
| Get Repository Details | `GET /repos/{owner}/{repo}` | Repository detail screen | P1 |
| Get Repository Issues | `GET /repos/{owner}/{repo}/issues` | Show repo issues | P2 |
| Get Repository Commits | `GET /repos/{owner}/{repo}/commits` | Show commit history | P2 |
| Get User Followers | `GET /users/{username}/followers` | User network | P2 |
| Get User Following | `GET /users/{username}/following` | User network | P2 |
| Get Public Events | `GET /events` | Activity feed | P3 |
| Get User Gists | `GET /users/{username}/gists` | User code snippets | P3 |
| Get Organization Repos | `GET /orgs/{org}/repos` | Organization view | P3 |

---

## Common Configuration

### Required Headers

```
X-GitHub-Api-Version: 2022-11-28
Accept: application/vnd.github+json
```

### Authentication (Optional)

```
Authorization: Bearer YOUR_GITHUB_TOKEN
```

### Rate Limits

- **Unauthenticated:** 60 requests/hour
- **Authenticated:** 5,000 requests/hour
- **Check rate limit:** `GET /rate_limit`

---

## Implemented APIs

### API 1: Search Users
**API ID:** API-1

**Endpoint:** `GET /search/users`


**Official Docs:** [GitHub Search Users](https://docs.github.com/en/rest/search/search?apiVersion=2022-11-28#search-users)

**Example:**
```
https://api.github.com/search/users?q=dinkar1708&page=1&per_page=30
```

**Used In:** User Search Screen

---

### API 2: Get User Profile
**API ID:** API-2

**Endpoint:** `GET /users/{username}`


**Official Docs:** [GitHub Get User](https://docs.github.com/en/rest/users/users?apiVersion=2022-11-28#get-a-user)

**Example:**
```
https://api.github.com/users/dinkar1708
```

**Used In:** User Profile Screen

---

### API 3: Get User Repositories
**API ID:** API-3

**Endpoint:** `GET /users/{username}/repos`


**Official Docs:** [GitHub List User Repos](https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-repositories-for-a-user)

**Example:**
```
https://api.github.com/users/dinkar1708/repos?page=1&per_page=100
```

**Used In:** User Profile Screen (repository list)

---

### API 4: Search Repositories
**API ID:** API-4

**Endpoint:** `GET /search/repositories`


**Official Docs:** [GitHub Search Repositories](https://docs.github.com/en/rest/search/search?apiVersion=2022-11-28#search-repositories)

**Example:**
```
https://api.github.com/search/repositories?q=swift&per_page=40&page=1
```

**Used In:** Repository Search Screen (iOS only, TODO for Android/Flutter)

---

## Available APIs (Not Yet Implemented)

### API 5: Get Repository Details
**API ID:** API-5

**Endpoint:** `GET /repos/{owner}/{repo}`


**Official Docs:** [GitHub Get Repository](https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#get-a-repository)

**Example:**
```
https://api.github.com/repos/dinkar1708/github-cruise-android
```

**Use Case:** Enhanced repository detail screen with README, topics, license

---

### API 6: Get Repository Issues
**API ID:** API-6

**Endpoint:** `GET /repos/{owner}/{repo}/issues`


**Official Docs:** [GitHub List Issues](https://docs.github.com/en/rest/issues/issues?apiVersion=2022-11-28#list-repository-issues)

**Example:**
```
https://api.github.com/repos/dinkar1708/github-cruise-android/issues?state=open
```

**Use Case:** Repository issues tab

---

### API 7: Get Repository Commits
**API ID:** API-7

**Endpoint:** `GET /repos/{owner}/{repo}/commits`


**Official Docs:** [GitHub List Commits](https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28#list-commits)

**Example:**
```
https://api.github.com/repos/dinkar1708/github-cruise-android/commits
```

**Use Case:** Repository commit history screen

---

### API 8: Get User Followers
**API ID:** API-8

**Endpoint:** `GET /users/{username}/followers`


**Official Docs:** [GitHub List Followers](https://docs.github.com/en/rest/users/followers?apiVersion=2022-11-28#list-followers-of-a-user)

**Example:**
```
https://api.github.com/users/dinkar1708/followers
```

**Use Case:** User profile - followers screen

---

### API 9: Get User Following
**API ID:** API-9

**Endpoint:** `GET /users/{username}/following`


**Official Docs:** [GitHub List Following](https://docs.github.com/en/rest/users/followers?apiVersion=2022-11-28#list-the-people-a-user-follows)

**Example:**
```
https://api.github.com/users/dinkar1708/following
```

**Use Case:** User profile - following screen

---

### API 10: Get Public Events
**API ID:** API-10

**Endpoint:** `GET /events`


**Official Docs:** [GitHub List Events](https://docs.github.com/en/rest/activity/events?apiVersion=2022-11-28#list-public-events)

**Example:**
```
https://api.github.com/events
```

**Use Case:** Activity feed / Home screen

---

### API 11: Get User Gists
**API ID:** API-11

**Endpoint:** `GET /users/{username}/gists`


**Official Docs:** [GitHub List Gists](https://docs.github.com/en/rest/gists/gists?apiVersion=2022-11-28#list-gists-for-a-user)

**Example:**
```
https://api.github.com/users/dinkar1708/gists
```

**Use Case:** User profile - gists tab

---

### API 12: Get Organization Repositories
**API ID:** API-12

**Endpoint:** `GET /orgs/{org}/repos`


**Official Docs:** [GitHub List Org Repos](https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-organization-repositories)

**Example:**
```
https://api.github.com/orgs/github/repos
```

**Use Case:** Organization repositories screen

---

### API 13: Get Repository Languages
**API ID:** API-13

**Endpoint:** `GET /repos/{owner}/{repo}/languages`


**Official Docs:** [GitHub List Languages](https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-repository-languages)

**Example:**
```
https://api.github.com/repos/dinkar1708/github-cruise-android/languages
```

**Use Case:** Repository detail - language statistics

---

### API 14: Check Rate Limit
**API ID:** API-14

**Endpoint:** `GET /rate_limit`


**Official Docs:** [GitHub Rate Limit](https://docs.github.com/en/rest/rate-limit/rate-limit?apiVersion=2022-11-28#get-rate-limit-status-for-the-authenticated-user)

**Example:**
```
https://api.github.com/rate_limit
```

**Use Case:** Settings screen - API usage stats

---

## Additional Resources

- **GitHub REST API Docs:** https://docs.github.com/en/rest
- **API Status:** https://www.githubstatus.com/
- **Best Practices:** https://docs.github.com/en/rest/guides/best-practices-for-using-the-rest-api

---

*End of Document*
