package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Modern Jetpack Compose Swiping & Paging Patterns (100% XML-Free)
 *
 * Demonstrates the top 4 actively used horizontal swiping & paging architectures in modern Android:
 * 1. 3D Cube & Depth Page Transformer (HorizontalPager + graphicsLayer)
 * 2. Centered Card Snapping Carousel (LazyRow + rememberSnapFlingBehavior)
 * 3. Tinder-Style Swipeable Cards (Custom Drag Gestures + Physics Spring)
 * 4. Adaptive Hero Carousel with Dynamic Size Masking
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ComposeSwipingPatternsSampleScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("3D Pager", "Card Snapping", "Swipe Deck", "Hero Carousel")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compose Swiping Suite") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Tab Selector
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> ThreeDimensionalPagerSection()
                    1 -> CenteredCardSnappingSection()
                    2 -> SwipeableCardDeckSection()
                    3 -> AdaptiveHeroCarouselSection()
                }
            }
        }
    }
}

// =============================================================================
// PATTERN 1: 3D Cube & Depth Page Transformer (HorizontalPager + graphicsLayer)
// =============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThreeDimensionalPagerSection() {
    val coroutineScope = rememberCoroutineScope()
    var transitionMode by remember { mutableStateOf("3D Cube") }
    val items = listOf(
        Triple("Jetpack Compose 1.7", "Declarative UI with Subcomposition", Brush.linearGradient(listOf(Color(0xFF1E88E5), Color(0xFF1565C0)))),
        Triple("Kotlin Coroutines 1.8", "Lightweight Structured Concurrency", Brush.linearGradient(listOf(Color(0xFF7B1FA2), Color(0xFF4A148C)))),
        Triple("WorkManager 2.9", "Guaranteed Persistent Background Sync", Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFBF360C)))),
        Triple("Material Design 3", "Dynamic Theming & Motion Standards", Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))))
    )
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size })
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = transitionMode == "3D Cube",
                onClick = { transitionMode = "3D Cube" },
                label = { Text("3D Cube Flip") }
            )
            FilterChip(
                selected = transitionMode == "Depth Zoom",
                onClick = { transitionMode = "Depth Zoom" },
                label = { Text("Depth / Zoom-Fade") }
            )
        }

        // Pager Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                val item = items[page]

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (transitionMode == "3D Cube") {
                                // 3D Cube Rotation Math
                                cameraDistance = 12 * density.density
                                rotationY = pageOffset * -45f
                                alpha = lerp(
                                    start = 0.4f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                                )
                            } else {
                                // Depth & Zoom Fade Math
                                val scale = lerp(0.85f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                                scaleX = scale
                                scaleY = scale
                                alpha = lerp(0.5f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                            }
                        },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(item.third)
                            .padding(24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("PAGE ${page + 1} OF ${items.size}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                            Text(item.first, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(item.second, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }
        }

        // Page Indicator Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        // Quick Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }
                },
                enabled = pagerState.currentPage > 0
            ) {
                Text("Previous")
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < items.size - 1) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                enabled = pagerState.currentPage < items.size - 1
            ) {
                Text("Next Page")
            }
        }

        // Architecture Explanation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("💡 How It Works:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("• HorizontalPager uses `beyondViewportPageCount = 1` for a 3-page memory sliding window.", fontSize = 12.sp)
                Text("• `pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction` computes smooth frame-by-frame interpolation.", fontSize = 12.sp)
                Text("• `graphicsLayer { rotationY = ... }` offloads matrix rotations directly to the GPU render thread without recomposition.", fontSize = 12.sp)
            }
        }
    }
}

// =============================================================================
// PATTERN 2: Centered Card Snapping Carousel (LazyRow + rememberSnapFlingBehavior)
// =============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CenteredCardSnappingSection() {
    val lazyListState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
    val cards = listOf(
        Pair("Platinum Card •••• 4821", Color(0xFF263238)),
        Pair("Sapphire Travel •••• 9920", Color(0xFF0D47A1)),
        Pair("Emerald Cash •••• 3012", Color(0xFF1B5E20)),
        Pair("Ruby Rewards •••• 7714", Color(0xFFB71C1C)),
        Pair("Gold Savings •••• 5510", Color(0xFFF57F17))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("💳 Swipe to Snap Cards (Centered Fling)", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        LazyRow(
            state = lazyListState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(cards) { index, card ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = card.second),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BANK CRUISE", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                        }
                        Text(card.first, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CARDHOLDER", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text("EXP 12/29", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Architecture Explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("💡 When to Use `LazyRow` + `SnapFlingBehavior`:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("• Best for horizontal card pickers, onboarding carousels, and Netflix-style trays where cards are smaller than full-screen width.", fontSize = 12.sp)
                Text("• `rememberSnapFlingBehavior()` ensures every swipe automatically snaps cleanly to the nearest item boundary.", fontSize = 12.sp)
                Text("• `contentPadding = PaddingValues(horizontal = 48.dp)` creates the classic preview peek effect for adjacent cards.", fontSize = 12.sp)
            }
        }
    }
}

