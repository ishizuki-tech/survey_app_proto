package com.negi.survey.ui.screen

// ---------- Android Compose & Kotlin 標準 ----------
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ---------- アプリ内コンポーネント ----------
import com.negi.survey.model.QuestionSpec
import com.negi.survey.ui.components.BackButton
import com.negi.survey.ui.components.BackgroundImageBox
import com.negi.survey.ui.components.CenteredQuestionColumn
import com.negi.survey.ui.components.NextButton
import com.negi.survey.ui.components.QuestionBodyCard
import com.negi.survey.ui.components.QuestionContent
import com.negi.survey.ui.components.QuestionTitleCard
import com.negi.survey.ui.components.RequiredErrorBox

// ---------- テーマ（色など） ----------
import com.negi.survey.ui.theme.BorderGray

/**
 * 質問単体の画面。
 * ・左右スワイプで前後プレビュー＋遷移
 * ・Back / Next ボタン
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    background:     Painter,
    spec:           QuestionSpec,
    answer:         String,
    nextSpec:       QuestionSpec?,
    nextAnswer:     String?,
    backSpec:       QuestionSpec?,
    backAnswer:     String?,
    onAnswer:       (String) -> Unit,
    onBack:         () -> Unit,
    onNext:         () -> Unit,
    onBranchToId:   (String) -> Unit
) {
    val canNext   = spec.isValid(answer)
    val offsetX   = remember { Animatable(0f) }
    val scope     = rememberCoroutineScope()
    val screenW   = LocalConfiguration.current.screenWidthDp.dp
    val pxWidth   = with(LocalDensity.current) { screenW.toPx() }

    BackgroundImageBox(background) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar  = { /* トップバーは空：スワイプのみ */ },
            bottomBar = {
                BottomAppBar(containerColor = Color.Transparent) {
                    BackButton(onClick = onBack)
                    Spacer(Modifier.weight(1f))
                    NextButton(enabled = canNext, onClick = onNext)
                }
            }
        ) { innerPadding ->
            // ── 横スワイプ検知 ─────────────────────────────
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, delta ->
                                scope.launch { offsetX.snapTo(offsetX.value + delta) }
                            },
                            onDragEnd = {
                                scope.launch {
                                    when {
                                        offsetX.value < -200 && canNext -> {      // ← 左へ大きく
                                            offsetX.animateTo(-pxWidth, tween(150))
                                            onNext();  offsetX.snapTo(0f)
                                        }
                                        offsetX.value >  200 -> {                 // → 右へ大きく
                                            offsetX.animateTo(pxWidth, tween(150))
                                            onBack();  offsetX.snapTo(0f)
                                        }
                                        else -> offsetX.animateTo(0f, tween(300)) // 戻す
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch { offsetX.animateTo(0f, tween(300)) }
                            }
                        )
                    }
                    .padding(innerPadding)
            ) {
                // ── 前後プレビュー ────────────────────────────
                if (offsetX.value < 0f) {
                    PreviewQuestion(
                        spec      = nextSpec,
                        answer    = nextAnswer,
                        emptyMsg  = "次の質問はありません",
                        offsetX   = pxWidth + offsetX.value
                    )
                }
                if (offsetX.value > 0f) {
                    PreviewQuestion(
                        spec      = backSpec,
                        answer    = backAnswer,
                        emptyMsg  = "前の質問はありません",
                        offsetX   = -pxWidth + offsetX.value
                    )
                }

                // ── 本体表示 ───────────────────────────────
                Box(Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }) {
                    CenteredQuestionColumn {
                        QuestionTitleCard(spec.titleRes)
                        QuestionBodyCard {
                            QuestionContent(spec, answer, onAnswer, onBranchToId)
                            if (!canNext && spec.required) {
                                RequiredErrorBox("回答は必須です")
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ─────────────────────────────────────────────────────────────
   内部ヘルパー：プレビューを半透明で描画
   ───────────────────────────────────────────────────────────── */
@Composable
private fun PreviewQuestion(
    spec:      QuestionSpec?,
    answer:    String?,
    emptyMsg:  String,
    offsetX:   Float
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .fillMaxSize()
            .alpha(0.5f)
    ) {
        if (spec != null && answer != null) {
            CenteredQuestionColumn {
                QuestionTitleCard(spec.titleRes)
                QuestionBodyCard {
                    // プレビューなので onAnswer / onBranchToId は空
                    QuestionContent(spec, answer, {}, {})
                }
            }
        } else {
            CenteredQuestionColumn {
                QuestionBodyCard { Text(emptyMsg, color = BorderGray) }
            }
        }
    }
}
