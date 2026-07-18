# Cross-Platform Master Documentation

This folder contains **master specifications** that apply to all three GitHub API portfolio projects.

## Projects Covered

| Platform | Repository | URL |
|----------|-----------|-----|
| **Android** | github-cruise-android | [github.com/dinkar1708/github-cruise-android](https://github.com/dinkar1708/github-cruise-android) |
| **iOS** | github-repo-search-ios | [github.com/dinkar1708/github-repo-search-ios](https://github.com/dinkar1708/github-repo-search-ios) |
| **Flutter** | flutter_riverpod_template | [github.com/dinkar1708/flutter_riverpod_template](https://github.com/dinkar1708/flutter_riverpod_template) |

---

## Master Documents

### 1. MASTER_FEATURE_SPECIFICATION.md
**Purpose:** Complete feature inventory for all three platforms

**Contains:**
- Feature IDs (1.1, 1.2, 2.1, 3.0, etc.)
- Feature descriptions and requirements
- Implementation status per platform
- Links to API specifications

**Who Uses This:**
- All platform teams to understand complete feature set
- Check what features exist and what's planned
- Reference Feature IDs in tests, journeys, and documentation

**URL:** [MASTER_FEATURE_SPECIFICATION.md](./MASTER_FEATURE_SPECIFICATION.md)

---

### 2. GITHUB_API_SPECIFICATION.md
**Purpose:** Complete GitHub API reference for all platforms

**Contains:**
- API IDs (API-1, API-2, API-3, etc.)
- Endpoint documentation with examples
- Implementation status per platform
- Links to official GitHub API docs
- TODO list of available APIs not yet implemented

**Who Uses This:**
- Developers implementing new features
- Reference API IDs in code and documentation
- Check which APIs are available for use

**URL:** [GITHUB_API_SPECIFICATION.md](./GITHUB_API_SPECIFICATION.md)

---

### 3. BEST_PRACTICES.md
**Purpose:** Engineering standards and best practices for all platforms

**Contains:**
- Architecture patterns (MVVM + Clean Architecture)
- Code quality standards (70%+ test coverage)
- State management guidelines
- Error handling patterns
- Performance requirements

**Who Uses This:**
- All developers to follow consistent coding standards
- Code review reference
- Onboarding new team members

**URL:** [BEST_PRACTICES.md](./BEST_PRACTICES.md)

---

## How to Reference These Docs

### From Android Project (Local)
```markdown
See [MASTER_FEATURE_SPECIFICATION.md](docs/master/MASTER_FEATURE_SPECIFICATION.md)
```

### From iOS/Flutter Projects (GitHub URL)
```markdown
[MASTER_FEATURE_SPECIFICATION.md](https://github.com/dinkar1708/github-cruise-android/blob/main/docs/master/MASTER_FEATURE_SPECIFICATION.md)
```

---

## Management Guidelines

### When to Update Master Docs

**Update MASTER_FEATURE_SPECIFICATION.md when:**
- Adding a new feature that applies to all platforms
- Changing feature requirements
- Marking a feature as implemented in any platform

**Update GITHUB_API_SPECIFICATION.md when:**
- Adding a new API endpoint
- Implementing an existing TODO API
- Updating API documentation links

### Single Source of Truth

These documents are the **single source of truth** for cross-platform standards.

**Do:**
- ✅ Update these docs when adding/changing features
- ✅ Reference Feature IDs and API IDs in your code
- ✅ Link to these docs from project READMEs

**Don't:**
- ❌ Duplicate feature specs in individual project docs
- ❌ Create separate "master" docs in iOS/Flutter repos
- ❌ Skip updating when features change

---

## Version Control

All changes to master docs are tracked in git history.

**To see changes:**
```bash
git log docs/master/MASTER_FEATURE_SPECIFICATION.md
git diff HEAD~1 docs/master/MASTER_FEATURE_SPECIFICATION.md
```

---

*These master docs ensure consistent feature parity across all three platforms.*
