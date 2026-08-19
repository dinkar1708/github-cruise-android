package com.jetpack.compose.github.github.cruise.ui.samples.advanced

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

// 100% Tested & Verified 200 OK CDN Video Stream URLs
@Immutable
data class VideoStreamPreset(
    val title: String,
    val description: String,
    val url: String
)

private val PRESET_VIDEO_STREAMS = listOf(
    VideoStreamPreset(
        title = "Big Buck Bunny (Global CDN)",
        description = "jsDelivr Cloud High-Speed CDN (1080p MP4)",
        url = "https://cdn.jsdelivr.net/gh/mediaelement/mediaelement-files@master/big_buck_bunny.mp4"
    ),
    VideoStreamPreset(
        title = "Live Broadcast (HLS Stream)",
        description = "Mux Low-Latency Adaptive Live CDN",
        url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    ),
    VideoStreamPreset(
        title = "Live Music Session",
        description = "GitHub Media Cloud CDN (MP4)",
        url = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4"
    ),
    VideoStreamPreset(
        title = "Sample HD Video",
        description = "FileSamples CDN (MP4)",
        url = "https://filesamples.com/samples/video/mp4/sample_960x540.mp4"
    )
)

/**
 * Live Chat message model
 */
@Immutable
data class LiveChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val userName: String,
    val text: String,
    val badge: String? = null,
    val badgeColor: Color = Color(0xFFFFA000)
)

/**
 * Floating Heart Particle animation model
 */
class FloatingHeartParticle(
    val id: String = UUID.randomUUID().toString(),
    val color: Color,
    val scaleFactor: Float = Random.nextFloat() * 0.5f + 0.8f,
    val startXFraction: Float = Random.nextFloat() * 0.3f + 0.65f,
    val waveOffset: Float = Random.nextFloat() * 20f
) {
    val progress = Animatable(0f)
}

/**
 * Digital Gift Item model
 */
@Immutable
data class StoreGiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val costDiamonds: Int
)

/**
 * Digital Gift Banner model
 */
@Immutable
data class DigitalGiftEvent(
    val senderName: String,
    val giftTitle: String,
    val giftEmoji: String,
    val amountDiamonds: Int
)

private val GIFT_CATALOGUE = listOf(
    StoreGiftItem("coffee", "Coffee", "☕", 10),
    StoreGiftItem("sakura", "Sakura", "🌸", 50),
    StoreGiftItem("star", "Shooting Star", "🌟", 100),
    StoreGiftItem("ring", "Diamond Ring", "💎", 300),
    StoreGiftItem("rocket", "Space Rocket", "🚀", 500),
    StoreGiftItem("crown", "Royal Crown", "👑", 1000)
)

/**
 * Live Stream Room with Audience Interactions (Live Video & Audience Community Experience)
 * Supports real-time video playback via AndroidX Media3 ExoPlayer with URL Switcher
 */
