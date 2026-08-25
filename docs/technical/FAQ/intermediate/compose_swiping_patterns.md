# Modern Jetpack Compose Swiping & Paging Patterns Guide (100% XML-Free)

**Staff-Level Architecture Guide: 3D Cube Transformers, Centered Card Snapping, Tinder-Style Physics Decks, and Adaptive Hero Carousels**

📖 **Related Code Implementation**: [`ComposeSwipingPatternsSampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ComposeSwipingPatternsSampleScreen.kt)

---

## 🧭 Master Architecture Matrix: Top 4 Modern Compose Swiping Patterns

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MODERN JETPACK COMPOSE SWIPING SUITE                     │
├───────────────────────┬─────────────────────────┬───────────────────────────┤
│ 1. 3D CUBE & DEPTH    │ 2. CENTERED CARD SNAP   │ 3. TINDER SWIPE DECK      │
│  - HorizontalPager +  │  - LazyRow +            │  - detectDragGestures +   │
│    graphicsLayer math │    SnapFlingBehavior    │    Animatable Physics     │
├───────────────────────┴─────────────────────────┴───────────────────────────┤
│ 4. ADAPTIVE HERO CAROUSEL (Dynamic Scale & Alpha Focus)                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

| Pattern | Backing Compose API | Memory Strategy | Primary Android Use Case |
| :--- | :--- | :--- | :--- |
| **1. 3D Cube / Depth Pager** | `HorizontalPager` + `graphicsLayer` | `beyondViewportPageCount = 1` (3-page window) | Reader apps, magazines, onboarding heroes, story readers |
| **2. Centered Card Snapping** | `LazyRow` + `rememberSnapFlingBehavior` | LazyLayout item recycling | Bank/credit card selectors, Netflix trays, media carousels |
| **3. Tinder Physics Deck** | `pointerInput` + `detectDragGestures` + `Animatable` | In-memory queue / StateList | Card swiping (Pass/Like), dismissible cards, match feeds |
| **4. Adaptive Hero Carousel** | `HorizontalPager` + `lerp()` scaling | Subcomposition sliding window | Editorial news feeds, featured products, banner highlights |

---

## 🛠️ Pattern 1: 3D Cube & Depth Page Transformer (`HorizontalPager` + `graphicsLayer`)

Replaces legacy `ViewPager2.PageTransformer` with **zero XML** and **zero Fragment overhead**. Offloads 3D matrix operations directly to the GPU render thread without triggering recomposition!

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThreeDimensionalPager(items: List<PageItem>) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size })
    val density = LocalDensity.current

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1, // Retains 3 pages in RAM: Active + Left + Right
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.fillMaxWidth().height(280.dp)
    ) { page ->
        // Compute precise fractional offset from current page
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)

        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 🚀 GPU-Accelerated 3D Cube Rotation Math:
                    cameraDistance = 12 * density.density
                    rotationY = pageOffset * -45f
                    alpha = lerp(
                        start = 0.4f,
                        stop = 1f,
                        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                    )
                }
        ) {
            PageContent(items[page])
        }
    }
}
```

---

## 🛠️ Pattern 2: Centered Card Snapping Carousel (`LazyRow` + `rememberSnapFlingBehavior`)

The industry standard for **credit card pickers, onboarding wizard steps, and horizontal media trays** where items are narrower than the screen width.

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CenteredCardSnappingCarousel(cards: List<CreditCard>) {
    val lazyListState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

    LazyRow(
        state = lazyListState,
        flingBehavior = snapFlingBehavior,
        contentPadding = PaddingValues(horizontal = 48.dp), // 👈 Creates preview peek for adjacent cards
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(cards) { index, card ->
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .height(160.dp)
            ) {
                CreditCardView(card)
            }
        }
    }
}
```

---

## 🛠️ Pattern 3: Tinder-Style Swipeable Cards (Drag Gestures + Physics Spring)

Uses `pointerInput` with `detectDragGestures` and `Animatable` to deliver 60/120 FPS gesture physics with spring snap-back:

```kotlin
@Composable
fun SwipeableCardDeck(
    card: TechCard,
    onSwipedRight: () -> Unit,
    onSwipedLeft: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(240.dp)
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                // Dynamic angular tilt proportional to drag distance
                rotationZ = (offsetX.value / 400f) * 15f
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetX.value > 250f) {
                                // Swiped Right (Like)
                                offsetX.animateTo(1000f, spring(stiffness = Spring.StiffnessMedium))
                                onSwipedRight()
                            } else if (offsetX.value < -250f) {
                                // Swiped Left (Pass)
                                offsetX.animateTo(-1000f, spring(stiffness = Spring.StiffnessMedium))
                                onSwipedLeft()
                            } else {
                                // Physics Snap Back
                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    }
                )
            }
    ) {
        CardContent(card)
    }
}
```

---

## 🛠️ Pattern 4: Adaptive Hero Carousel with Dynamic Scale & Alpha Focus

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdaptiveHeroCarousel(articles: List<Article>) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { articles.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 48.dp),
        pageSpacing = 12.dp,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) { page ->
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
        val scale = lerp(0.9f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
        val alpha = lerp(0.6f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            HeroArticleContent(articles[page])
        }
    }
}
```

---

## 🔗 Related Documentation & Navigation

- **Interactive Sample Screen**: [`ComposeSwipingPatternsSampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ComposeSwipingPatternsSampleScreen.kt)
- **HorizontalPager vs ViewPager**: [`horizontal_pager_vs_viewpager.md`](horizontal_pager_vs_viewpager.md)
- **MultiTab Dynamic Feed**: [`MultiTabFeedSampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/MultiTabFeedSampleScreen.kt)
