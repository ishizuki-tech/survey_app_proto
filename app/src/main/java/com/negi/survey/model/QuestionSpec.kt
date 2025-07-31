package com.negi.survey.model

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.KeyboardType

data class Option(
    val key: String,                 // 言語に依存しない安定キー（保存用）
    @StringRes val labelRes: Int     // 表示用ラベル（リソースID）
)

sealed interface QuestionSpec {
    val id: String
    @get:StringRes val titleRes: Int
    val required: Boolean
    val nextId: String? get() = null

    /** 回答が有効か（FREE 以外は key の存在チェック） */
    fun isValid(answer: String?): Boolean =
        if (!required) true else !answer.isNullOrBlank()
}

/** 自由記述 */
data class FreeSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    override val required: Boolean = true,
    val singleLine: Boolean = true,
    val keyboardType: KeyboardType = KeyboardType.Text,
    override val nextId: String? = null
) : QuestionSpec

/** 単一選択（分岐なし） */
data class SingleSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    val options: List<Option>,
    override val required: Boolean = true,
    override val nextId: String? = null
) : QuestionSpec {
    override fun isValid(answer: String?): Boolean =
        if (!required) true else options.any { it.key == answer }
}

/** YES/NO 分岐（回答は yesKey/noKey を保存） */
data class YesNoSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    val yesKey: String = "yes",
    @StringRes val yesLabelRes: Int,
    val noKey: String = "no",
    @StringRes val noLabelRes: Int,
    val nextIdIfYes: String,
    val nextIdIfNo: String,
    override val required: Boolean = true
) : QuestionSpec {
    override fun isValid(answer: String?): Boolean =
        if (!required) true else (answer == yesKey || answer == noKey)
}

/** 単一選択 → 選んだ key ごとに分岐 */
data class SingleBranchSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    val options: List<Option>,
    val nextIdByKey: Map<String, String>,   // key -> nextId
    override val required: Boolean = true
) : QuestionSpec {
    override fun isValid(answer: String?): Boolean =
        if (!required) true else options.any { it.key == answer }
}

/** 複数選択 → 選んだ key 群の各サブフロー開始IDをキュー処理 */
data class MultiQueueSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    val options: List<Option>,
    val subflowStartIdByKey: Map<String, String>, // key -> subflow start qid
    val priority: List<String>? = null,           // 評価順（key）
    val fallbackNextId: String? = null,
    override val required: Boolean = true,
    override val nextId: String? = null
) : QuestionSpec {
    override fun isValid(answer: String?): Boolean =
        if (!required) true else !answer.isNullOrBlank()
}

/** 音声録音回答 */
data class VoiceSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    override val required: Boolean = true,
    val maxDurationSec: Int = 60,
    override val nextId: String? = null
) : QuestionSpec {
    override fun isValid(answer: String?): Boolean =
        if (!required) true else !answer.isNullOrBlank()
}

/** 動画録画回答 */
data class VideoSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    override val required: Boolean = true,
    val maxDurationSec: Int = 60,
    override val nextId: String? = null
) : QuestionSpec {
    override fun isValid(answer: String?): Boolean =
        if (!required) true else !answer.isNullOrBlank()
}

data class CameraSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    override val required: Boolean = true,
    val maxImages: Int = 1,                  // 複数枚対応する場合
    override val nextId: String? = null
) : QuestionSpec {
    override fun isValid(answer: String?): Boolean =
        if (!required) true else !answer.isNullOrBlank() // answer: 画像ファイルパスやcontent://Uri
}