package com.filemanager.app.presentation.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Video preview screen using ExoPlayer.
 */
@Composable
fun VideoPreviewScreen(
    videoPath: String,
    onBack: () -> Unit = {}
) {
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    val context = LocalContext.current

    LaunchedEffect(videoPath) {
        val newPlayer = ExoPlayer.Builder(context).build()
        newPlayer.setMediaItem(MediaItem.fromUri(videoPath))
        newPlayer.prepare()
        newPlayer.playWhenReady = true
        player = newPlayer
        playerView?.player = newPlayer
    }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).also { playerView = it }
            },
            update = { view ->
                view.player = player
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
