/*
 * model/QuestionSpec.kt
 *
 * 質問テンプレートを表すデータモデル群。
 * 1. Option              …… 単一／複数選択肢の要素
 * 2. QuestionSpec (sealed) …… すべての設問タイプの共通インタフェース
 * 3. 各派生データクラス   …… Free / Single / YesNo / SingleBranch /
 *                               MultiQueue / Voice / Video / Camera
 *
 * どの設問でも `isValid()` を呼べば「回答が有効か」を判定できるため、
 * ViewModel 側で共通ロジックを組みやすい設計になっています。
 */
package com.negi.survey.model

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.KeyboardType

/* -------------------------------------------------------------
 *  1) 選択肢モデル
 * ---------------------------------------------------------- */
/**
 * @param key      データ保存／分岐判定に使う内部キー（ローカライズ非依存）
 * @param labelRes UI 表示用のリソース ID
 * @param branchId SingleBranchSpec などで “この選択肢を選んだら飛ぶ” qid
 */
data class Option(
    val key: String,
    @StringRes val labelRes: Int,
    val branchId: String? = null
)

/* -------------------------------------------------------------
 *  2) 共通インタフェース
 * ---------------------------------------------------------- */
sealed interface QuestionSpec {
    val id: String
    @get:StringRes val titleRes: Int
    val required: Boolean

    /** “直列” で遷移する次の設問 ID（分岐しない通常遷移） */
    val nextId: String? get() = null

    /** 回答が有効かを判定するデフォルト実装 */
    fun isValid(answer: String?): Boolean =
        if (!required) true else !answer.isNullOrBlank()
}

/* -------------------------------------------------------------
 *  3) 各設問タイプ
 * ---------------------------------------------------------- */

/** 自由記述テキスト (1 行 or 複数行) */
data class FreeSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    override val required: Boolean = true,
    val singleLine: Boolean = true,
    val keyboardType: KeyboardType = KeyboardType.Text,
    override val nextId: String? = null
) : QuestionSpec

/** 単一選択 (分岐なし) */
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

/** Yes / No (2 択) + 分岐 */
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

/** 単一選択 → 選択肢ごとに分岐 */
data class SingleBranchSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    val options: List<Option>,
    /** key → nextId のマッピング */
    val nextIdByKey: Map<String, String>,
    override val required: Boolean = true
) : QuestionSpec {
    override fun isValid(answer: String?): Boolean =
        if (!required) true else options.any { it.key == answer }
}

/** 複数選択 → 選択 key ごとのサブフローをキュー処理 */
data class MultiQueueSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    val options: List<Option>,
    /** key → サブフロー最初の qid */
    val subflowStartIdByKey: Map<String, String>,
    /** サブフロー実行の優先順 (key)。null なら options 順 */
    val priority: List<String>? = null,
    /** 何も選ばれなかった場合に進む qid (null なら次またはキュー) */
    val fallbackNextId: String? = null,
    override val required: Boolean = true,
    override val nextId: String? = null
) : QuestionSpec

/** 音声録音回答 */
data class VoiceSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    override val required: Boolean = true,
    val maxDurationSec: Int = 60,
    override val nextId: String? = null
) : QuestionSpec

/** 動画回答 */
data class VideoSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    override val required: Boolean = true,
    val maxDurationSec: Int = 60,
    override val nextId: String? = null
) : QuestionSpec

/** 写真回答 (複数枚対応可) */
data class CameraSpec(
    override val id: String,
    @StringRes override val titleRes: Int,
    override val required: Boolean = true,
    val maxImages: Int = 1,
    override val nextId: String? = null
) : QuestionSpec
