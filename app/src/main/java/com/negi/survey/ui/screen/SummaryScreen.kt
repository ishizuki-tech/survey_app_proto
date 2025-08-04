/*
 * ui/screen/SummaryScreen.kt
 *
 * すべての回答を一覧表示する画面。
 * - LazyColumn でスクロール対応
 * - 各 QuestionSpec に応じて AnswerText を描画
 * - 送信ボタンは必須未回答があると無効
 */
package com.negi.survey.ui.screen

/* ────────── Compose 基本 ────────── */
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/* ────────── Android / Media ────────── */
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri

/* ────────── アプリ内 ────────── */
import com.negi.survey.R
import com.negi.survey.model.*
import com.negi.survey.vm.SurveyViewModel
import com.negi.survey.ui.components.BackButton

/* =============================================================
 *  メイン Composable
 * ========================================================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    vm: SurveyViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val visited  by vm.visited.collectAsState()
    val answers  by vm.answers.collectAsState()
    val qmap     = vm.graph.questions
    val allValid = vm.allVisitedAnswered()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_summary)) }) },
        bottomBar = {
            BottomAppBar {
                BackButton(onClick = onBack)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick  = onFinish,
                    enabled  = allValid,
                    modifier = Modifier.padding(8.dp)
                ) { Text(stringResource(R.string.action_submit)) }
            }
        }
    ) { innerPadding ->

        /* LazyColumn でスクロール表示 */
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) { /* items … */
            items(visited.distinct()) { qid ->
                val q = qmap[qid] ?: return@items
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text  = stringResource(q.titleRes),
                            style = MaterialTheme.typography.titleMedium
                        )
                        AnswerText(spec = q, value = answers[qid])
                    }
                }
            }

            /* 必須未回答がある場合に警告表示 */
            if (!allValid) {
                item {
                    Text(
                        text   = stringResource(R.string.msg_required),
                        color  = MaterialTheme.colorScheme.error,
                        style  = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

/* =============================================================
 *  回答表示ユーティリティ
 * ========================================================== */
@Composable
private fun AnswerText(
    spec: QuestionSpec,
    value: String?
) {
    val empty = stringResource(R.string.answer_empty)

    if (value.isNullOrBlank()) {
        Text(
            text  = empty,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    when (spec) {

        /* ---------- FreeSpec ---------- */
        is FreeSpec -> Text(
            text     = value,
            maxLines = 2,
            style    = MaterialTheme.typography.bodyMedium
        )

        /* ---------- Single / SingleBranch ---------- */
        is SingleSpec, is SingleBranchSpec -> {
            val opts = when (spec) {
                is SingleSpec        -> spec.options
                is SingleBranchSpec  -> spec.options
                else                 -> emptyList()
            }
            val label = opts.firstOrNull { it.key == value }
                ?.let { stringResource(it.labelRes) } ?: value
            Text(label, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }

        /* ---------- Yes / No ---------- */
        is YesNoSpec -> {
            val label = when (value) {
                spec.yesKey -> stringResource(spec.yesLabelRes)
                spec.noKey  -> stringResource(spec.noLabelRes)
                else        -> value
            }
            Text(label, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }

        /* ---------- MultiQueue ---------- */
        is MultiQueueSpec -> {
            val context = LocalContext.current
            val labels = value.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { key ->
                    spec.options.firstOrNull { it.key == key }?.labelRes
                }
                .joinToString(", ") { resId ->
                    context.getString(resId)      // ← ここは非 Composable
                }
            Text(if (labels.isBlank()) empty else labels,
                maxLines = 2,
                style    = MaterialTheme.typography.bodyMedium)
        }

        /* ---------- Voice ---------- */
        is VoiceSpec -> VoiceAnswer(value)

        /* ---------- Video ---------- */
        is VideoSpec -> VideoAnswer(value)

        /* ---------- Camera ---------- */
        is CameraSpec -> CameraAnswer(value)

    }
}

/* =============================================================
 *  Voice Answer Helper
 * ========================================================== */
@Composable
private fun VoiceAnswer(path: String) {
    var playing by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf<String?>(null) }
    val mp      = remember { MediaPlayer() }

    DisposableEffect(path) { onDispose { mp.release() } }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        Text("Recorded voice", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = {
            try {
                if (playing) {
                    mp.stop(); mp.reset(); playing = false
                } else {
                    mp.reset()
                    mp.setDataSource(path)
                    mp.setOnCompletionListener { playing = false }
                    mp.prepare(); mp.start(); playing = true
                }
            } catch (e: Exception) {
                error = e.localizedMessage; playing = false
            }
        }) {
            Icon(if (playing) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
        }
    }
    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

/* =============================================================
 *  Video Answer Helper
 * ========================================================== */
@Composable
private fun VideoAnswer(path: String) {
    var playing by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf<String?>(null) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        Text("Recorded video", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { playing = true }) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
        }
    }

    if (playing) {
        AndroidView(
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth()
                .padding(top = 8.dp),
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    try {
                        setVideoURI(Uri.parse(path))
                        setOnCompletionListener {
                            Handler(Looper.getMainLooper()).post { playing = false }
                        }
                        start()
                    } catch (e: Exception) {
                        error = e.localizedMessage
                    }
                }
            }
        )
        Button(
            onClick = { playing = false },
            modifier = Modifier.padding(top = 8.dp)
        ) { Text("Close") }
    }

    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

/* =============================================================
 *  Camera Answer Helper
 * ========================================================== */
@Composable
private fun CameraAnswer(path: String) {
    var preview by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        Text("Photo attached", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))

        /* サムネイル */
        val thumb = remember(path) {
            try {
                context.contentResolver.openInputStream(path.toUri())?.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                error = e.localizedMessage; null
            }
        }
        thumb?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { preview = true }
            )
        }
    }

    /* 拡大プレビュー */
    if (preview) {
        Dialog(onDismissRequest = { preview = false }) {
            val bmp = remember(path) {
                try {
                    context.contentResolver.openInputStream(path.toUri())?.use {
                        BitmapFactory.decodeStream(it)
                    }
                } catch (_: Exception) { null }
            }
            bmp?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(it.width / it.height.toFloat())
                )
            } ?: Text("Cannot load image", color = MaterialTheme.colorScheme.error)
        }
    }

    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}
