# Journey 6: View Repository Details

**Test File:** `Journey6_ViewRepositoryDetailsTest.kt`

---

## User Flow

User views repository list → Clicks on repository → Sees WebView with GitHub URL

---

## What We Test

1. Repository item is clickable
2. WebView opens for repository
3. GitHub URL loads in WebView
4. Loading indicator during page load
5. Back button works from WebView

---

## Test Cases

### Test 1: Repository Item Is Clickable
**What it does:** Verifies repository items can be clicked

**Steps:**
1. Navigate to profile
2. Click on a repository item
3. Verify click is registered

**Expected:** Repository item responds to click

---

### Test 2: WebView Opens For Repository
**What it does:** Verifies WebView screen opens after clicking repo

**Steps:**
1. Click on repository item
2. Wait for navigation
3. Verify WebView screen appears

**Expected:** WebView opens with repository

---

### Test 3: GitHub URL Loads In WebView
**What it does:** Verifies correct GitHub URL is loaded

**Steps:**
1. Click on repository
2. Check WebView URL
3. Verify it matches repository URL

**Expected:** Correct GitHub URL loads

---

### Test 4: Loading Indicator During Page Load
**What it does:** Verifies loading state shows while page loads

**Steps:**
1. Click on repository
2. Observe loading indicator
3. Wait for page to load

**Expected:** Loading indicator appears during load

---

### Test 5: Back Button Works From WebView
**What it does:** Verifies navigation back from WebView

**Steps:**
1. Open repository in WebView
2. Press back button
3. Verify return to profile screen

**Expected:** Back button navigates to previous screen

---

## Results

**Status:** ✅ All 5 tests passing

**What you see on simulator:**
- Repository items are clickable
- WebView opens smoothly
- GitHub page loads
- Loading indicators appear
- Back navigation works

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey6_ViewRepositoryDetailsTest.kt
```
