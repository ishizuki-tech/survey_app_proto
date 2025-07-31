package com.negi.survey.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.negi.survey.R
import com.negi.survey.model.*
import com.negi.survey.vm.SurveyViewModel
import android.media.MediaPlayer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import android.graphics.BitmapFactory

// ...


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    vm: SurveyViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val visited by vm.visited.collectAsState()
    val answers by vm.answers.collectAsState()
    val qmap = vm.graph.questions
    val ok = vm.allVisitedAnswered()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_summary)) }
            )
        },
        bottomBar = {
            BottomAppBar {
                BackButton(onClick = onBack)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onFinish,
                    enabled = ok,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(stringResource(R.string.action_submit))
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ★ ここをdistinct()で重複排除
            visited.distinct().forEach { qid ->
                val q = qmap[qid] ?: return@forEach
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(q.titleRes),
                            style = MaterialTheme.typography.titleMedium
                        )
                        AnswerText(spec = q, value = answers[qid])
                    }
                }
            }
            if (!ok) {
                Text(
                    text = stringResource(R.string.msg_required),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AnswerText(
    spec: QuestionSpec,
    value: String?
) {
    val empty = stringResource(R.string.answer_empty)

    // 未入力・null・空欄は一律で
    if (value.isNullOrBlank()) {
        Text(
            text = empty,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    when (spec) {

        is FreeSpec -> Text(
            value,
            maxLines = 2,
            style = MaterialTheme.typography.bodyMedium
        )

        is SingleSpec, is SingleBranchSpec -> {
            // Optionのkeyからラベルを取得
            val opts = when (spec) {
                is SingleSpec -> spec.options
                is SingleBranchSpec -> spec.options
                else -> emptyList()
            }
            val label = opts.firstOrNull { it.key == value }
                ?.let { stringResource(it.labelRes) }
                ?: value
            Text(
                label,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        is YesNoSpec -> {
            val label = when (value) {
                spec.yesKey -> stringResource(spec.yesLabelRes)
                spec.noKey  -> stringResource(spec.noLabelRes)
                else        -> value
            }
            Text(
                label,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        is MultiQueueSpec -> {
            val labels = value.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { key -> spec.options.firstOrNull { it.key == key }?.labelRes }
                .map { res -> stringResource(res) }
                .joinToString(", ")
            Text(
                if (labels.isBlank()) empty else labels,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        is VoiceSpec -> {
            // 録音済みかどうか
            if (value.isNotBlank()) {
                var isPlaying by remember { mutableStateOf(false) }
                var lastError by remember { mutableStateOf<String?>(null) }
                val context = LocalContext.current
                val mediaPlayer = remember { MediaPlayer() }
                DisposableEffect(value) {
                    onDispose {
                        try {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.stop()
                            }
                        } catch (_: Exception) {}
                        mediaPlayer.release()
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "音声で回答済み",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    if (isPlaying) {
                        IconButton(onClick = {
                            try {
                                mediaPlayer.stop()
                                mediaPlayer.reset()
                                isPlaying = false
                            } catch (e: Exception) {
                                lastError = "停止に失敗: ${e.message}"
                                isPlaying = false
                            }
                        }) {
                            Icon(Icons.Default.Stop, contentDescription = "停止")
                        }
                    } else {
                        IconButton(onClick = {
                            try {
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(value)
                                mediaPlayer.setOnCompletionListener {
                                    isPlaying = false
                                }
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                isPlaying = true
                            } catch (e: Exception) {
                                lastError = "再生に失敗: ${e.message}"
                                isPlaying = false
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "再生")
                        }
                    }
                }
                if (lastError != null) {
                    Text(
                        text = lastError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text(
                    text = empty,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        is VideoSpec -> {
            if (value.isNotBlank()) {
                var isPlaying by remember { mutableStateOf(false) }
                var lastError by remember { mutableStateOf<String?>(null) }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "動画で回答済み",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { isPlaying = true }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "動画再生")
                    }
                }

                if (isPlaying && value.isNotBlank()) {
                    // 動画プレビューUI
                    AndroidView(
                        modifier = Modifier
                            .height(220.dp)
                            .fillMaxWidth()
                            .padding(8.dp),
                        factory = { ctx ->
                            android.widget.VideoView(ctx).apply {
                                try {
                                    setVideoURI(android.net.Uri.parse(value))
                                    setOnCompletionListener {
                                        // State更新はHandler経由で安全に
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            isPlaying = false
                                        }
                                    }
                                    start()
                                } catch (e: Exception) {
                                    lastError = "再生に失敗: ${e.message}"
                                }
                            }
                        },
                        update = { vv ->
                            if (!vv.isPlaying) {
                                try {
                                    vv.setVideoURI(android.net.Uri.parse(value))
                                    vv.start()
                                } catch (e: Exception) {
                                    lastError = "再生に失敗: ${e.message}"
                                }
                            }
                        }
                    )
                    Button(
                        onClick = { isPlaying = false },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("閉じる")
                    }
                }

                if (lastError != null) {
                    Text(
                        text = lastError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text(
                    text = empty,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        is CameraSpec -> {
            if (value.isNotBlank()) {
                var showPreview by remember { mutableStateOf(false) }
                var lastError by remember { mutableStateOf<String?>(null) }
                val context = LocalContext.current

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "写真で回答済み",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    // サムネイル
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPreview = true }
                    ) {
                        // content://...のUri画像をBitmapとしてデコード
                        val bitmap = remember(value) {
                            try {
                                val input = context.contentResolver.openInputStream(value.toUri())
                                BitmapFactory.decodeStream(input)
                            } catch (e: Exception) {
                                lastError = "画像取得失敗: ${e.message}"
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "回答画像サムネイル"
                            )
                        } else {
                            // 読み込みエラー用プレースホルダー
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // プレビュー拡大（ダイアログで大きく表示）
                if (showPreview) {
                    Dialog(onDismissRequest = { showPreview = false }) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            val bitmap = remember(value) {
                                try {
                                    val input = context.contentResolver.openInputStream(value.toUri())
                                    BitmapFactory.decodeStream(input)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "回答画像プレビュー",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(bitmap.width / bitmap.height.toFloat())
                                )
                            } else {
                                Text("画像を表示できません", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                if (lastError != null) {
                    Text(
                        text = lastError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text(
                    text = empty,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
