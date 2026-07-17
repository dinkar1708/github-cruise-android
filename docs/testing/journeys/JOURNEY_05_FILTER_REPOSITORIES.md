# Journey 5: Filter Repositories

**Test File:** `Journey5_FilterRepositoriesTest.kt`

---

## User Flow

User views profile → Toggles fork filter → Sees filtered repository list

---

## What We Test

1. Filter toggle exists on profile screen
2. Toggle changes filter state
3. Forked repos hidden when filtered
4. Filter works client-side

---

## Test Cases

### Test 1: Filter Toggle Exists
**What it does:** Verifies filter toggle is present on profile

**Steps:**
1. Navigate to user profile
2. Look for filter toggle
3. Verify toggle is visible

**Expected:** Filter toggle appears on profile

---

### Test 2: Toggle Changes Filter State
**What it does:** Verifies toggle interaction updates filter state

**Steps:**
1. Navigate to profile
2. Click filter toggle
3. Verify state changes

**Expected:** Toggle updates filter state

---

### Test 3: Forked Repos Hidden When Filtered
**What it does:** Verifies forked repositories are hidden when filter is active

**Steps:**
1. Navigate to profile with forked repos
2. Enable fork filter
3. Verify forked repos are hidden

**Expected:** Forked repos do not appear in list

---

### Test 4: Filter Works Client Side
**What it does:** Verifies filtering happens without API call

**Steps:**
1. Load profile with repositories
2. Toggle filter
3. Verify filtering is instant

**Expected:** Filter works without network request

---

## Results

**Status:** ✅ All 4 tests passing

**What you see on simulator:**
- Filter toggle appears on profile
- Clicking toggle updates UI
- Forked repositories hide/show
- Instant filtering response

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey5_FilterRepositoriesTest.kt
```
