// app/src/main/java/com/negi/survey/ui/SurveyApp.kt
package com.negi.survey.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.negi.survey.model.QuestionSpec
import com.negi.survey.ui.screen.QuestionScreen
import com.negi.survey.ui.screen.SummaryScreen
import com.negi.survey.ui.screen.ThankPageScreen
import com.negi.survey.ui.screen.WelcomePageScreen
import com.negi.survey.vm.SurveyViewModel

sealed class Route(val route: String) {
    data object Welcome : Route("welcome")          // ← 追加
    data object Q : Route("q/{id}") {
        fun path(id: String) = "q/$id"
    }
    data object Summary : Route("summary")
    data object Thanks : Route("thanks")
}

@Composable
fun SurveyApp() {
    val nav = rememberNavController()
    val vm: SurveyViewModel = viewModel()

    NavHost(
        navController = nav,
        startDestination = Route.Welcome.route      // ← 変更: Welcome を開始地点に
    ) {
        // Welcome
        composable(Route.Welcome.route) {
            val visited by vm.visited.collectAsState()
            val canResume = visited.isNotEmpty()

            WelcomePageScreen(
                canResume = canResume,
                onStart = {
                    vm.resetAll()
                    nav.navigate(Route.Q.path(vm.graph.startId)) {
                        launchSingleTop = true
                        popUpTo(Route.Welcome.route) { inclusive = false }
                    }
                },
                onResume = {
                    // VM のヘルパーを使うパターン
                    val targetId = vm.getFirstUnanswered()
                    nav.navigate(Route.Q.path(targetId)) {
                        launchSingleTop = true
                        popUpTo(Route.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        // 質問ページ
        composable(
            route = Route.Q.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val qid = entry.arguments!!.getString("id")!!
            vm.markVisited(qid)

            val visited by vm.visited.collectAsState()   // ← 訪問履歴を監視
            val answers by vm.answers.collectAsState()
            val spec: QuestionSpec = requireNotNull(vm.graph.questions[qid])
            val answer = answers[qid] ?: ""

            val goto: (String?) -> Unit = { nextId ->
                if (nextId != null) {
                    nav.navigate(Route.Q.path(nextId)) { launchSingleTop = true }
                } else {
                    nav.navigate(Route.Summary.route) { launchSingleTop = true }
                }
            }

            val onNext: () -> Unit = {
                if (spec.isValid(answer)) {
                    goto(vm.decideNext(qid))
                }
            }

            val onImmediateBranch: (String) -> Unit = { nextId -> goto(nextId) }

            // ★ ここを変更 ★
            // visited の中から自分の index を探し、一つ前の qid を取得
            val currentIndex = visited.indexOf(qid)
            val prevId = visited.getOrNull(currentIndex - 1)  // index 0 の場合は null

            val onBack: () -> Unit = {
                if (prevId != null) {
                    // 一つ前の質問へ戻る。バックスタックに重複を残さないため popUpTo。
                    nav.navigate(Route.Q.path(prevId)) {
                        launchSingleTop = true
                        popUpTo(Route.Q.path(prevId)) { inclusive = false }
                    }
                } else {
                    // それ以前がないなら Welcome に戻す or finish()
                    nav.navigate(Route.Welcome.route) {
                        launchSingleTop = true
                        popUpTo(Route.Welcome.route) { inclusive = false }
                    }
                }
            }

            QuestionScreen(
                spec = spec,
                answer = answer,
                onAnswer = { vm.setAnswer(qid, it) },
                onBack = onBack,
                onNext = onNext,
                onBranchToId = onImmediateBranch
            )
        }

        // サマリー
        composable(Route.Summary.route) {
            SummaryScreen(
                vm = vm,
                onFinish = {
                    // 送信処理（API/DB）を済ませたらサンクスへ
                    nav.navigate(Route.Thanks.route) {
                        launchSingleTop = true
                        popUpTo(nav.graph.id) { inclusive = false }
                    }
                }
            )
        }

        // サンクス
        composable(Route.Thanks.route) {
            ThankPageScreen(
                onRestart = {
                    // Welcome に戻してやり直し
                    nav.navigate(Route.Welcome.route) {
                        launchSingleTop = true
                        popUpTo(nav.graph.id) { inclusive = true }
                    }
                },
                onClose = { /* Activity.finish() は ThankPageScreen 内で実行 */ }
            )
        }
    }
}
