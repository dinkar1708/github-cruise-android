# Journey 2: User Search

**Test File:** `Journey2_UserSearchTest.kt`

---

## User Flow

User enters search query → Views search results

---

## What We Test

1. Search input field accepts text
2. Search auto-triggers after typing
3. Results display after search
4. User list shows with search results

---

## Test Cases

### Test 1: User Can Type In Search Field
**What it does:** Verifies search input works

**Steps:**
1. Wait for search screen
2. Type "dinkar" in search field
3. Verify text appears

**Expected:** Text "dinkar" visible in search field

---

### Test 2: Search Auto-Triggers After Typing
**What it does:** Verifies auto-search (500ms debounce)

**Steps:**
1. Navigate to search screen
2. Type "android" (3+ characters)
3. Wait 600ms for auto-search
4. Verify search triggers

**Expected:** Search runs automatically without button click

---

### Test 3: User Can Search And See Results
**What it does:** Verifies complete search flow

**Steps:**
1. Navigate to search screen
2. Type "dinkar" in search
3. Wait 2 seconds for API response
4. Verify user list exists

**Expected:** User list component renders with results

---

### Test 4: Empty Search Shows Empty State
**What it does:** Verifies empty state handling

**Steps:**
1. Navigate to search screen
2. Leave search field empty
3. Verify no results shown

**Expected:** App handles empty state without crash

---

## Results

**Status:** ⚠️ Needs app rebuild

**What you'll see on simulator:**
- App launches and navigates to search
- Simulator types text in search field
- Search auto-triggers
- Results load (if API returns data)
- Real UI interaction!

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey2_UserSearchTest.kt
```

---

## Next Step

Rebuild app to include testTag:
```bash
./gradlew clean assembleDebug
./gradlew connectedDebugAndroidTest
```
