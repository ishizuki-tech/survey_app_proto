/*
 * ui/SurveyApp.kt
 *
 * アプリのエントリーポイントとなる Composable。
 * - Accompanist Navigation（アニメーション付き）で画面遷移を定義
 * - Locale 変更時の再コンポーズに対応
 * - Welcome → Question → Summary → Thanks の 4 画面構成
 *
 * ViewModel 依存:
 *   └ SurveyViewModel  (回答状態・訪問履歴などを保持)
 */
package com.negi.survey.ui

// ──────────────────────────────────
//  Compose / Navigation
// ──────────────────────────────────
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable as animComposable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

// ──────────────────────────────────
//  Android 標準
// ──────────────────────────────────
import android.app.Activity
import android.content.Intent
import androidx.compose.ui.res.painterResource

// ──────────────────────────────────
//  アプリ内リソース
// ──────────────────────────────────
import com.negi.survey.R
import com.negi.survey.ui.screen.*
import com.negi.survey.vm.SurveyViewModel

/* -------------------------------------------------------------
 *  1) ルート定義
 * ---------------------------------------------------------- */
sealed class Route(val route: String) {
    object Welcome  : Route("welcome")
    object Question : Route("question/{id}") {
        fun path(id: String) = "question/$id"
    }
    object Summary  : Route("summary")
    object Thanks   : Route("thanks")
}

/* -------------------------------------------------------------
 *  2) アプリ再起動ユーティリティ
 * ---------------------------------------------------------- */
fun restartApp(activity: Activity) {
    activity.packageManager.getLaunchIntentForPackage(activity.packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        ?.also { intent ->
            activity.finish()
            activity.startActivity(intent)
        }
}

/* -------------------------------------------------------------
 *  3) Main Composable
 * ---------------------------------------------------------- */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SurveyApp() {

    /* ViewModel（画面状態を保持） */
    val vm: SurveyViewModel = viewModel()

    /* アニメーション対応 NavController */
    val nav = rememberAnimatedNavController()

    /* Locale 変更トリガー用キー (値が変わると Welcome 画面が再構築される) */
    var localeKey by remember { mutableStateOf(0) }

    AnimatedNavHost(
        navController    = nav,
        startDestination = Route.Welcome.route,
        enterTransition  = { slideInHorizontally { it } + fadeIn() },
        exitTransition   = { slideOutHorizontally { it } + fadeOut() }
    ) {

        /* ========== ① Welcome ========== */
        animComposable(Route.Welcome.route) {
            val visited by vm.visited.collectAsState()
            val answers by vm.answers.collectAsState()
            val canResume = answers.values.any { it?.toString()?.isNotBlank() == true }

            /* Locale が変わるたびに再描画させる */
            key(localeKey) {
                WelcomePageWrapper(
                    canResume = canResume,
                    onStart = {
                        vm.resetAll()
                        nav.navigate(Route.Question.path(vm.graph.startId)) {
                            popUpTo(Route.Welcome.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onResume = {
                        val targetId = vm.getFirstUnanswered()
                        nav.navigate(Route.Question.path(targetId)) {
                            popUpTo(Route.Welcome.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onLocaleChanged = { localeKey++ }   // ← トリガーをインクリメント
                )
            }
        }

        /* ========== ② Question ========== */
        animComposable(
            Route.Question.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->

            /* ---------- 質問 ID と ViewModel 状態 ---------- */
            val qid = backStackEntry.arguments?.getString("id") ?: return@animComposable
            vm.markVisited(qid)

            val visited by vm.visited.collectAsState()
            val answers by vm.answers.collectAsState()

            val spec   = vm.graph.questions[qid] ?: return@animComposable
            val answer = answers[qid] ?: ""

            /* ---------- 次／前の質問情報 ---------- */
            val nextId     = vm.decideNext(qid)
            val nextSpec   = nextId?.let { vm.graph.questions[it] }
            val nextAnswer = nextId?.let { answers[it] ?: "" }

            val curIndex   = visited.indexOf(qid)
            val backId     = visited.getOrNull(curIndex - 1)
            val backSpec   = backId?.let { vm.graph.questions[it] }
            val backAnswer = backId?.let { answers[it] ?: "" }

            /* ---------- 背景画像 ---------- */
            val bg = painterResource(R.drawable.welcome_bg)

            /* ---------- 画面本体 ---------- */
            QuestionScreen(
                background   = bg,
                spec         = spec,
                answer       = answer,
                nextSpec     = nextSpec,
                nextAnswer   = nextAnswer,
                backSpec     = backSpec,
                backAnswer   = backAnswer,
                onAnswer     = { vm.setAnswer(qid, it) },

                /* ← 戻る */
                onBack       = {
                    val prevId = visited.getOrNull(curIndex - 1)
                    nav.navigate(prevId?.let { Route.Question.path(it) } ?: Route.Welcome.route) {
                        popUpTo(Route.Welcome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },

                /* → 次へ (回答が有効な場合のみ) */
                onNext       = {
                    if (spec.isValid(answer)) {
                        nav.navigate(nextId?.let { Route.Question.path(it) } ?: Route.Summary.route) {
                            launchSingleTop = true
                        }
                    }
                },

                /* 単一選択 + 分岐時に呼ばれる */
                onBranchToId = { branchId ->
                    nav.navigate(Route.Question.path(branchId)) { launchSingleTop = true }
                }
            )
        }

        /* ========== ③ Summary ========== */
        animComposable(Route.Summary.route) {
            SummaryScreen(
                vm = vm,
                onBack = {
                    vm.visited.value.lastOrNull()?.let { last ->
                        nav.navigate(Route.Question.path(last)) { launchSingleTop = true }
                    } ?: nav.popBackStack()
                },
                onFinish = {
                    nav.navigate(Route.Thanks.route) {
                        popUpTo(Route.Welcome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        /* ========== ④ Thanks ========== */
        animComposable(Route.Thanks.route) {
            ThankPageScreen(
                onRestart = {
                    nav.navigate(Route.Welcome.route) {
                        popUpTo(Route.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onClose = { /* Activity.finish() はホスト側で実装 */ }
            )
        }
    }
}