// =============================================================================
// PATTERN 3: Tinder-Style Swipeable Cards (Drag Gestures + Physics Spring)
// =============================================================================
@Composable
private fun SwipeableCardDeckSection() {
    val coroutineScope = rememberCoroutineScope()
    var cards by remember {
        mutableStateOf(
            listOf(
                Triple("Android 15 Vanilla Ice Cream", "16KB Page Sizes & Private Space", Color(0xFF6750A4)),
                Triple("Compose Multiplatform 1.7", "Shared UI across iOS & Android", Color(0xFF006874)),
                Triple("Kotlin 2.1 K2 Compiler", "2x Faster Compilation Times", Color(0xFF984061))
            )
        )
    }
    var actionLog by remember { mutableStateOf("Swipe card left (Pass) or right (Like)") }

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔥 Swipeable Card Deck (Physics Gestures)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(actionLog, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cards.isNotEmpty()) {
                val topCard = cards.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(240.dp)
                        .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                        .graphicsLayer {
                            rotationZ = (offsetX.value / 400f) * 15f
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        if (offsetX.value > 250f) {
                                            // Swiped Right (Like)
                                            actionLog = "💚 Liked: ${topCard.first}"
                                            offsetX.animateTo(1000f, spring(stiffness = Spring.StiffnessMedium))
                                            cards = cards.drop(1)
                                            offsetX.snapTo(0f)
                                            offsetY.snapTo(0f)
                                        } else if (offsetX.value < -250f) {
                                            // Swiped Left (Pass)
                                            actionLog = "❌ Passed: ${topCard.first}"
                                            offsetX.animateTo(-1000f, spring(stiffness = Spring.StiffnessMedium))
                                            cards = cards.drop(1)
                                            offsetX.snapTo(0f)
                                            offsetY.snapTo(0f)
                                        } else {
                                            // Snap back to center
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
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = topCard.third),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TECH RADAR", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(topCard.first, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(topCard.second, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                        Text("← Swipe Left (Pass)  |  Swipe Right (Like) →", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎉 All cards reviewed!", fontWeight = FontWeight.Bold)
                    Button(onClick = {
                        cards = listOf(
                            Triple("Android 15 Vanilla Ice Cream", "16KB Page Sizes & Private Space", Color(0xFF6750A4)),
                            Triple("Compose Multiplatform 1.7", "Shared UI across iOS & Android", Color(0xFF006874)),
                            Triple("Kotlin 2.1 K2 Compiler", "2x Faster Compilation Times", Color(0xFF984061))
                        )
                        actionLog = "Deck reset!"
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Deck")
                    }
                }
            }
        }

        // Explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("💡 Physics Gesture Mechanics:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("• Uses `Animatable` offsets with `detectDragGestures` for 60/120 FPS gesture tracking.", fontSize = 12.sp)
                Text("• `Spring.DampingRatioMediumBouncy` creates natural physics snap-back when the swipe threshold isn't met.", fontSize = 12.sp)
                Text("• `graphicsLayer { rotationZ = ... }` adds angular tilt as the card travels outwards.", fontSize = 12.sp)
            }
        }
    }
}

// =============================================================================
// PATTERN 4: Adaptive Hero Carousel with Dynamic Size Masking
// =============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AdaptiveHeroCarouselSection() {
    val items = listOf(
        Pair("Breaking: Compose 1.7 Released", Color(0xFF004D40)),
        Pair("Deep Dive into WorkManager 2.9", Color(0xFF311B92)),
        Pair("Kotlin K2 Compiler Benchmark", Color(0xFF880E4F)),
        Pair("Staff System Design Blueprint", Color(0xFFE65100))
    )
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🌟 Adaptive Hero Item Carousel", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val scale = lerp(0.9f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
            val alpha = lerp(0.6f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
            val item = items[page]

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = item.second),
                elevation = CardDefaults.cardElevation(defaultElevation = if (pageOffset < 0.5f) 8.dp else 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("FEATURED STORY", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(item.first, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Architecture Explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("💡 Dynamic Hero Masking:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("• The active central item expands to 100% scale while neighboring items gracefully contract to 90% scale.", fontSize = 12.sp)
                Text("• Provides high-end editorial focus without requiring external XML ViewPager transformers.", fontSize = 12.sp)
            }
        }
    }
}
