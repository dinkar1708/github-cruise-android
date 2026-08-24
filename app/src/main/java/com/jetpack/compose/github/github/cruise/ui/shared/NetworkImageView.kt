package com.jetpack.compose.github.github.cruise.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.jetpack.compose.github.github.cruise.BuildConfig
import com.jetpack.compose.github.github.cruise.ui.theme.AppShapes
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import timber.log.Timber

/**
 * Reusable network image component with proper loading and error handling
 *
 * @param imageUrl URL of the image to load
 * @param contentDescription Accessibility description
 * @param modifier Modifier to be applied
 * @param contentScale How to scale the image within bounds
 * @param shape Shape to clip the image (defaults to circular avatar)
 * @param showCacheIndicator Show visual indicator for cache source (Debug only)
 *
 * Design principles:
 * - Uses Coil for efficient image loading and caching
 * - L1 Cache (Memory): 25% of available memory for instant loading
 * - L2 Cache (Disk): 50 MB persistent storage for offline access
 * - Visual cache indicators in debug builds
 * - Proper content descriptions for accessibility
 * - Customizable shape for different use cases
 * - Fallback background color during loading
 *
 * Cache Behavior:
 * - 🟢 Green badge: Image from L1 (Memory Cache) - Fastest!
 * - 🟡 Yellow badge: Image from L2 (Disk Cache) - Fast!
 * - 🔴 Red badge: Image from Network - Downloaded and cached
 */
@Composable
fun NetworkImageView(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = AppShapes.avatar,
    showCacheIndicator: Boolean = BuildConfig.DEBUG
) {
    val context = LocalContext.current
    var cacheSource by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        imageUrl?.let {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .listener(
                        onSuccess = { _, result ->
                            cacheSource = when (result.dataSource) {
                                coil.decode.DataSource.MEMORY_CACHE -> "L1"
                                coil.decode.DataSource.DISK -> "L2"
                                coil.decode.DataSource.NETWORK -> "NET"
                                coil.decode.DataSource.MEMORY -> "MEM"
                            }
                            Timber.d("Image loaded from $cacheSource: $imageUrl")
                        }
                    )
                    .build(),
                imageLoader = context.imageLoader,
                contentDescription = contentDescription,
                modifier = Modifier.clip(shape),
                contentScale = contentScale
            )

            // Show cache indicator badge in debug builds
            if (showCacheIndicator && cacheSource != null) {
                CacheIndicatorBadge(
                    cacheSource = cacheSource!!,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }
    }
}

/**
 * Visual indicator showing which cache tier served the image
 */
@Composable
private fun CacheIndicatorBadge(
    cacheSource: String,
    modifier: Modifier = Modifier
) {
    val (badgeColor, badgeText) = when (cacheSource) {
        "L1" -> Color(0xFF4CAF50) to "L1" // Green - Memory Cache (fastest)
        "L2" -> Color(0xFFFFC107) to "L2" // Yellow - Disk Cache (fast)
        "NET" -> Color(0xFFF44336) to "NET" // Red - Network (slow)
        "MEM" -> Color(0xFF2196F3) to "MEM" // Blue - Bitmap Pool
        else -> Color.Gray to "?"
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .background(badgeColor, shape = MaterialTheme.shapes.small)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeText,
            color = Color.White,
            fontSize = 9.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NetworkImageViewPreview() {
    GithubCruiseTheme {
        Surface {
            NetworkImageView(imageUrl = "", contentDescription = "desc")
        }
    }
}