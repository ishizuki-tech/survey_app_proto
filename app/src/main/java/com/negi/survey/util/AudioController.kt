package com.negi.survey.util

import android.media.MediaPlayer
import android.media.MediaRecorder

/**
 * MediaRecorder と MediaPlayer をラップし、
 * 録音／再生のライフサイクルを安全に扱うためのユーティリティ。
 *
 * Compose とは独立しているため、テストや再利用が容易になる。
 */
class AudioController {

    private var recorder: MediaRecorder? = null
    private var player:   MediaPlayer?   = null
    private var lastPath: String? = null

    /** 録音を開始し、成功すればパスを覚えておく */
    fun startRecording(path: String) {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(path)
            prepare()
            start()
        }
        lastPath = path
    }

    /** 録音停止。成功すればファイルパスを返す */
    fun stopRecording(): String? = try {
        recorder?.apply { stop(); release() }
        recorder = null
        lastPath.also { lastPath = null }
    } catch (e: Exception) {
        recorder?.release()
        recorder = null
        lastPath = null
        null
    }

    /** 録音済みファイルを再生 */
    fun play(path: String, onComplete: () -> Unit) {
        player = MediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener { onComplete(); release() }
            prepare()
            start()
        }
    }

    /** 再生停止 */
    fun stopPlaying() {
        player?.stop()
        player?.release()
        player = null
    }

    /** Activity/Fragment が破棄される際に呼び出してリソースを解放 */
    fun releaseAll() {
        recorder?.release(); recorder = null
        player?.release();   player   = null
        lastPath = null
    }
}