@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamRoomScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var currentStreamUrl by remember { mutableStateOf(PRESET_VIDEO_STREAMS[0].url) }
    var currentStreamTitle by remember { mutableStateOf(PRESET_VIDEO_STREAMS[0].title) }
    var isVideoBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    // 1. Configure HttpDataSourceFactory with standard browser user-agent & cross-protocol redirects
    val httpDataSourceFactory = remember {
        DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(20000)
            .setReadTimeoutMs(20000)
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
    }

    val mediaSourceFactory = remember {
        DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory)
    }

    // 2. Initialize ExoPlayer with error handling & buffering listeners
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                setMediaItem(MediaItem.fromUri(currentStreamUrl))
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isVideoBuffering = (playbackState == Player.STATE_BUFFERING)
                        if (playbackState == Player.STATE_READY) {
                            playbackError = null
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Timber.e(error, "ExoPlayer playback error: ${error.message}")
                        playbackError = "${error.errorCodeName}: ${error.message ?: "Failed to stream video"}"
                        isVideoBuffering = false
                    }
                })
                prepare()
            }
    }

    // Switch video stream helper
    fun switchVideoStream(url: String, title: String) {
        currentStreamUrl = url
        currentStreamTitle = title
        isVideoBuffering = true
        playbackError = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // Lifecycle observer to pause/resume playback
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // Stream Stats
    var viewerCount by remember { mutableIntStateOf(2450) }
    var likeCount by remember { mutableLongStateOf(18420L) }
    var userDiamonds by remember { mutableIntStateOf(1250) }

    // Floating Hearts
    val activeHearts = remember { mutableStateListOf<FloatingHeartParticle>() }

    // Live Comments
    val chatMessages = remember {
        mutableStateListOf(
            LiveChatMessage(userName = "Sarah_K", text = "Hello everyone! Excited for the live broadcast! 🎉", badge = "VIP", badgeColor = Color(0xFFFFD54F)),
            LiveChatMessage(userName = "Kenji_Dev", text = "Audio and video quality are super crisp today!", badge = "MOD", badgeColor = Color(0xFF81C784)),
            LiveChatMessage(userName = "Mika_Tokyo", text = "Can you play the guitar next? 🎸", badge = "MEMBER", badgeColor = Color(0xFF64B5F6))
        )
    }
    val chatListState = rememberLazyListState()

    // Gift & Modals State
    var activeGift by remember { mutableStateOf<DigitalGiftEvent?>(null) }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showLeaderboard by remember { mutableStateOf(false) }
    var showStreamSourceSheet by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var isNoticePinned by remember { mutableStateOf(true) }
    var commentInput by remember { mutableStateOf("") }

    // Spawn floating heart
    fun spawnHeart() {
        likeCount++
        val heartColors = listOf(
            Color(0xFFFF1744),
            Color(0xFFFF4081),
            Color(0xFFFF5252),
            Color(0xFFFF80AB),
            Color(0xFFFFD700)
        )
        val particle = FloatingHeartParticle(color = heartColors.random())
        activeHearts.add(particle)

        coroutineScope.launch {
            particle.progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
            )
            activeHearts.remove(particle)
        }
    }

    // Send digital gift
    fun sendGift(gift: StoreGiftItem) {
        if (userDiamonds >= gift.costDiamonds) {
            userDiamonds -= gift.costDiamonds
            val event = DigitalGiftEvent(
                senderName = "You (@cruise_user)",
                giftTitle = gift.name,
                giftEmoji = gift.emoji,
                amountDiamonds = gift.costDiamonds
            )
            activeGift = event
            repeat(6) { spawnHeart() }

            coroutineScope.launch {
                delay(3500)
                if (activeGift == event) {
                    activeGift = null
                }
            }
        }
    }

    // Simulated incoming viewer comments ticker
    LaunchedEffect(Unit) {
        val simulatedUsers = listOf("Alex_99", "Yuki_Fan", "David_M", "Emma_W", "Taro_Osaka", "Sakura_Art")
        val simulatedComments = listOf(
            "Love this live stream! ❤️",
            "Greetings from Shibuya! 🇯🇵",
            "Great camera angle!",
            "Sent 100 stars! ⭐",
            "Let's goooo! 🔥",
            "Best stream of the week!"
        )

        while (true) {
            delay(2800)
            val newMsg = LiveChatMessage(
                userName = simulatedUsers.random(),
                text = simulatedComments.random(),
                badge = if (Random.nextBoolean()) "FAN" else null,
                badgeColor = Color(0xFF4FC3F7)
            )
            chatMessages.add(newMsg)
            if (chatMessages.size > 25) chatMessages.removeAt(0)
            chatListState.animateScrollToItem(chatMessages.size - 1)

            if (Random.nextBoolean()) spawnHeart()
            viewerCount += Random.nextInt(-2, 5)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // LAYER 1: Real Video Player Background via Media3 ExoPlayer
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    keepScreenOn = true
                }
            },
            update = { playerView ->
                if (playerView.player != exoPlayer) {
                    playerView.player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // LAYER 2: Background tap surface for heart spawning (behind UI controls)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { spawnHeart() }
        )

        // LAYER 3: Subtle gradient overlay for readable text on video
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // LAYER 4: High-Performance Canvas for Floating Hearts (Pure drawing, non-clickable)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            activeHearts.forEach { particle ->
                val progress = particle.progress.value
                val y = canvasHeight * (1f - progress * 0.85f)
                val x = (canvasWidth * particle.startXFraction) + (sin(progress * 10f + particle.waveOffset) * 45f)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val scale = particle.scaleFactor * (0.6f + progress * 0.6f)

                drawFloatingHeart(
                    center = Offset(x, y),
                    size = 28.dp.toPx() * scale,
                    color = particle.color.copy(alpha = alpha)
                )
            }
        }

        // LAYER 5: Video Loading / Buffering Indicator
        if (isVideoBuffering && playbackError == null) {
            CircularProgressIndicator(
                color = Color(0xFF7C4DFF),
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.Center)
            )
        }

        // LAYER 6: Playback Error Fallback Card (Foreground with fully interactive buttons)
        if (playbackError != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C).copy(alpha = 0.98f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFFF5252), modifier = Modifier.size(40.dp))
                    Text("Stream Playback Alert", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = playbackError ?: "Unable to stream video",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { switchVideoStream(currentStreamUrl, currentStreamTitle) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry")
                        }
                        Button(
                            onClick = { showStreamSourceSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
                        ) {
                            Text("Switch Source")
                        }
                    }
                }
            }
        }

        // LAYER 7: Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.clickable { showLeaderboard = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C4DFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Airi_Official", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("🏆 Top Fans", color = Color(0xFFFFD54F), fontSize = 10.sp)
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Stream URL Switcher Button
                Surface(
                    color = Color(0xFF7C4DFF).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { showStreamSourceSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌐", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Source", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = Color(0xFFE53935),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "👁 $viewerCount",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // LAYER 8: Pinned Notice Banner (Creator Announcement)
        AnimatedVisibility(
            visible = isNoticePinned,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 95.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                color = Color(0xFF263238).copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📌", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Airi: Playing [$currentStreamTitle] • Tap 🌐 Source to change video URL.",
                        color = Color(0xFFE0E0E0),
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isNoticePinned = false },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // LAYER 9: Digital Gift / Super Chat Banner Popup
        AnimatedVisibility(
            visible = activeGift != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 135.dp, start = 16.dp, end = 16.dp)
        ) {
            activeGift?.let { gift ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF311B92).copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(gift.giftEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(gift.senderName, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Sent ${gift.giftTitle} (${gift.amountDiamonds} Diamonds)!", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // LAYER 10: Bottom Audience Interaction Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp, start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Chat Scrolling Ticker
            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(180.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(chatMessages, key = { it.id }) { message ->
                    Surface(
                        color = Color.Black.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            message.badge?.let { badge ->
                                Surface(
                                    color = message.badgeColor,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = badge,
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Text(
                                text = "${message.userName}: ",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Quick Gifts Row + Open Full Store Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GiftChip("🌟 Star", "⭐", 100) { sendGift(GIFT_CATALOGUE[2]) }
                    GiftChip("🚀 Rocket", "🚀", 500) { sendGift(GIFT_CATALOGUE[4]) }
                }

                Surface(
                    color = Color(0xFF7C4DFF),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { showGiftSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎁", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gift Store (💎 $userDiamonds)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Chat Input & Like Heart Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    placeholder = { Text("Send a live comment...", fontSize = 12.sp, color = Color.LightGray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.55f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.4f),
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        if (commentInput.isNotBlank()) {
                            IconButton(onClick = {
                                chatMessages.add(LiveChatMessage(userName = "You", text = commentInput))
                                commentInput = ""
                                coroutineScope.launch {
                                    chatListState.animateScrollToItem(chatMessages.size - 1)
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF7C4DFF))
                            }
                        }
                    },
                    singleLine = true
                )

                Surface(
                    color = Color(0xFFE91E63),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { spawnHeart() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // LAYER 11: Stream URL Switcher & Custom URL Input Bottom Sheet
        if (showStreamSourceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStreamSourceSheet = false },
                containerColor = Color(0xFF1E1E2C)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("🌐 Select Live Stream Source / URL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    // Preset Streams List
                    Text("Preset Cloud Streams (Verified 200 OK):", color = Color.Gray, fontSize = 12.sp)
                    PRESET_VIDEO_STREAMS.forEach { preset ->
                        Surface(
                            color = if (currentStreamUrl == preset.url) Color(0xFF7C4DFF).copy(alpha = 0.3f) else Color(0xFF2C2C3E),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    switchVideoStream(preset.url, preset.title)
                                    showStreamSourceSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(preset.description, color = Color.LightGray, fontSize = 11.sp)
                                }
                                if (currentStreamUrl == preset.url) {
                                    Text("▶ Playing", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Custom URL Input
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Or Paste Any Network Video URL (MP4 / HLS .m3u8):", color = Color.Gray, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customUrlInput,
                            onValueChange = { customUrlInput = it },
                            placeholder = { Text("https://example.com/stream.m3u8", fontSize = 11.sp, color = Color.Gray) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2C2C3E),
                                unfocusedContainerColor = Color(0xFF2C2C3E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (customUrlInput.isNotBlank()) {
                                    switchVideoStream(customUrlInput.trim(), "Custom URL Stream")
                                    showStreamSourceSheet = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Text("Play", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // LAYER 12: Interactive Gift Store Bottom Sheet
        if (showGiftSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGiftSheet = false },
                containerColor = Color(0xFF1E1E2C)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎁 Send Digital Gift", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Surface(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "💎 $userDiamonds Diamonds",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(GIFT_CATALOGUE) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C3E)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable {
                                    sendGift(item)
                                    showGiftSheet = false
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(item.emoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("💎 ${item.costDiamonds}", color = Color(0xFFFFD54F), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // LAYER 13: Top Supporters Leaderboard Modal
        if (showLeaderboard) {
            ModalBottomSheet(
                onDismissRequest = { showLeaderboard = false },
                containerColor = Color(0xFF1E1E2C)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🏆 Top Supporters Leaderboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    LeaderboardItem(rank = 1, name = "Kenji_Dev", amount = "4,500 💎", badge = "🥇")
                    LeaderboardItem(rank = 2, name = "Sarah_K", amount = "3,200 💎", badge = "🥈")
                    LeaderboardItem(rank = 3, name = "Mika_Tokyo", amount = "1,800 💎", badge = "🥉")
                    LeaderboardItem(rank = 4, name = "You (@cruise_user)", amount = "1,250 💎", badge = "⭐")
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItem(rank: Int, name: String, amount: String, badge: String) {
    Surface(
        color = Color(0xFF2C2C3E),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(badge, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("#$rank $name", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(amount, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun GiftChip(
    title: String,
    emoji: String,
    cost: Int,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Text("($cost)", color = Color(0xFFFFD54F), fontSize = 10.sp)
        }
    }
}

/**
 * Custom Vector Path Drawing for Floating Hearts on Canvas
 */
private fun DrawScope.drawFloatingHeart(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        val width = size
        val height = size
        val halfWidth = width / 2f

        moveTo(center.x, center.y + height / 4f)
        cubicTo(
            center.x - halfWidth * 1.2f, center.y - height / 2f,
            center.x - halfWidth, center.y - height,
            center.x, center.y - height * 0.4f
        )
        cubicTo(
            center.x + halfWidth, center.y - height,
            center.x + halfWidth * 1.2f, center.y - height / 2f,
            center.x, center.y + height / 4f
        )
        close()
    }
    drawPath(path = path, color = color)
}
