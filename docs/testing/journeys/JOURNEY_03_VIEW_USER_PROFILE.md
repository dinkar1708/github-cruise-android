# Journey 3: View User Profile

**Test File:** `Journey3_ViewUserProfileTest.kt`

---

## User Flow

User searches → Taps user from list → Views profile

---

## What We Test

1. User can search for users
2. User item is clickable in list
3. Navigation to profile screen works
4. Profile screen loads without crash

---

## Test Cases

### Test 1: User Can Navigate To Profile
**What it does:** Verifies complete search-to-profile flow

**Steps:**
1. Wait for search screen
2. Search for "dinkar"
3. Wait for results (2 seconds)
4. Tap first user in list
5. Verify profile loads

**Expected:** Profile screen opens without crash

---

### Test 2: Profile Screen Loads After Tap
**What it does:** Verifies profile loads after tapping user

**Steps:**
1. Navigate to search screen
2. Search for "android"
3. Wait for results
4. Tap first user item
5. Wait for profile load (1.5 seconds)

**Expected:** Profile screen appears

---

### Test 3: Complete Search To Profile Flow
**What it does:** End-to-end test of full user flow

**Steps:**
1. Launch app (wait for splash)
2. Search for "github"
3. Wait for results
4. Tap first result
5. View profile

**Expected:** Complete flow works without crash

---

## Results

**Status:** ⚠️ Needs app rebuild

**What you'll see on simulator:**
- App launches
- Types in search field
- Waits for results
- **Taps on user item** (you'll see the tap!)
- Navigates to profile screen
- Full user journey in action!

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey3_ViewUserProfileTest.kt
```

---

## Next Step

Rebuild app to include testTag:
```bash
./gradlew clean assembleDebug
./gradlew connectedDebugAndroidTest
```
