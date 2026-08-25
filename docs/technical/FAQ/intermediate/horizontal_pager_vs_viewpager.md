# HorizontalPager vs ViewPager vs ViewPager2: Android Paging Standards Guide

**Comprehensive Comparison of Horizontal Swiping & Pager Options Across Android's History and Modern Jetpack Compose**

---

## 🧭 Executive Summary: Is `HorizontalPager` the Standard?

**YES!** In modern Android development with **Jetpack Compose**, `androidx.compose.foundation.pager.HorizontalPager` (and `VerticalPager`) is the **official first-party Google standard**.

### 📊 Complete Evolution & Options Comparison

| Technology | UI Framework | Status (2026) | Backing Engine | Memory Strategy | Recommendation |
| :--- | :--- | :---: | :--- | :--- | :--- |
| **`HorizontalPager`** | **Jetpack Compose** | ⭐ **Current Standard** | Compose Foundation Subcompose | `beyondViewportPageCount = 1` | **Use for all modern Compose apps** |
| **`LazyRow` + `SnapFling`** | **Jetpack Compose** | ✅ Active (Alternative) | LazyLayout Item Provider | Standard Lazy Recycling | **Use for smaller carousels / card pickers** |
| **`ViewPager2`** | **XML / Android Views** | ✅ Active (Legacy XML) | `RecyclerView` + `FragmentStateAdapter` | `offscreenPageLimit = 1` | **Use only in legacy XML / View codebases** |
| **`Accompanist Pager`** | **Jetpack Compose** | 🪦 **Deprecated** | Early experimental Compose library | Migrated to Foundation | **Migrate to official `foundation.pager`** |
| **`ViewPager` (v1)** | **Legacy Android Support** | ❌ **Obsolete / Deprecated** | Custom ViewGroup + `PagerAdapter` | `setOffscreenPageLimit` | **Do NOT use (memory leak prone)** |

---

## 🔍 Detailed Breakdown of the Options

### Option 1: `HorizontalPager` (The Modern Standard ⭐)

Integrated directly into `androidx.compose.foundation.pager.HorizontalPager` (no external libraries needed).

```kotlin
val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabCount })

HorizontalPager(
    state = pagerState,
    beyondViewportPageCount = 1, // 3-page active window (Active + Left + Right)
    modifier = Modifier.fillMaxSize()
) { pageIndex ->
    CategoryTabContent(pageIndex)
}
```

#### Why it is the standard:
- **Zero XML, Zero Fragments:** Fully declarative Kotlin code without Fragment lifecycle complexity.
- **Direct Compose State Integration:** `pagerState.currentPage`, `pagerState.currentPageOffsetFraction`, and `pagerState.animateScrollToPage(index)`.
- **Memory Efficient:** Subcomposition keeps only active and pre-rendered pages in the layout node tree.
- **Synchronizes with Tabs:** Pairs cleanly with `ScrollableTabRow` or custom tab chips.

---

### Option 2: `LazyRow` with `rememberSnapFlingBehavior` (Carousel Alternative)

For **card carousels, horizontal lists, or product cards** (where items are smaller than full-screen pages):

```kotlin
val lazyListState = rememberLazyListState()
val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

LazyRow(
    state = lazyListState,
    flingBehavior = snapFlingBehavior,
    modifier = Modifier.fillMaxWidth()
) {
    items(cardList) { card ->
        CardItemView(card)
    }
}
```

* **When to use:** Onboarding slide cards, multi-card pickers, or horizontal media trays where multiple items are partially visible.
* **When NOT to use:** Full-screen tabbed navigation feeds (use `HorizontalPager` instead).

---

### Option 3: `ViewPager2` (XML / Android View System)

`ViewPager2` was introduced in 2019 to replace the buggy legacy `ViewPager`. It is built internally on top of `RecyclerView`.

```kotlin
// XML / View System Only:
class FeedPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 5
    override fun createFragment(position: Int): Fragment = CategoryFragment.newInstance(position)
}

// In Activity / Fragment:
val viewPager = findViewById<ViewPager2>(R.id.viewPager)
viewPager.adapter = FeedPagerAdapter(this)
viewPager.offscreenPageLimit = 1

TabLayoutMediator(tabLayout, viewPager) { tab, position ->
    tab.text = "Tab $position"
}.attach()
```

* **When to use:** In existing codebases that still use XML layouts and `Fragment` architectures.
* **In Compose:** Never wrap `ViewPager2` in an `AndroidView` wrapper; use native `HorizontalPager`.

---

### Option 4: Legacy `ViewPager` (v1) (❌ Never Use in New Code)

The original `androidx.viewpager.widget.ViewPager` from Android Support Library 2011:
- Did **not** support `RecyclerView` recycling.
- Suffered from deep Fragment lifecycle synchronization bugs (`FragmentPagerAdapter` vs `FragmentStatePagerAdapter`).
- Formally replaced by `ViewPager2` in 2019 and `HorizontalPager` in Compose.

---

## ⚡ Architectural Comparison: `HorizontalPager` vs `ViewPager2`

