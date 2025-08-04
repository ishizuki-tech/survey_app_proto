package com.negi.survey.ui.components

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.negi.survey.ui.theme.*
import java.io.File
import com.negi.survey.model.*
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

/* -------------------------------------------------------------------------------------------
   写真撮影フィールド
   ---------------------------------------------------------------------------------------- */
@Composable
fun CameraCaptureField(
    imagePath: String?,
    onImagePathChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context  = LocalContext.current
    var path     by remember { mutableStateOf(imagePath ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var lastErr  by remember { mutableStateOf<String?>(null) }
    var preview  by remember { mutableStateOf(false) }

    fun createTempFile() = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")

    // カメラ起動 → 成功したら path 更新
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok && imageUri != null) {
            path = imageUri.toString()
            onImagePathChange(path)
        } else {
            lastErr = "撮影がキャンセルされました"
        }
    }

    Column(modifier) {
        lastErr?.let { Text(it, color = ErrorRed) }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            if (path.isNotBlank()) {
                // サムネ
                val bmp = remember(path) {
                    runCatching {
                        context.contentResolver.openInputStream(Uri.parse(path))!!.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }.getOrNull()
                }
                bmp?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "thumbnail",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, AccentBlue, RoundedCornerShape(8.dp))
                            .clickable { preview = true }
                    )
                }
                Text("撮影済み", color = AccentBlue, modifier = Modifier.padding(start = 8.dp))
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = {
                    val file = createTempFile()
                    imageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    launcher.launch(imageUri!!)
                }) {
                    Icon(Icons.Default.Videocam, contentDescription = null) // ←適宜カメラアイコンへ
                    Spacer(Modifier.width(2.dp))
                    Text("再撮影")
                }
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = {
                    runCatching { File(Uri.parse(path).path!!).delete() }
                    path = ""
                    onImagePathChange("")
                }) {
                    Icon(Icons.Filled.Delete, null, tint = BorderGray)
                }
            } else {
                OutlinedButton(onClick = {
                    val file = createTempFile()
                    imageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    launcher.launch(imageUri!!)
                }) {
                    Icon(Icons.Default.Videocam, null) // ←適宜カメラアイコンへ
                    Spacer(Modifier.width(2.dp))
                    Text("写真を撮る")
                }
            }
        }

        // プレビュー
        if (preview && path.isNotBlank()) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { preview = false }) {
                // ❶ Bitmap をメモ化して読み込む
                val bmp = remember(path) {
                    try {
                        context.contentResolver.openInputStream(Uri.parse(path))?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (e: Exception) {
                        null   // 読み込み失敗時は null を返す
                    }
                }

                // ❷ 読み込み成功なら画像表示、失敗ならメッセージ
                bmp?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "画像プレビュー",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(bitmap.width / bitmap.height.toFloat())
                            .background(MaterialTheme.colorScheme.background)
                    )
                } ?: Text(
                    text = "画像を表示できません",
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------------------------
   動画録画フィールド
   ---------------------------------------------------------------------------------------- */
@Composable
fun VideoRecorderField(
    videoPath: String?,
    maxDurationSec: Int,
    onVideoPathChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context    = LocalContext.current
    var path       by remember { mutableStateOf(videoPath ?: "") }
    var videoUri   by remember { mutableStateOf<Uri?>(null) }
    var playing    by remember { mutableStateOf(false) }
    var lastErr    by remember { mutableStateOf<String?>(null) }

    fun createTempFile() = File(context.cacheDir, "video_${System.currentTimeMillis()}.mp4")

    // 標準カメラアプリ呼び出し
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            videoUri?.let {
                path = it.toString()
                onVideoPathChange(path)
            }
        } else {
            lastErr = "録画がキャンセルされました"
        }
    }

    Column(modifier) {
        lastErr?.let { Text(it, color = ErrorRed) }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            if (path.isNotBlank()) {
                IconButton(onClick = { playing = true }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "play", tint = AccentBlue)
                }
                Text("録画済み", color = AccentBlue, modifier = Modifier.padding(start = 8.dp))
                Spacer(Modifier.width(12.dp))

                OutlinedButton(onClick = {
                    val file = createTempFile()
                    videoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                        putExtra(MediaStore.EXTRA_DURATION_LIMIT, maxDurationSec)
                        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }.also { launcher.launch(it) }
                }) {
                    Icon(Icons.Filled.Videocam, null)
                    Spacer(Modifier.width(2.dp))
                    Text("再録画")
                }

                Spacer(Modifier.width(12.dp))
                IconButton(onClick = {
                    runCatching { File(Uri.parse(path).path!!).delete() }
                    path = ""
                    onVideoPathChange("")
                }) {
                    Icon(Icons.Filled.Delete, null, tint = BorderGray)
                }
            } else {
                OutlinedButton(onClick = {
                    val file = createTempFile()
                    videoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                        putExtra(MediaStore.EXTRA_DURATION_LIMIT, maxDurationSec)
                        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }.also { launcher.launch(it) }
                }) {
                    Icon(Icons.Filled.Videocam, null)
                    Spacer(Modifier.width(2.dp))
                    Text("動画録画")
                }
            }
        }

        // 簡易プレーヤ
        if (playing && path.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    android.widget.VideoView(ctx).apply {
                        setVideoURI(Uri.parse(path))
                        setOnCompletionListener {
                            Handler(Looper.getMainLooper()).post { playing = false }
                        }
                        start()
                    }
                },
                modifier = Modifier
                    .height(220.dp)
                    .fillMaxWidth()
                    .padding(8.dp),
                update = { vv ->
                    if (!vv.isPlaying) {
                        vv.setVideoURI(Uri.parse(path))
                        vv.start()
                    }
                }
            )
            Button(
                onClick = { playing = false },
                modifier = Modifier.padding(8.dp)
            ) { Text("閉じる") }
        }
    }
}
