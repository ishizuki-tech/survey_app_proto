/*
 * model/SurveyGraph.kt
 *
 * 質問フロー定義をまとめたファイル。
 * ① SurveyGraph データクラス … startId と質問マップ
 * ② カメラ／ビデオ／音声テスト用の簡易グラフ
 * ③ 本番想定のサンプルグラフ (buildGraph)
 *
 * すべて `questions` を  Map<id, QuestionSpec> で保持するため、
 * ViewModel 側は id さえ分かればランダムアクセス可能です。
 */
package com.negi.survey.model

import androidx.compose.ui.text.input.KeyboardType
import com.negi.survey.R

/* -------------------------------------------------------------
 *  1) グラフのコンテナ
 * ---------------------------------------------------------- */
data class SurveyGraph(
    val startId:   String,                     // 最初に表示する設問 ID
    val questions: Map<String, QuestionSpec>   // id → QuestionSpec
)

/* -------------------------------------------------------------
 *  2) カメラ／ビデオ／音声テスト用のシンプルなグラフ
 * ---------------------------------------------------------- */
fun buildCameraTestGraph(): SurveyGraph {
    val nodes = listOf(
        CameraSpec(
            id        = "q_photo_reason",
            titleRes  = R.string.q_photo_reason_title,
            required  = false,
            maxImages = 1,
            nextId    = "q_photo_family"
        ),
        CameraSpec(
            id        = "q_photo_family",
            titleRes  = R.string.q_photo_family_title,
            required  = false,
            maxImages = 1,
            nextId    = "q_photo_local"
        ),
        CameraSpec(
            id        = "q_photo_local",
            titleRes  = R.string.q_photo_local_title,
            required  = false,
            maxImages = 1,
            nextId    = null                 // 終端
        )
    )
    return SurveyGraph(
        startId   = "q_photo_reason",
        questions = nodes.associateBy { it.id }
    )
}

fun buildVideoTestGraph(): SurveyGraph {
    val nodes = listOf(
        VideoSpec(
            id             = "q_video_reason",
            titleRes       = R.string.q_video_reason_title,
            required       = false,
            maxDurationSec = 30,
            nextId         = "q_video_family"
        ),
        VideoSpec(
            id             = "q_video_family",
            titleRes       = R.string.q_video_family_title,
            required       = false,
            maxDurationSec = 20,
            nextId         = "q_video_local"
        ),
        VideoSpec(
            id             = "q_video_local",
            titleRes       = R.string.q_video_local_title,
            required       = false,
            maxDurationSec = 25,
            nextId         = null
        )
    )
    return SurveyGraph(
        startId   = "q_video_reason",
        questions = nodes.associateBy { it.id }
    )
}

fun buildVoiceTestGraph(): SurveyGraph {
    val nodes = listOf(
        VoiceSpec(
            id             = "q_voice_reason",
            titleRes       = R.string.q_voice_reason_title,
            required       = false,
            maxDurationSec = 30,
            nextId         = "q_voice_family"
        ),
        VoiceSpec(
            id             = "q_voice_family",
            titleRes       = R.string.q_voice_family_title,
            required       = false,
            maxDurationSec = 20,
            nextId         = "q_voice_local"
        ),
        VoiceSpec(
            id             = "q_voice_local",
            titleRes       = R.string.q_voice_local_title,
            required       = false,
            maxDurationSec = 25,
            nextId         = null
        )
    )
    return SurveyGraph(
        startId   = "q_voice_reason",
        questions = nodes.associateBy { it.id }
    )
}

/* -------------------------------------------------------------
 *  3) 本番想定のサンプルグラフ
 * ---------------------------------------------------------- */
