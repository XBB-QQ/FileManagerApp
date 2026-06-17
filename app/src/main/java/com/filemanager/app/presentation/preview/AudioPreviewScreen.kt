package com.filemanager.app.presentation.preview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Audio preview screen with playback controls, progress bar, and time display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPreviewScreen(
    audioPath: String,
    onBack: () -> Unit = {}
) {
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }

    val ctx = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(audioPath) {
        val newPlayer = ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(audioPath))
            prepare()
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            duration = this@apply.duration
                        }
                        Player.STATE_ENDED -> {
                            isPlaying = false
                            currentPosition = 0L
                        }
                    }
                }
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
        player = newPlayer
    }

    // Poll progress when not dragging
    if (!isDragging && player != null) {
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (isPlaying && player != null) {
                    currentPosition = player?.currentPosition ?: 0L
                    kotlinx.coroutines.delay(250L)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("音频播放") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Album art placeholder
            Icon(
                Icons.Default.Headphones,
                "Audio",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Now playing text
            Text(
                "正在播放",
                style = MaterialTheme.typography.titleMedium
            )

            // Progress bar with time labels
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() else 0f,
                    onValueChange = {
                        if (player != null && duration > 0) {
                            currentPosition = it.toLong()
                        }
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        player?.seekTo(currentPosition)
                    },
                    valueRange = if (duration > 0) 0f..duration.toFloat() else 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatTime(currentPosition),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        formatTime(duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            // Playback controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop button
                IconButton(
                    onClick = {
                        player?.stop()
                        isPlaying = false
                        currentPosition = 0L
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        "Stop",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Pause button (larger)
                IconButton(
                    onClick = {
                        player?.let { p ->
                            if (isPlaying) {
                                p.pause()
                            } else {
                                p.play()
                            }
                        }
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Play/Pause",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0L) return "0:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
