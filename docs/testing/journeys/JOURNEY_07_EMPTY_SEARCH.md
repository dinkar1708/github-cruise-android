# Journey 7: Empty Search

**Test File:** `Journey7_EmptySearchTest.kt`

---

## User Flow

User on search screen → No input provided → Sees empty state message

---

## What We Test

1. Empty search shows message
2. Empty state message is helpful
3. No API call for empty search
4. User can type after empty state
5. Clearing search returns to empty state
6. Short query does not trigger search

---

## Test Cases

### Test 1: Empty Search Shows Message
**What it does:** Verifies empty state message appears

**Steps:**
1. Launch app to search screen
2. Do not enter any input
3. Verify empty state message displays

**Expected:** Empty state message is visible

---

### Test 2: Empty State Message Is Helpful
**What it does:** Verifies message provides guidance to user

**Steps:**
1. View empty state
2. Read message content
3. Verify it guides user action

**Expected:** Message is clear and helpful

---

### Test 3: No API Call For Empty Search
**What it does:** Verifies no network request on empty input

**Steps:**
1. Stay on search screen with no input
2. Verify no API call is made
3. Check network activity

**Expected:** No API call without input

---

### Test 4: User Can Type After Empty State
**What it does:** Verifies input field remains functional

**Steps:**
1. View empty state
2. Click on search field
3. Type username

**Expected:** Input field accepts text

---

### Test 5: Clearing Search Returns To Empty State
**What it does:** Verifies clearing input shows empty state again

**Steps:**
1. Enter search query
2. Clear the input
3. Verify empty state returns

**Expected:** Empty state reappears after clearing

---

### Test 6: Short Query Does Not Trigger Search
**What it does:** Verifies minimum query length requirement

**Steps:**
1. Enter very short query
2. Verify no search is triggered
3. Check for validation

**Expected:** Short queries do not trigger search

---

## Results

**Status:** ✅ All 6 tests passing

**What you see on simulator:**
- Empty state message displays
- No unnecessary API calls
- Input field remains responsive
- Validation prevents short queries

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey7_EmptySearchTest.kt
```