fun buildGraph(): SurveyGraph {

    /* ---------- 選択肢定義 ---------- */
    // 作物
    val maize = Option("maize", R.string.opt_maize)
    val rice  = Option("rice",  R.string.opt_rice)
    val wheat = Option("wheat", R.string.opt_wheat)
    val other = Option("other", R.string.opt_other)

    // 副次作物
    val legume = Option("legume", R.string.opt_legume)
    val veg    = Option("veg",    R.string.opt_veg)
    val fruit  = Option("fruit",  R.string.opt_fruit)

    /* ---------- 質問ノード ---------- */
    val nodes: List<QuestionSpec> = listOf(
        /* --- スタート: Yes / No --- */
        YesNoSpec(
            id            = "q_start",
            titleRes      = R.string.q_start_title,
            yesLabelRes   = R.string.yes,
            noLabelRes    = R.string.no,
            nextIdIfYes   = "q_crop",
            nextIdIfNo    = "q_job"
        ),

        /* --- 作物選択 (分岐) --- */
        SingleBranchSpec(
            id           = "q_crop",
            titleRes     = R.string.q_crop_title,
            options      = listOf(maize, rice, wheat, other),
            nextIdByKey  = mapOf(
                "maize" to "flow_maize_1",
                "rice"  to "flow_rice_1",
                "wheat" to "flow_wheat_1",
                "other" to "q_other_crop"
            )
        ),

        /* ===== Maize フロー ===== */
        FreeSpec(
            id           = "flow_maize_1",
            titleRes     = R.string.q_maize_area,
            keyboardType = KeyboardType.Number,
            nextId       = "flow_maize_2"
        ),
        FreeSpec(
            id       = "flow_maize_2",
            titleRes = R.string.q_maize_variety,
            nextId   = "q_voice_reason"
        ),

        /* ===== Rice フロー ===== */
        FreeSpec(
            id           = "flow_rice_1",
            titleRes     = R.string.q_rice_area,
            keyboardType = KeyboardType.Number,
            nextId       = "flow_rice_2"
        ),
        FreeSpec(
            id       = "flow_rice_2",
            titleRes = R.string.q_rice_irrigation,
            nextId   = "q_secondary"
        ),

        /* ===== Wheat フロー ===== */
        FreeSpec(
            id           = "flow_wheat_1",
            titleRes     = R.string.q_wheat_area,
            keyboardType = KeyboardType.Number,
            nextId       = "q_secondary"
        ),

        /* ===== Other Crop ===== */
        FreeSpec(
            id       = "q_other_crop",
            titleRes = R.string.q_other_crop,
            nextId   = "q_secondary"
        ),

        /* --- 副次作物 (MultiQueue) --- */
        MultiQueueSpec(
            id                = "q_secondary",
            titleRes          = R.string.q_secondary_title,
            options           = listOf(legume, veg, fruit),
            subflowStartIdByKey = mapOf(
                "legume" to "flow_legume_1",
                "veg"    to "flow_veg_1",
                "fruit"  to "flow_fruit_1"
            ),
            priority          = listOf("legume", "veg", "fruit"),
            fallbackNextId    = "q_job",
            nextId            = "q_job"
        ),
        /* サブフロー (Legume / Veg / Fruit) */
        FreeSpec(
            id           = "flow_legume_1",
            titleRes     = R.string.q_legume_area,
            keyboardType = KeyboardType.Number
        ),
        FreeSpec(
            id           = "flow_veg_1",
            titleRes     = R.string.q_veg_area,
            keyboardType = KeyboardType.Number
        ),
        FreeSpec(
            id           = "flow_fruit_1",
            titleRes     = R.string.q_fruit_count,
            keyboardType = KeyboardType.Number
        ),

        /* --- 職業 / 国名 (共通末尾) --- */
        FreeSpec(
            id       = "q_job",
            titleRes = R.string.q_job_title,
            nextId   = "q_country"
        ),
        FreeSpec(
            id       = "q_country",
            titleRes = R.string.q_country_title
        ),

        /* --- 作物理由を音声で --- */
        VoiceSpec(
            id             = "q_voice_reason",
            titleRes       = R.string.q_voice_reason_title,
            required       = false,
            maxDurationSec = 30,
            nextId         = "q_secondary"
        )
    )

    /* ---------- グラフ生成 ---------- */
    return SurveyGraph(
        startId   = "q_start",
        questions = nodes.associateBy { it.id }
    )
}

