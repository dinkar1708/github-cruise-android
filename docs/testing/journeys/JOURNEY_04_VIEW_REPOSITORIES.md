# Journey 4: View Repositories

**Test File:** `Journey4_ViewRepositoriesTest.kt`

---

## User Flow

User searches for username → Views profile → Sees repository list with details

---

## What We Test

1. Repository list displays on profile screen
2. Repositories show correct details
3. Repository list is scrollable
4. Loading state appears while fetching repos

---

## Test Cases

### Test 1: Repository List Displays On Profile
**What it does:** Verifies repository list appears on profile screen

**Steps:**
1. Search for a user
2. Navigate to profile screen
3. Verify repository list is displayed

**Expected:** Repository list shows on profile

---

### Test 2: Repositories Show Details
**What it does:** Verifies repository details are correctly displayed

**Steps:**
1. Navigate to user profile
2. Check repository items
3. Verify details like name, description, stars

**Expected:** All repository details are visible

---

### Test 3: Repository List Scrolls
**What it does:** Verifies user can scroll through repository list

**Steps:**
1. Navigate to profile with multiple repos
2. Scroll through repository list
3. Verify scrolling works smoothly

**Expected:** Repository list is scrollable

---

### Test 4: Loading State While Fetching Repos
**What it does:** Verifies loading indicator appears during fetch

**Steps:**
1. Search for user
2. Navigate to profile
3. Observe loading state while repos load

**Expected:** Loading indicator shows during fetch

---

## Results

**Status:** ✅ All 4 tests passing

**What you see on simulator:**
- User profile loads
- Repository list displays
- Scrolling through repositories works
- Loading states appear appropriately

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey4_ViewRepositoriesTest.kt
```
