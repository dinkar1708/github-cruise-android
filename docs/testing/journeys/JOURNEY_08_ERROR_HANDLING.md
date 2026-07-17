# Journey 8: Error Handling

**Test File:** `Journey8_ErrorHandlingTest.kt`

---

## User Flow

User searches for invalid username → Sees error message → Can retry search

---

## What We Test

1. Invalid user shows error message
2. Error message is user-friendly
3. Retry button exists on error
4. Retry button triggers new search
5. Error state clears on retry
6. Network error handled gracefully
7. App stays responsive during error

---

## Test Cases

### Test 1: Invalid User Shows Error Message
**What it does:** Verifies error appears for invalid username

**Steps:**
1. Enter invalid username
2. Trigger search
3. Verify error message displays

**Expected:** Error message appears

---

### Test 2: Error Message Is User Friendly
**What it does:** Verifies error message is clear and helpful

**Steps:**
1. Trigger error state
2. Read error message
3. Verify message clarity

**Expected:** Error message is understandable

---

### Test 3: Retry Button Exists On Error
**What it does:** Verifies retry option is available

**Steps:**
1. Trigger error state
2. Look for retry button
3. Verify button is visible

**Expected:** Retry button appears on error

---

### Test 4: Retry Button Triggers New Search
**What it does:** Verifies retry functionality works

**Steps:**
1. Trigger error state
2. Click retry button
3. Verify new search is triggered

**Expected:** Retry button initiates search

---

### Test 5: Error State Clears On Retry
**What it does:** Verifies error clears after successful retry

**Steps:**
1. Trigger error state
2. Retry with valid input
3. Verify error clears

**Expected:** Error message disappears on success

---

### Test 6: Network Error Handled Gracefully
**What it does:** Verifies network errors are caught and displayed

**Steps:**
1. Simulate network error
2. Attempt search
3. Verify error handling

**Expected:** Network error is handled gracefully

---

### Test 7: App Stays Responsive During Error
**What it does:** Verifies app remains functional after error

**Steps:**
1. Trigger error state
2. Interact with UI
3. Verify responsiveness

**Expected:** App remains responsive

---

## Results

**Status:** ✅ All 7 tests passing

**What you see on simulator:**
- Error messages display clearly
- Retry button appears
- App recovers from errors
- UI remains responsive

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey8_ErrorHandlingTest.kt
```
