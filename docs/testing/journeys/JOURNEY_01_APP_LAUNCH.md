# Journey 1: App Launch

**Test File:** `Journey1_AppLaunchTest.kt`

---

## User Flow

User opens app → Sees splash screen → Auto-navigates to search screen

---

## What We Test

1. App launches without crashing
2. Splash screen displays for 3 seconds
3. App auto-navigates after splash
4. Search screen appears

---

## Test Cases

### Test 1: App Launches Without Crashing
**What it does:** Verifies app starts successfully

**Steps:**
1. Launch app
2. Wait for app to load
3. Verify no crash

**Expected:** App runs without crash

---

### Test 2: Splash Displays For 3 Seconds
**What it does:** Verifies splash screen shows and completes

**Steps:**
1. Launch app
2. Wait 3.5 seconds (3 seconds + buffer)
3. Verify splash completes

**Expected:** Splash finishes and navigates

---

### Test 3: Auto-Navigates After Splash
**What it does:** Verifies automatic navigation after splash

**Steps:**
1. Launch app with splash screen
2. Wait for auto-navigation (3 seconds)
3. Verify search screen appears

**Expected:** App navigates to search screen

---

## Results

**Status:** ✅ All 3 tests passing

**What you see on simulator:**
- App launches
- Splash screen appears
- Auto-navigates to search screen
- No interaction, just verification

---

## Code Location

```
app/src/androidTest/java/com/jetpack/compose/github/cruise/journeys/
└── Journey1_AppLaunchTest.kt
```
