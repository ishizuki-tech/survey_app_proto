package com.negi.survey.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import com.negi.survey.ui.theme.AccentBlue
import com.negi.survey.ui.theme.BorderGray
import com.negi.survey.ui.theme.ErrorRed

/**
 * 音声録音／再生 UI をまとめたコンポーネント。
 * 画面側は「状態」と「コールバック」だけ渡せば OK。
 */
@Composable
fun VoiceRecorderField(
    audioPath: String?,
    isRecording: Boolean,
    isPlaying: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlay: () -> Unit,
    onStopPlay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape  = MaterialTheme.shapes.medium,
        color  = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            when {
                isRecording -> {
                    IconButton(onClick = onStopRecording) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = ErrorRed)
                    }
                    Text("録音中...", color = ErrorRed, modifier = Modifier.padding(start = 8.dp))
                }
                isPlaying -> {
                    IconButton(onClick = onStopPlay) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = AccentBlue)
                    }
                    Text("再生中", color = AccentBlue, modifier = Modifier.padding(start = 8.dp))
                }
                !audioPath.isNullOrBlank() -> {
                    IconButton(onClick = onPlay) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = AccentBlue)
                    }
                    Text("録音済み", color = AccentBlue, modifier = Modifier.padding(start = 8.dp))
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(onClick = onStartRecording) {
                        Icon(Icons.Filled.Mic, contentDescription = "Rerecord")
                        Spacer(Modifier.width(2.dp))
                        Text("再録音")
                    }
                    Spacer(Modifier.width(12.dp))
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = BorderGray)
                    }
                }
                else -> {
                    OutlinedButton(onClick = onStartRecording) {
                        Icon(Icons.Filled.Mic, contentDescription = "Record")
                        Spacer(Modifier.width(2.dp))
                        Text("録音する")
                    }
                }
            }
        }
    }
}