/* -------------------------------------------------------------
 *  テスト用：全コンポーネント網羅グラフ
 * ---------------------------------------------------------- */
fun buildAllComponentsTestGraph(): SurveyGraph {

    /* ---------- 汎用選択肢 ---------- */
    val optA = Option("a", R.string.opt_a)
    val optB = Option("b", R.string.opt_b)
    val optC = Option("c", R.string.opt_c)

    val nodes: List<QuestionSpec> = listOf(
        /* 1️⃣ Yes / No */
        YesNoSpec(
            id            = "q_yes_no",
            titleRes      = R.string.q_yes_no_title,
            yesLabelRes   = R.string.yes,
            noLabelRes    = R.string.no,
            nextIdIfYes   = "q_single",
            nextIdIfNo    = "q_single"
        ),

        /* 2️⃣ 単一選択 (分岐なし) */
        SingleSpec(
            id        = "q_single",
            titleRes  = R.string.q_single_title,
            options   = listOf(optA, optB, optC),
            nextId    = "q_single_branch"
        ),

        /* 3️⃣ 単一選択 + 分岐 */
        SingleBranchSpec(
            id          = "q_single_branch",
            titleRes    = R.string.q_single_branch_title,
            options     = listOf(optA, optB, optC),
            nextIdByKey = mapOf(
                "a" to "q_free",      // a を選んだら FreeSpec へ
                "b" to "q_camera",    // b を選んだら CameraSpec へ
                "c" to "q_camera"     // c も CameraSpec へ
            )
        ),

        /* 4️⃣ 自由記述 (FreeSpec) */
        FreeSpec(
            id           = "q_free",
            titleRes     = R.string.q_free_title,
            singleLine   = false,
            keyboardType = KeyboardType.Text,
            nextId       = "q_multi_queue"
        ),

        /* 5️⃣ 複数選択 (MultiQueue) + サブフロー */
        MultiQueueSpec(
            id       = "q_multi_queue",
            titleRes = R.string.q_multi_queue_title,
            options  = listOf(optA, optB, optC),
            subflowStartIdByKey = mapOf(
                "a" to "sub_flow_a",
                "b" to "sub_flow_b",
                "c" to "sub_flow_c"
            ),
            priority       = listOf("a", "b", "c"),
            fallbackNextId = "q_voice",
            nextId         = "q_voice"
        ),
        /* --- サブフロー ×3 (簡易) --- */
        FreeSpec(
            id       = "sub_flow_a",
            titleRes = R.string.q_sub_a,
            nextId   = "q_voice"
        ),
        FreeSpec(
            id       = "sub_flow_b",
            titleRes = R.string.q_sub_b,
            nextId   = "q_voice"
        ),
        FreeSpec(
            id       = "sub_flow_c",
            titleRes = R.string.q_sub_c,
            nextId   = "q_voice"
        ),

        /* 6️⃣ 音声録音 */
        VoiceSpec(
            id             = "q_voice",
            titleRes       = R.string.q_voice_title,
            required       = false,
            maxDurationSec = 15,
            nextId         = "q_video"
        ),

        /* 7️⃣ 動画録画 */
        VideoSpec(
            id             = "q_video",
            titleRes       = R.string.q_video_title,
            required       = false,
            maxDurationSec = 20,
            nextId         = "q_camera"
        ),

        /* 8️⃣ 写真撮影 */
        CameraSpec(
            id        = "q_camera",
            titleRes  = R.string.q_camera_title,
            required  = false,
            maxImages = 1,
            nextId    = "q_thanks"
        ),

        /* 9️⃣ 最終：自由記述で終了確認 */
        FreeSpec(
            id       = "q_thanks",
            titleRes = R.string.q_thanks_title,
            required = false,
            singleLine = true,
            nextId   = null          // 終端
        )
    )

    return SurveyGraph(
        startId   = "q_yes_no",
        questions = nodes.associateBy { it.id }
    )
}