| Dimension | Compose `HorizontalPager` | XML `ViewPager2` |
| :--- | :--- | :--- |
| **Component Hierarchy** | Pure Composable Functions | `ViewGroup` / `RecyclerView` + `Fragments` |
| **Page State Retention** | `rememberSaveable` / StateFlow | `SavedStateRegistry` / `Bundle` in Fragment |
| **Page Transitions** | `pagerState.currentPageOffsetFraction` | `ViewPager2.PageTransformer` |
| **Memory Sliding Window** | `beyondViewportPageCount = 1` | `viewPager.offscreenPageLimit = 1` |
| **Tab Synchronization** | `pagerState.animateScrollToPage(i)` | `TabLayoutMediator` |
| **Performance Overhead** | Minimal (No Fragment transactions) | Medium (Fragment lifecycle overhead) |

---

## 📱 Implemented: Modern Jetpack Compose Swiping & Paging Suite (100% XML-Free)

All modern, zero-XML Compose swiping and paging patterns are fully implemented and testable in:
👉 **[`ComposeSwipingPatternsSampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ComposeSwipingPatternsSampleScreen.kt)** (Sample: *"Compose Swiping & Paging Patterns"*)
📖 **Deep Technical Guide**: [`compose_swiping_patterns.md`](compose_swiping_patterns.md)

### 1. 🌟 Material 3 Adaptive Hero Carousel
* **What it is:** Dynamic item resizing where the active item expands to large width and adjacent items peek at smaller widths.
* **Tested In:** Tab 4 ("Hero Carousel") of `ComposeSwipingPatternsSampleScreen`.
* **Code Blueprint:**
```kotlin
// TODO: Material 3 Adaptive Carousel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroCarouselSample(items: List<NewsArticle>) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { items.size },
        preferredItemWidth = 260.dp,
        itemSpacing = 8.dp,
        modifier = Modifier.fillMaxWidth().height(220.dp)
    ) { index ->
        val item = items[index]
        ArticleCard(article = item, modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge))
    }
}
```

---

### 2. 📋 TODO: 3D Cube & Depth Page Transformations in `HorizontalPager`
* **What it is:** Custom 3D page flip and depth animations using `graphicsLayer` math.
* **Best for:** Immersive reader transitions and magazine-style page turn effects.
* **Code Blueprint:**
```kotlin
// TODO: 3D Cube Page Turn Transformation
HorizontalPager(state = pagerState) { page ->
    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // 3D Cube Rotation math:
                cameraDistance = 8 * density
                rotationY = pageOffset * 90f
                alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 0.5f)
            }
    ) {
        CategoryFeedPage(page)
    }
}
```

---

### 3. 📋 TODO: Centered Card Snapping with `LazyRow` + `SnapLayoutInfoProvider`
* **What it is:** Lightweight card picker with centered snap alignment and edge peek padding.
* **Best for:** Credit card pickers, onboarding wizard steps, and horizontal story bubbles.
* **Code Blueprint:**
```kotlin
// TODO: Center-Snapped Card Carousel
val lazyListState = rememberLazyListState()
val snapBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

LazyRow(
    state = lazyListState,
    flingBehavior = snapBehavior,
    contentPadding = PaddingValues(horizontal = 48.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxWidth()
) {
    itemsIndexed(cards) { index, card ->
        CardItem(card)
    }
}
```

---

### 4. 📋 TODO: Gesture Dragging via `Modifier.anchoredDraggable()` (Custom Swipe Decks)
* **What it is:** Physics-based drag and swipe state machine in `androidx.compose.foundation.gestures`.
* **Best for:** Tinder-style swipe cards, swipe-to-dismiss feeds, or custom multi-anchor bottom sheets.
* **Code Blueprint:**
```kotlin
// TODO: Custom Drag Anchors
enum class DragValue { Start, Center, End }
val state = remember {
    AnchoredDraggableState(
        initialValue = DragValue.Center,
        positionalThreshold = { distance: Float -> distance * 0.5f },
        velocityThreshold = { 100f },
        snapAnimationSpec = spring(),
        decayAnimationSpec = exponentialDecay()
    )
}
```

---

### 5. 📋 TODO: Custom `SubcomposeLayout` for Physics Cover Flow
* **What it is:** Custom layout with mathematical placement curves for 3D iTunes-style Cover Flow.
* **Best for:** Album art browsers, custom circular wheel dials, or perspective skew lists.

---

## 🔗 Related References

- **Interactive Implementation**: [`MultiTabFeedSampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/MultiTabFeedSampleScreen.kt)
- **Feed Architecture**: [`02_OFFLINE_FIRST_FEED_SYSTEM_ARCHITECTURE.md`](../../../../outputs/system_design/02_architecture/OFFLINE_FIRST_FEED_SYSTEM_ARCHITECTURE.md)
- **Case Study**: [`SMARTNEWS_CASE_02_MULTITAB_DYNAMIC_FEED.md`](../../../../outputs/smartnews/SMARTNEWS_CASE_02_MULTITAB_DYNAMIC_FEED.md)

