# Journey 10: Back Navigation

**Test File:** `Journey10_BackNavigationTest.kt`

---

## User Flow

User navigates through screens → Presses back button → Returns to previous screen

---

## What We Test

1. Back from profile to search results
2. Back from repo details to profile
3. Back stack works correctly
4. State persists on back navigation
5. Multiple back presses work
6. System back button works

---

## Test Cases

### Test 1: Back From Profile To Search Results
**What it does:** Verifies back navigation from profile screen

**Steps:**
1. Navigate to profile screen
2. Press back button
3. Verify return to search results

**Expected:** Navigates back to search results

---

### Test 2: Back From Repo Details To Profile
**What it does:** Verifies back navigation from repository details

**Steps:**
1. Open repository details
2. Press back button
3. Verify return to profile

**Expected:** Returns to profile screen

---

### Test 3: Back Stack Works Correctly
**What it does:** Verifies back stack maintains proper order

**Steps:**
1. Navigate through multiple screens
2. Press back multiple times
3. Verify correct navigation order

**Expected:** Back stack maintains correct order

---

### Test 4: State Persists On Back Navigation
**What it does:** Verifies screen state is preserved after back

**Steps:**
1. Scroll or interact on screen
2. Navigate forward then back
3. Verify state is preserved

**Expected:** Screen state persists

---

### Test 5: Multiple Back Presses Work
**What it does:** Verifies consecutive back presses work correctly

**Steps:**
1. Navigate through multiple screens
2. Press back button multiple times
3. Verify navigation works smoothly

**Expected:** Multiple back presses navigate correctly

---

### Test 6: System Back Button Works
**What it does:** Verifies system back button integration

**Steps:**
1. Navigate to any screen
2. Use system back button
3. Verify navigation works

**Expected:** System back button works correctly

---

## Results

**Status:** ✅ All 6 tests passing

**What you see on simulator:**
- Back navigation works smoothly
- Screens appear in correct order
- State is preserved
- System integration works

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey10_BackNavigationTest.kt
```
