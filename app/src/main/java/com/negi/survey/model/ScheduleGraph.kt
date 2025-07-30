//package com.negi.survey.model
//
//import androidx.compose.ui.text.input.KeyboardType
//import com.negi.survey.R
//
//data class SurveyGraph(
//    val startId: String,
//    val questions: Map<String, QuestionSpec>
//)
//
//fun buildGraph(): SurveyGraph {
//    val maize = Option("maize", R.string.opt_maize)
//    val rice  = Option("rice",  R.string.opt_rice)
//    val wheat = Option("wheat", R.string.opt_wheat)
//    val other = Option("other", R.string.opt_other)
//
//    val legume = Option("legume", R.string.opt_legume)
//    val veg    = Option("veg",    R.string.opt_veg)
//    val fruit  = Option("fruit",  R.string.opt_fruit)
//
//    val nodes: List<QuestionSpec> = listOf(
//        YesNoSpec(
//            id = "q_start",
//            titleRes = R.string.q_start_title,
//            yesLabelRes = R.string.yes,
//            noLabelRes  = R.string.no,
//            nextIdIfYes = "q_crop",
//            nextIdIfNo  = "q_job"
//        ),
//        SingleBranchSpec(
//            id = "q_crop",
//            titleRes = R.string.q_crop_title,
//            options = listOf(maize, rice, wheat, other),
//            nextIdByKey = mapOf(
//                "maize" to "flow_maize_1",
//                "rice"  to "flow_rice_1",
//                "wheat" to "flow_wheat_1",
//                "other" to "q_other_crop"
//            )
//        ),
//        // --- Maize flow ---
//        FreeSpec(
//            id = "flow_maize_1",
//            titleRes = R.string.q_maize_area,
//            keyboardType = KeyboardType.Number,
//            nextId = "flow_maize_2"
//        ),
//        FreeSpec(
//            id = "flow_maize_2",
//            titleRes = R.string.q_maize_variety,
//            nextId = "q_secondary"
//        ),
//        // --- Rice flow ---
//        FreeSpec(
//            id = "flow_rice_1",
//            titleRes = R.string.q_rice_area,
//            keyboardType = KeyboardType.Number,
//            nextId = "flow_rice_2"
//        ),
//        FreeSpec(
//            id = "flow_rice_2",
//            titleRes = R.string.q_rice_irrigation,
//            nextId = "q_secondary"
//        ),
//        // --- Wheat flow ---
//        FreeSpec(
//            id = "flow_wheat_1",
//            titleRes = R.string.q_wheat_area,
//            keyboardType = KeyboardType.Number,
//            nextId = "q_secondary"
//        ),
//        // --- Other ---
//        FreeSpec(
//            id = "q_other_crop",
//            titleRes = R.string.q_other_crop,
//            nextId = "q_secondary"
//        ),
//        // --- Secondary (MultiQueue) ---
//        MultiQueueSpec(
//            id = "q_secondary",
//            titleRes = R.string.q_secondary_title,
//            options = listOf(legume, veg, fruit),
//            subflowStartIdByKey = mapOf(
//                "legume" to "flow_legume_1",
//                "veg"    to "flow_veg_1",
//                "fruit"  to "flow_fruit_1"
//            ),
//            priority = listOf("legume", "veg", "fruit"),
//            fallbackNextId = "q_job",
//            nextId = "q_job"
//        ),
//        FreeSpec(
//            id = "flow_legume_1",
//            titleRes = R.string.q_legume_area,
//            keyboardType = KeyboardType.Number
//        ),
//        FreeSpec(
//            id = "flow_veg_1",
//            titleRes = R.string.q_veg_area,
//            keyboardType = KeyboardType.Number
//        ),
//        FreeSpec(
//            id = "flow_fruit_1",
//            titleRes = R.string.q_fruit_count,
//            keyboardType = KeyboardType.Number
//        ),
//        FreeSpec(
//            id = "q_job",
//            titleRes = R.string.q_job_title,
//            nextId = "q_country"
//        ),
//        FreeSpec(
//            id = "q_country",
//            titleRes = R.string.q_country_title
//        )
//    )
//    return SurveyGraph(
//        startId = "q_start",
//        questions = nodes.associateBy { it.id }
//    )
//}
