//package com.negi.survey.ui.screen
//
//import android.annotation.SuppressLint
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.*
//import androidx.compose.foundation.gestures.detectHorizontalDragGestures
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.selection.selectable
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material.icons.filled.Warning
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.painter.Painter
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import com.negi.survey.R
//import com.negi.survey.model.*
//import kotlinx.coroutines.launch
//import kotlin.math.roundToInt
//import androidx.compose.foundation.background
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Mic
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.Stop
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.platform.LocalContext
//import java.io.File
//import android.media.MediaPlayer
//import android.media.MediaRecorder
//import android.Manifest
//import android.content.pm.PackageManager
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import android.app.Activity
//import android.content.Intent
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.provider.MediaStore
//import androidx.compose.ui.viewinterop.AndroidView
//import android.os.Handler
//import android.os.Looper
//import android.widget.VideoView
//import androidx.compose.foundation.layout.ColumnScope.align
//import androidx.compose.material.icons.filled.Videocam
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.window.Dialog
//import androidx.core.content.FileProvider
//
//private val BorderGray = Color(0xFFB0B0B0)
//private val AccentBlue = Color(0xFF1976D2)
//private val ErrorRed = Color(0xFFD32F2F)
//
//@Composable
//fun BackgroundImageBox(
//    painter: Painter,
//    content: @Composable BoxScope.() -> Unit
//) {
//    Box(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Image(
//            painter = painter,
//            contentDescription = "background",
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            content()
//        }
//    }
//}
//
//@Composable
//fun CenteredQuestionColumn(content: @Composable ColumnScope.() -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(vertical = 24.dp),
//        contentAlignment = Alignment.TopCenter
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .widthIn(max = 600.dp),
//            verticalArrangement = Arrangement.Top,
//            content = content
//        )
//    }
//}
//
//@Composable
//fun QuestionTitleCard(titleRes: Int) {
//    Surface(
//        color = Color.White,
//        shadowElevation = 8.dp,
//        shape = RoundedCornerShape(16.dp),
//        border = BorderStroke(2.dp, BorderGray),
//        modifier = Modifier
//            .padding(horizontal = 12.dp, vertical = 12.dp)
//            .fillMaxWidth()
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
//        ) {
//            Icon(Icons.Filled.Info, contentDescription = null, tint = AccentBlue)
//            Spacer(Modifier.width(14.dp))
//            Text(
//                text = stringResource(titleRes),
//                style = MaterialTheme.typography.titleLarge.copy(
//                    color = Color.Black,
//                    fontWeight = FontWeight.Bold,
//                )
//            )
//        }
//    }
//}
//
//@Composable
//fun QuestionBodyCard(content: @Composable ColumnScope.() -> Unit) {
//    Surface(
//        shape = RoundedCornerShape(18.dp),
//        color = Color.White,
//        shadowElevation = 6.dp,
//        border = BorderStroke(2.dp, BorderGray),
//        modifier = Modifier
//            .padding(horizontal = 10.dp, vertical = 8.dp)
//            .fillMaxWidth()
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(horizontal = 20.dp, vertical = 22.dp)
//                .fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(20.dp),
//            content = content
//        )
//    }
//}
//
//@Composable
//fun RequiredErrorBox(text: String) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier
//            .fillMaxWidth()
//            .border(2.dp, ErrorRed, RoundedCornerShape(12.dp))
//            .padding(horizontal = 12.dp, vertical = 8.dp)
//    ) {
//        Icon(Icons.Filled.Warning, contentDescription = null, tint = ErrorRed)
//        Spacer(Modifier.width(6.dp))
//        Text(text, color = ErrorRed, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
//    }
//}
//
//@Composable
//fun PreviewQuestion(
//    spec: QuestionSpec?,
//    answer: String?,
//    emptyMessage: String
//) {
//    CenteredQuestionColumn {
//        if (spec != null && answer != null) {
//            QuestionTitleCard(spec.titleRes)
//            QuestionBodyCard {
//                QuestionContent(
//                    spec = spec,
//                    answer = answer,
//                    onAnswer = {},        // Previewはreadonly
//                    onBranchToId = {}
//                )
//            }
//        } else {
//            Spacer(modifier = Modifier.height(16.dp))
//            QuestionBodyCard {
//                Text(
//                    emptyMessage,
//                    style = MaterialTheme.typography.titleMedium,
//                    color = Color.Gray,
//                    modifier = Modifier.Companion.align(Alignment.CenterHorizontally)
//                )
//            }
//        }
//    }
//}
//
//@SuppressLint("UnusedBoxWithConstraintsScope")
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun QuestionScreen(
//    backgroundPainter: Painter,
//    spec: QuestionSpec,
//    answer: String,
//    nextSpec: QuestionSpec?,
//    nextAnswer: String?,
//    backSpec: QuestionSpec?,
//    backAnswer: String?,
//    onAnswer: (String) -> Unit,
//    onBack: () -> Unit,
//    onNext: () -> Unit,
//    onBranchToId: (String) -> Unit
//) {
//    val canNext = spec.isValid(answer)
//    val offsetX = remember { Animatable(0f) }
//    val coroutineScope = rememberCoroutineScope()
//    val width = with(LocalDensity.current) {
//        LocalConfiguration.current.screenWidthDp.dp.toPx()
//    }
//
//    BackgroundImageBox(backgroundPainter) {
//        Scaffold(
//            containerColor = Color.Transparent,
//            topBar = {
//                Box(modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }) {
//                    TopAppBar(
//                        title = { /* 空 */ },
//                        colors = TopAppBarDefaults.topAppBarColors(
//                            containerColor = Color.Transparent,
//                            titleContentColor = Color.Black
//                        )
//                    )
//                }
//            },
//            bottomBar = {
//                BottomAppBar(containerColor = Color.Transparent) {
//                    BackButton(onClick = onBack)
//                    Spacer(Modifier.weight(1f))
//                    NextButton(enabled = canNext, onClick = onNext)
//                }
//            }
//        ) { innerPadding ->
//            BoxWithConstraints(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .pointerInput(Unit) {
//                        detectHorizontalDragGestures(
//                            onDragEnd = {
//                                coroutineScope.launch {
//                                    when {
//                                        offsetX.value < -200 && canNext -> {
//                                            offsetX.animateTo(-width, tween(150))
//                                            onNext()
//                                            offsetX.snapTo(0f)
//                                        }
//                                        offsetX.value > 200 -> {
//                                            offsetX.animateTo(width, tween(150))
//                                            onBack()
//                                            offsetX.snapTo(0f)
//                                        }
//                                        else -> {
//                                            offsetX.animateTo(0f, tween(300))
//                                        }
//                                    }
//                                }
//                            },
//                            onDragCancel = {
//                                coroutineScope.launch { offsetX.animateTo(0f, tween(300)) }
//                            },
//                            onHorizontalDrag = { _, dragAmount ->
//                                coroutineScope.launch {
//                                    offsetX.snapTo(offsetX.value + dragAmount)
//                                }
//                            }
//                        )
//                    }
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(innerPadding)
//                ) {
//                    // プレビュー: 次
//                    if (offsetX.value < 0f) {
//                        Box(
//                            modifier = Modifier
//                                .offset { IntOffset((width + offsetX.value).roundToInt(), 0) }
//                                .fillMaxSize()
//                                .alpha(0.5f)
//                        ) {
//                            PreviewQuestion(nextSpec, nextAnswer, "次の質問はありません")
//                        }
//                    }
//                    // プレビュー: 前
//                    if (offsetX.value > 0f) {
//                        Box(
//                            modifier = Modifier
//                                .offset { IntOffset((-width + offsetX.value).roundToInt(), 0) }
//                                .fillMaxSize()
//                                .alpha(0.5f)
//                        ) {
//                            PreviewQuestion(backSpec, backAnswer, "前の質問はありません")
//                        }
//                    }
//                    // ★★★ 現在の質問をoffsetで移動（ワイプアウト） ★★★
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
//                    ) {
//                        CenteredQuestionColumn {
//                            QuestionTitleCard(spec.titleRes)
//                            QuestionBodyCard {
//                                QuestionContent(spec, answer, onAnswer, onBranchToId)
//                                if (!canNext && spec.required) {
//                                    RequiredErrorBox(stringResource(R.string.msg_required))
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BackButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    OutlinedButton(
//        onClick = onClick,
//        modifier = modifier.padding(8.dp)
//    ) {
//        Icon(Icons.Filled.ArrowBack, contentDescription = null)
//        Spacer(Modifier.width(4.dp))
//        Text(stringResource(R.string.action_back))
//    }
//}
//
//@Composable
//fun NextButton(
//    enabled: Boolean,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Button(
//        onClick = onClick,
//        enabled = enabled,
//        modifier = modifier.padding(8.dp),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = if (enabled) AccentBlue else BorderGray,
//            contentColor = Color.White
//        )
//    ) {
//        Text(stringResource(R.string.action_next), fontWeight = FontWeight.Bold)
//    }
//}
//
//// --- 録音・再生用の状態管理クラス ---
//class AudioController {
//    private var recorder: MediaRecorder? = null
//    private var player: MediaPlayer? = null
//    private var recordingPath: String? = null   // ← 追加
//
//    fun startRecording(path: String) {
//        recorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            setOutputFile(path)
//            prepare()
//            start()
//        }
//        recordingPath = path   // ← ここで覚えておく！
//    }
//
//    fun stopRecording(): String? {
//        return try {
//            recorder?.apply {
//                stop()
//                release()
//            }
//            val path = recordingPath    // ← ここで返す
//            recorder = null
//            recordingPath = null
//            path
//        } catch (e: Exception) {
//            recorder?.release()
//            recorder = null
//            recordingPath = null
//            null
//        }
//    }
//
//    fun play(path: String, onComplete: () -> Unit) {
//        player = MediaPlayer().apply {
//            setDataSource(path)
//            setOnCompletionListener {
//                onComplete()
//                release()
//            }
//            prepare()
//            start()
//        }
//    }
//
//    fun stopPlaying() {
//        player?.stop()
//        player?.release()
//        player = null
//    }
//
//    fun releaseAll() {
//        recorder?.release()
//        recorder = null
//        player?.release()
//        player = null
//        recordingPath = null
//    }
//}
//
//@Composable
//fun VoiceRecorderField(
//    audioPath: String?,
//    isRecording: Boolean,
//    isPlaying: Boolean,
//    onStartRecording: () -> Unit,
//    onStopRecording: () -> Unit,
//    onPlay: () -> Unit,
//    onStopPlay: () -> Unit,
//    onDelete: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Surface(
//        shape = MaterialTheme.shapes.medium,
//        color = Color(0xFFF6F8FC),
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
//        ) {
//            when {
//                isRecording -> {
//                    IconButton(onClick = onStopRecording) {
//                        Icon(Icons.Filled.Stop, contentDescription = "Stop Recording", tint = Color.Red)
//                    }
//                    Text("録音中...", color = Color.Red, modifier = Modifier.padding(start = 8.dp))
//                }
//                isPlaying -> {
//                    IconButton(onClick = onStopPlay) {
//                        Icon(Icons.Filled.Stop, contentDescription = "Stop Playing", tint = AccentBlue)
//                    }
//                    Text("再生中", color = AccentBlue, modifier = Modifier.padding(start = 8.dp))
//                }
//                !audioPath.isNullOrBlank() -> {
//                    IconButton(onClick = onPlay) {
//                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = AccentBlue)
//                    }
//                    Text("録音済み", color = AccentBlue, modifier = Modifier.padding(start = 8.dp))
//                    Spacer(Modifier.width(12.dp))
//                    OutlinedButton(onClick = onStartRecording) {
//                        Icon(Icons.Filled.Mic, contentDescription = "Re-record")
//                        Spacer(Modifier.width(2.dp))
//                        Text("再録音")
//                    }
//                    Spacer(Modifier.width(12.dp))
//                    IconButton(onClick = onDelete) {
//                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Gray)
//                    }
//                }
//                else -> {
//                    OutlinedButton(onClick = onStartRecording) {
//                        Icon(Icons.Filled.Mic, contentDescription = "Record")
//                        Spacer(Modifier.width(2.dp))
//                        Text("録音する")
//                    }
//                }
//            }
//        }
//    }
//}
//@Composable
//fun QuestionContent(
//    spec: QuestionSpec,
//    answer: String,
//    onAnswer: (String) -> Unit,
//    onBranchToId: (String) -> Unit
//) {
//    when (spec) {
//
//        is CameraSpec -> {
//            val context = LocalContext.current
//            var imagePath by remember { mutableStateOf(answer) }
//            var imageUri by remember { mutableStateOf<Uri?>(if (answer.isNotBlank()) Uri.parse(answer) else null) }
//            var lastError by remember { mutableStateOf<String?>(null) }
//            var showPreview by remember { mutableStateOf(false) }
//
//            // 一時ファイル作成
//            fun createTempImageFile(): File =
//                File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
//
//            // 撮影用Launcher
//            val photoLauncher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.TakePicture()
//            ) { success ->
//                if (success && imageUri != null) {
//                    imagePath = imageUri.toString()
//                    onAnswer(imagePath)
//                } else {
//                    lastError = "撮影がキャンセルされました"
//                }
//            }
//
//            Column {
//                if (lastError != null) {
//                    Text(lastError ?: "", color = ErrorRed)
//                }
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(8.dp)
//                ) {
//                    if (!imagePath.isNullOrBlank()) {
//                        // サムネイル表示 & タップで拡大
//                        val bitmap = remember(imagePath) {
//                            try {
//                                val input = context.contentResolver.openInputStream(Uri.parse(imagePath))
//                                BitmapFactory.decodeStream(input)
//                            } catch (_: Exception) { null }
//                        }
//                        if (bitmap != null) {
//                            Image(
//                                bitmap = bitmap.asImageBitmap(),
//                                contentDescription = "回答画像サムネイル",
//                                modifier = Modifier
//                                    .size(64.dp)
//                                    .clip(RoundedCornerShape(8.dp))
//                                    .border(2.dp, AccentBlue, RoundedCornerShape(8.dp))
//                                    .clickable { showPreview = true }
//                            )
//                        }
//                        Text("撮影済み", color = AccentBlue, modifier = Modifier.padding(start = 8.dp))
//                        Spacer(Modifier.width(12.dp))
//                        OutlinedButton(onClick = {
//                            // 再撮影
//                            val file = createTempImageFile()
//                            val uri = FileProvider.getUriForFile(
//                                context,
//                                "${context.packageName}.provider",
//                                file
//                            )
//                            imageUri = uri
//                            photoLauncher.launch(uri)
//                        }) {
//                            Icon(Icons.Filled.Mic, contentDescription = "再撮影") // カメラアイコンが望ましい
//                            Spacer(Modifier.width(2.dp))
//                            Text("再撮影")
//                        }
//                        Spacer(Modifier.width(12.dp))
//                        IconButton(onClick = {
//                            // ファイル削除
//                            try { File(Uri.parse(imagePath).path!!).delete() } catch (_: Exception) {}
//                            imagePath = ""
//                            onAnswer("")
//                        }) {
//                            Icon(Icons.Filled.Delete, contentDescription = "削除", tint = Color.Gray)
//                        }
//                    } else {
//                        OutlinedButton(onClick = {
//                            // 撮影
//                            val file = createTempImageFile()
//                            val uri = FileProvider.getUriForFile(
//                                context,
//                                "${context.packageName}.provider",
//                                file
//                            )
//                            imageUri = uri
//                            photoLauncher.launch(uri)
//                        }) {
//                            Icon(Icons.Filled.Mic, contentDescription = "撮影")
//                            Spacer(Modifier.width(2.dp))
//                            Text("写真を撮る")
//                        }
//                    }
//                }
//                // 拡大プレビュー
//                if (showPreview && !imagePath.isNullOrBlank()) {
//                    Dialog(onDismissRequest = { showPreview = false }) {
//                        val bitmap = remember(imagePath) {
//                            try {
//                                val input = context.contentResolver.openInputStream(Uri.parse(imagePath))
//                                BitmapFactory.decodeStream(input)
//                            } catch (_: Exception) { null }
//                        }
//                        if (bitmap != null) {
//                            Image(
//                                bitmap = bitmap.asImageBitmap(),
//                                contentDescription = "画像プレビュー",
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .aspectRatio(bitmap.width / bitmap.height.toFloat())
//                                    .background(Color.Black)
//                            )
//                        } else {
//                            Text("画像を表示できません", color = ErrorRed)
//                        }
//                    }
//                }
//            }
//        }
//
//
//
//
//        is VideoSpec -> {
//            val context = LocalContext.current
//            var videoPath by remember { mutableStateOf(answer) }
//            var videoUri by remember { mutableStateOf<Uri?>(if (answer.isNotBlank()) Uri.parse(answer) else null) }
//            var isPlaying by remember { mutableStateOf(false) }
//            var lastError by remember { mutableStateOf<String?>(null) }
//
//            fun createTempVideoFile(): File =
//                File(context.cacheDir, "video_${System.currentTimeMillis()}.mp4")
//
//            val videoLauncher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.StartActivityForResult()
//            ) { result ->
//                if (result.resultCode == Activity.RESULT_OK) {
//                    videoUri?.let { uri ->
//                        videoPath = uri.toString()
//                        onAnswer(videoPath)
//                    }
//                } else {
//                    lastError = "録画がキャンセルされました"
//                }
//            }
//
//            Column {
//                if (lastError != null) {
//                    Text(lastError ?: "", color = ErrorRed)
//                }
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(8.dp)
//                ) {
//                    if (!videoPath.isNullOrBlank()) {
//                        IconButton(onClick = { isPlaying = true }) {
//                            Icon(Icons.Filled.PlayArrow, contentDescription = "動画再生", tint = AccentBlue)
//                        }
//                        Text("録画済み", color = AccentBlue, modifier = Modifier.padding(start = 8.dp))
//                        Spacer(Modifier.width(12.dp))
//                        OutlinedButton(onClick = {
//                            val file = createTempVideoFile()
//                            val uri = FileProvider.getUriForFile(
//                                context,
//                                "${context.packageName}.provider", // ← Manifestと同じauthorities!
//                                file
//                            )
//                            videoUri = uri
//                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
//                                putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                                putExtra(MediaStore.EXTRA_DURATION_LIMIT, spec.maxDurationSec)
//                                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                            }
//                            videoLauncher.launch(intent)
//                        }) {
//                            Icon(Icons.Filled.Videocam, contentDescription = "再録画")
//                            Spacer(Modifier.width(2.dp))
//                            Text("再録画")
//                        }
//                        Spacer(Modifier.width(12.dp))
//                        IconButton(onClick = {
//                            try {
//                                videoPath?.let { p ->
//                                    File(Uri.parse(p).path!!).delete()
//                                }
//                            } catch (_: Exception) {}
//                            videoPath = ""
//                            onAnswer("")
//                        }) {
//                            Icon(Icons.Filled.Delete, contentDescription = "削除", tint = Color.Gray)
//                        }
//                    } else {
//                        OutlinedButton(onClick = {
//                            val file = createTempVideoFile()
//                            val uri = FileProvider.getUriForFile(
//                                context,
//                                "${context.packageName}.provider", // ← Manifestと同じauthorities!
//                                file
//                            )
//                            videoUri = uri
//                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
//                                putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                                putExtra(MediaStore.EXTRA_DURATION_LIMIT, spec.maxDurationSec)
//                                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                            }
//                            videoLauncher.launch(intent)
//                        }) {
//                            Icon(Icons.Filled.Videocam, contentDescription = "録画")
//                            Spacer(Modifier.width(2.dp))
//                            Text("動画録画")
//                        }
//                    }
//                }
//
//                if (isPlaying && !videoPath.isNullOrBlank()) {
//                    AndroidView(
//                        modifier = Modifier
//                            .height(220.dp)
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        factory = { ctx ->
//                            VideoView(ctx).apply {
//                                setVideoURI(Uri.parse(videoPath))
//                                setOnCompletionListener {
//                                    Handler(Looper.getMainLooper()).post {
//                                        isPlaying = false
//                                    }
//                                }
//                                start()
//                            }
//                        },
//                        update = { vv ->
//                            if (!vv.isPlaying) {
//                                vv.setVideoURI(Uri.parse(videoPath))
//                                vv.start()
//                            }
//                        }
//                    )
//                    Button(
//                        onClick = { isPlaying = false },
//                        modifier = Modifier.padding(8.dp)
//                    ) { Text("閉じる") }
//                }
//            }
//        }
//
//        is VoiceSpec -> {
//            val context = LocalContext.current
//            val audioController = remember { AudioController() }
//            var isRecording by remember { mutableStateOf(false) }
//            var isPlaying by remember { mutableStateOf(false) }
//            var audioPath by remember { mutableStateOf(answer) }
//            var lastError by remember { mutableStateOf<String?>(null) }
//
//            // Runtime Permission（API 23+）
//            val audioPermission = Manifest.permission.RECORD_AUDIO
//            val hasPermission = remember {
//                mutableStateOf(
//                    context.checkSelfPermission(audioPermission) == PackageManager.PERMISSION_GRANTED
//                )
//            }
//            val permissionLauncher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.RequestPermission()
//            ) { granted ->
//                hasPermission.value = granted
//            }
//
//            DisposableEffect(Unit) {
//                onDispose {
//                    audioController.releaseAll()
//                }
//            }
//
//            // answerが親Stateから変更されたらローカルStateも同期
//            LaunchedEffect(answer) { audioPath = answer }
//
//            Column {
//                if (lastError != null) {
//                    Text(lastError ?: "", color = ErrorRed)
//                }
//                VoiceRecorderField(
//                    audioPath = audioPath,
//                    isRecording = isRecording,
//                    isPlaying = isPlaying,
//                    onStartRecording = {
//                        if (!hasPermission.value) {
//                            permissionLauncher.launch(audioPermission)
//                            return@VoiceRecorderField
//                        }
//                        val filePath = context.cacheDir.absolutePath + "/voice_${System.currentTimeMillis()}.m4a"
//                        try {
//                            audioController.releaseAll()
//                            audioController.startRecording(filePath)
//                            isRecording = true
//                            onAnswer("") // 録音中は未回答
//                        } catch (e: Exception) {
//                            lastError = "録音開始に失敗しました: ${e.message}"
//                            isRecording = false
//                        }
//                    },
//                    onStopRecording = {
//                        try {
//                            val path = audioController.stopRecording() ?: audioPath
//                            isRecording = false
//                            audioPath = path
//                            onAnswer(path ?: "")
//                        } catch (e: Exception) {
//                            lastError = "録音停止に失敗: ${e.message}"
//                            isRecording = false
//                        }
//                    },
//                    onPlay = {
//                        if (audioPath.isNullOrBlank()) return@VoiceRecorderField
//                        try {
//                            audioController.releaseAll()
//                            isPlaying = true
//                            audioController.play(audioPath!!) {
//                                isPlaying = false
//                            }
//                        } catch (e: Exception) {
//                            lastError = "再生に失敗: ${e.message}"
//                            isPlaying = false
//                        }
//                    },
//                    onStopPlay = {
//                        audioController.stopPlaying()
//                        isPlaying = false
//                    },
//                    onDelete = {
//                        try {
//                            audioController.releaseAll()
//                            if (!audioPath.isNullOrBlank()) {
//                                File(audioPath!!).delete()
//                            }
//                        } catch (_: Exception) {}
//                        audioPath = ""
//                        onAnswer("")
//                        isRecording = false
//                        isPlaying = false
//                    }
//                )
//            }
//        }
//
//        is FreeSpec -> {
//            OutlinedTextField(
//                value = answer,
//                onValueChange = onAnswer,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 10.dp),
//                singleLine = spec.singleLine,
//                keyboardOptions = KeyboardOptions(keyboardType = spec.keyboardType),
//                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
//            )
//        }
//
//        is SingleSpec -> {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                modifier = Modifier.padding(vertical = 8.dp)
//            ) {
//                spec.options.forEach { opt ->
//                    val selected = answer == opt.key
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .border(
//                                width = if (selected) 2.dp else 1.dp,
//                                color = if (selected) AccentBlue else BorderGray,
//                                shape = RoundedCornerShape(14.dp)
//                            )
//                            .background(
//                                color = if (selected) Color(0xFFE3F2FD) else Color.Transparent,
//                                shape = RoundedCornerShape(14.dp)
//                            )
//                            .selectable(selected = selected, onClick = { onAnswer(opt.key) })
//                            .padding(14.dp)
//                    ) {
//                        RadioButton(selected = selected, onClick = { onAnswer(opt.key) })
//                        Spacer(Modifier.width(12.dp))
//                        Text(
//                            stringResource(opt.labelRes),
//                            style = MaterialTheme.typography.bodyLarge.copy(
//                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
//                                color = Color.Black
//                            )
//                        )
//                    }
//                }
//            }
//        }
//
//        is YesNoSpec -> {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(26.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.padding(vertical = 12.dp)
//            ) {
//                val selectedKey = answer
//                Button(
//                    onClick = { onAnswer(spec.yesKey) },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (selectedKey == spec.yesKey) AccentBlue else Color.White,
//                        contentColor = if (selectedKey == spec.yesKey) Color.White else AccentBlue
//                    ),
//                    border = BorderStroke(2.dp, AccentBlue)
//                ) {
//                    Text(stringResource(spec.yesLabelRes), fontWeight = FontWeight.Bold)
//                }
//                Button(
//                    onClick = { onAnswer(spec.noKey) },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (selectedKey == spec.noKey) AccentBlue else Color.White,
//                        contentColor = if (selectedKey == spec.noKey) Color.White else AccentBlue
//                    ),
//                    border = BorderStroke(2.dp, AccentBlue)
//                ) {
//                    Text(stringResource(spec.noLabelRes), fontWeight = FontWeight.Bold)
//                }
//            }
//        }
//
//        is SingleBranchSpec -> {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                modifier = Modifier.padding(vertical = 8.dp)
//            ) {
//                spec.options.forEach { opt ->
//                    val selected = answer == opt.key
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .border(
//                                width = if (selected) 2.dp else 1.dp,
//                                color = if (selected) AccentBlue else BorderGray,
//                                shape = RoundedCornerShape(14.dp)
//                            )
//                            .background(
//                                color = if (selected) Color(0xFFE3F2FD) else Color.Transparent,
//                                shape = RoundedCornerShape(14.dp)
//                            )
//                            .selectable(selected = selected, onClick = { onAnswer(opt.key) })
//                            .padding(14.dp)
//                    ) {
//                        RadioButton(selected = selected, onClick = { onAnswer(opt.key) })
//                        Spacer(Modifier.width(12.dp))
//                        Text(
//                            stringResource(opt.labelRes),
//                            style = MaterialTheme.typography.bodyLarge.copy(
//                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
//                                color = Color.Black
//                            )
//                        )
//                    }
//                }
//            }
//        }
//
//        is MultiQueueSpec -> {
//            val selected = remember(answer) {
//                answer.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
//            }
//            Column(
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                modifier = Modifier.padding(vertical = 8.dp)
//            ) {
//                spec.options.forEach { opt ->
//                    val checked = opt.key in selected
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .border(
//                                width = if (checked) 2.dp else 1.dp,
//                                color = if (checked) AccentBlue else BorderGray,
//                                shape = RoundedCornerShape(14.dp)
//                            )
//                            .background(
//                                color = if (checked) Color(0xFFE3F2FD) else Color.Transparent,
//                                shape = RoundedCornerShape(14.dp)
//                            )
//                            .padding(14.dp)
//                    ) {
//                        Checkbox(
//                            checked = checked,
//                            onCheckedChange = { ch ->
//                                if (ch) selected.add(opt.key) else selected.remove(opt.key)
//                                onAnswer(selected.joinToString(","))
//                            }
//                        )
//                        Spacer(Modifier.width(12.dp))
//                        Text(
//                            stringResource(opt.labelRes),
//                            style = MaterialTheme.typography.bodyLarge.copy(
//                                fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
//                                color = Color.Black
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
