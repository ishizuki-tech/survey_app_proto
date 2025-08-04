package com.negi.survey.ui.components

// ────────── Kotlin / Compose 基本 ──────────
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ────────── アプリ内モデル & Util ──────────
import com.negi.survey.R
import com.negi.survey.model.*
import com.negi.survey.ui.theme.*
import com.negi.survey.util.AudioController

/**
 * 質問種別ごとに UI を切り替えて描画するメイン Composable。
 *
 * @param spec           質問のメタ情報
 * @param answer         現在の回答値
 * @param onAnswer       回答が更新されたときに呼ばれる
 * @param onBranchToId   SingleBranchSpec 等で分岐先を指定するときに呼ばれる
 */
@Composable
fun QuestionContent(
    spec: QuestionSpec,
    answer: String,
    onAnswer: (String) -> Unit,
    onBranchToId: (String) -> Unit
) {
    when (spec) {

        /* ──────────────── 写真撮影 ──────────────── */
        is CameraSpec -> {
            CameraCaptureField(
                imagePath = answer,
                onImagePathChange = onAnswer
            )
        }

        /* ──────────────── 動画録画 ──────────────── */
        is VideoSpec -> {
            VideoRecorderField(
                videoPath       = answer,
                maxDurationSec  = spec.maxDurationSec,
                onVideoPathChange = onAnswer
            )
        }

        /* ──────────────── 音声録音 ──────────────── */
        is VoiceSpec -> {
            val context         = LocalContext.current
            val audioController = remember { AudioController() }
            var isRecording     by remember { mutableStateOf(false) }
            var isPlaying       by remember { mutableStateOf(false) }
            var audioPath       by remember { mutableStateOf(answer) }
            var lastError       by remember { mutableStateOf<String?>(null) }

            // 権限チェック
            val permission  = Manifest.permission.RECORD_AUDIO
            val hasPerm     = remember { mutableStateOf(
                context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) }

            val permLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted -> hasPerm.value = granted }

            // 画面破棄時にリソース解放
            DisposableEffect(Unit) { onDispose { audioController.releaseAll() } }

            // 外部から answer が更新されたら同期
            LaunchedEffect(answer) { audioPath = answer }

            /* ---------- UI 本体 ---------- */
            Column {
                lastError?.let { Text(it, color = ErrorRed) }

                VoiceRecorderField(
                    audioPath     = audioPath,
                    isRecording   = isRecording,
                    isPlaying     = isPlaying,
                    onStartRecording = {
                        if (!hasPerm.value) {
                            permLauncher.launch(permission)
                            return@VoiceRecorderField
                        }
                        val path = "${context.cacheDir}/voice_${System.currentTimeMillis()}.m4a"
                        runCatching {
                            audioController.releaseAll()
                            audioController.startRecording(path)
                            isRecording = true
                            onAnswer("")   // 録音中は未回答
                        }.onFailure { e ->
                            lastError  = "録音開始に失敗: ${e.message}"
                            isRecording = false
                        }
                    },
                    onStopRecording = {
                        runCatching {
                            val p = audioController.stopRecording() ?: audioPath
                            isRecording = false
                            audioPath   = p
                            onAnswer(p ?: "")
                        }.onFailure { e ->
                            lastError  = "録音停止に失敗: ${e.message}"
                            isRecording = false
                        }
                    },
                    onPlay = {
                        if (audioPath.isBlank()) return@VoiceRecorderField
                        runCatching {
                            audioController.releaseAll()
                            isPlaying = true
                            audioController.play(audioPath) { isPlaying = false }
                        }.onFailure { e ->
                            lastError = "再生に失敗: ${e.message}"
                            isPlaying = false
                        }
                    },
                    onStopPlay = {
                        audioController.stopPlaying()
                        isPlaying = false
                    },
                    onDelete = {
                        runCatching { audioController.releaseAll() }
                        audioPath = ""
                        onAnswer("")
                        isRecording = false
                        isPlaying   = false
                    }
                )
            }
        }

        /* ──────────────── 自由入力 ──────────────── */
        is FreeSpec -> {
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                singleLine = spec.singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = spec.keyboardType),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }

        /* ──────────────── 単一選択 ──────────────── */
        is SingleSpec -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                spec.options.forEach { opt ->
                    val selected = answer == opt.key
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) AccentBlue else BorderGray,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                color = if (selected) Color(0xFFE3F2FD) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .selectable(selected = selected, onClick = { onAnswer(opt.key) })
                            .padding(14.dp)
                    ) {
                        RadioButton(selected = selected, onClick = { onAnswer(opt.key) })
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(opt.labelRes),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        /* ──────────────── Yes / No ──────────────── */
        is YesNoSpec -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(26.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                val selected = answer
                Button(
                    onClick = { onAnswer(spec.yesKey) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == spec.yesKey) AccentBlue else Color.White,
                        contentColor   = if (selected == spec.yesKey) Color.White else AccentBlue
                    ),
                    border = BorderStroke(2.dp, AccentBlue)
                ) { Text(stringResource(spec.yesLabelRes), fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { onAnswer(spec.noKey) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == spec.noKey) AccentBlue else Color.White,
                        contentColor   = if (selected == spec.noKey) Color.White else AccentBlue
                    ),
                    border = BorderStroke(2.dp, AccentBlue)
                ) { Text(stringResource(spec.noLabelRes), fontWeight = FontWeight.Bold) }
            }
        }

        /* ──────────────── 単一選択＋分岐 ──────────────── */
        is SingleBranchSpec -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                spec.options.forEach { opt ->
                    val selected = answer == opt.key
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) AccentBlue else BorderGray,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                color = if (selected) Color(0xFFE3F2FD) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .selectable(
                                selected = selected,
                                onClick  = {
                                    onAnswer(opt.key)
                                    opt.branchId?.let { onBranchToId(it) }
                                }
                            )
                            .padding(14.dp)
                    ) {
                        RadioButton(selected = selected, onClick = {
                            onAnswer(opt.key)
                            opt.branchId?.let { onBranchToId(it) }
                        })
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(opt.labelRes),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        /* ──────────────── 複数選択 ──────────────── */
        is MultiQueueSpec -> {
            val selectedSet = remember(answer) {
                answer.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                spec.options.forEach { opt ->
                    val checked = opt.key in selectedSet
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (checked) 2.dp else 1.dp,
                                color = if (checked) AccentBlue else BorderGray,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                color = if (checked) Color(0xFFE3F2FD) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { ch ->
                                if (ch) selectedSet.add(opt.key) else selectedSet.remove(opt.key)
                                onAnswer(selectedSet.joinToString(","))
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(opt.labelRes),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
