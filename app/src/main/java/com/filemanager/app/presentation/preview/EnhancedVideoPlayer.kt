package com.filemanager.app.presentation.preview

import android.app.Activity
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Enhanced video player with playback controls, fullscreen, brightness/volume gestures.
 */
@Composable
fun EnhancedVideoPlayer(
    videoPath: String,
    onBack: () -> Unit = {}
) {
    var isFullscreen by remember { mutableStateOf(false) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    if (isFullscreen) {
        BackHandler { isFullscreen = false }
        FullscreenVideoPlayer(
            videoPath = videoPath,
            onExit = { isFullscreen = false },
            onBack = onBack,
            onIsPlayingChanged = { isPlaying = it },
            onPositionChanged = { pos -> currentTime = pos },
            onDurationChanged = { duration = it }
        )
    } else {
        val ctx = androidx.compose.ui.platform.LocalContext.current
        var playerRef by remember { mutableStateOf<ExoPlayer?>(null) }
        var playerRefView by remember { mutableStateOf<PlayerView?>(null) }

        LaunchedEffect(videoPath) {
            val player = ExoPlayer.Builder(ctx).build().apply {
                setMediaItem(MediaItem.fromUri(videoPath))
                prepare()
            }
            playerRef = player
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        duration = player.duration
                    }
                }
            })
        }

        DisposableEffect(Unit) {
            onDispose {
                playerRef?.release()
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("视频播放") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isFullscreen = true }) {
                            Icon(Icons.Default.Fullscreen, "Fullscreen")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
                    .clickable { isControlsVisible = !isControlsVisible }
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = playerRef
                            this.player?.playWhenReady = true
                            useController = true
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }.also { playerRefView = it }
                    },
                    update = { view ->
                        view.player = playerRef
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isControlsVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "轻触隐藏控件",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen video player with custom controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullscreenVideoPlayer(
    videoPath: String,
    onExit: () -> Unit,
    onBack: () -> Unit,
    onIsPlayingChanged: (Boolean) -> Unit,
    onPositionChanged: (Long) -> Unit,
    onDurationChanged: (Long) -> Unit
) {
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isControlsVisible by remember { mutableStateOf(true) }

    var brightness by remember { mutableStateOf(-1f) }
    var volume by remember { mutableStateOf(-1f) }
    var showGestureOverlay by remember { mutableStateOf(false) }
    var gestureType by remember { mutableStateOf(GestureType.NONE) }
    var gestureProgress by remember { mutableStateOf(0f) }
    val ctx = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(videoPath) {
        player = ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(videoPath))
            prepare()
            playWhenReady = true
        }
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                onIsPlayingChanged(playing)
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = player!!.duration
                    onDurationChanged(duration)
                }
            }
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                currentTime = newPosition.positionMs
                onPositionChanged(currentTime)
            }
        })

        kotlinx.coroutines.delay(500)
        player?.let { p ->
            if (p.isPlaying) {
                currentTime = p.currentPosition
                onPositionChanged(currentTime)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
        }
    }

    // Set fullscreen window flags
    LaunchedEffect(Unit) {
        (ctx as? Activity)?.window?.let { window ->
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { center, pan, zoom, rotation ->
                    if (rotation != 0f || zoom != 1f) return@detectTransformGestures

                    val xDistance = center.x - size.width / 2f
                    if (xDistance > 0) {
                        gestureType = GestureType.VOLUME
                        volume = (volume + pan.y * 0.005f).coerceIn(-1f, 1f)
                        showGestureOverlay = true
                    } else {
                        gestureType = GestureType.BRIGHTNESS
                        brightness = (brightness + pan.y * 0.005f).coerceIn(-1f, 1f)
                        showGestureOverlay = true
                    }
                    gestureProgress = if (gestureType == GestureType.BRIGHTNESS) brightness else volume
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isControlsVisible = !isControlsVisible },
                    onLongPress = {
                        player?.seekTo(currentTime + 10000)
                    }
                )
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).also {
                    it.player = player
                    it.useController = true
                }
            },
            update = { view ->
                view.player = player
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isControlsVisible) {
            Column {
                TopAppBar(
                    title = { Text("视频播放") },
                    navigationIcon = {
                        IconButton(onClick = {
                            isControlsVisible = false
                            onExit()
                        }) {
                            Icon(Icons.Default.Close, "Exit", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            player?.let {
                                if (isPlaying) it.pause() else it.play()
                            }
                        }) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                "Play/Pause",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }

        if (!isPlaying) {
            Icon(
                Icons.Default.PlayArrow,
                "Play",
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.Center),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        if (showGestureOverlay) {
            val (icon, label) = when (gestureType) {
                GestureType.BRIGHTNESS -> Icons.Default.BrightnessHigh to "亮度"
                GestureType.VOLUME -> Icons.Default.VolumeUp to "音量"
                GestureType.NONE -> Icons.Default.Info to ""
            }
            val progress = gestureProgress

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, label, modifier = Modifier.size(40.dp), tint = Color.White)
                Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = (progress + 1f) / 2f,
                    onValueChange = {},
                    modifier = Modifier
                        .width(120.dp)
                        .padding(top = 8.dp)
                )
            }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500)
                showGestureOverlay = false
            }
        }

        Text(
            text = formatTime(currentTime) + " / " + formatTime(duration),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

enum class GestureType { NONE, BRIGHTNESS, VOLUME }

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
