package com.negi.survey.ui

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable as animComposable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.negi.survey.model.QuestionSpec
import com.negi.survey.model.SingleBranchSpec
import com.negi.survey.model.YesNoSpec
import com.negi.survey.ui.screen.*
import com.negi.survey.vm.SurveyViewModel
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.res.painterResource
import com.negi.survey.R

sealed class Route(val route: String) {
    object Welcome : Route("welcome")
    object Question : Route("question/{id}") {
        fun path(id: String) = "question/$id"
    }
    object Summary : Route("summary")
    object Thanks : Route("thanks")
}

fun restartApp(activity: Activity) {
    val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    activity.finish()
    activity.startActivity(intent)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SurveyApp() {
    val viewModel: SurveyViewModel = viewModel()
    val navController = rememberAnimatedNavController()

    // 🔁 言語切り替えトリガー
    var localeKey by remember { mutableStateOf(0) }

    AnimatedNavHost(
        navController = navController,
        startDestination = Route.Welcome.route,
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { slideOutHorizontally { it } + fadeOut() }
    ) {
        // Welcome Screen
        animComposable(Route.Welcome.route) {
            val visited by viewModel.visited.collectAsState()
            val answers by viewModel.answers.collectAsState()
            val canResume = answers.values.any { it?.toString()?.isNotBlank() == true }

            key(localeKey) {
                WelcomePageWrapper(
                    canResume = canResume,
                    onStart = {
                        viewModel.resetAll()
                        navController.navigate(Route.Question.path(viewModel.graph.startId)) {
                            popUpTo(Route.Welcome.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onResume = {
                        val targetId = viewModel.getFirstUnanswered()
                        navController.navigate(Route.Question.path(targetId)) {
                            popUpTo(Route.Welcome.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onLocaleChanged = { localeKey++ }
                )
            }
        }

        // Question Screen
        animComposable(
            Route.Question.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("id") ?: return@animComposable
            viewModel.markVisited(questionId)

            val visited by viewModel.visited.collectAsState()
            val answers by viewModel.answers.collectAsState()
            val spec: QuestionSpec = viewModel.graph.questions[questionId] ?: return@animComposable
            val answer = answers[questionId] ?: ""

            // 次の質問の取得
            val nextId = viewModel.decideNext(questionId)
            val nextSpec = nextId?.let { viewModel.graph.questions[it] }
            val nextAnswer = nextId?.let { answers[it] ?: "" }

            // 前の質問の取得
            val currentIndex = visited.indexOf(questionId)
            val backId = if (currentIndex > 0) visited[currentIndex - 1] else null
            val backSpec = backId?.let { viewModel.graph.questions[it] }
            val backAnswer = backId?.let { answers[it] ?: "" }

            val bgPainter = painterResource(id = R.drawable.welcome_bg) // ← あなたの背景画像

            QuestionScreen(
                backgroundPainter = bgPainter,
                spec = spec,
                answer = answer,
                nextSpec = nextSpec,
                nextAnswer = nextAnswer,
                backSpec = backSpec,
                backAnswer = backAnswer,
                onAnswer = { viewModel.setAnswer(questionId, it) },
                onBack = {
                    val previousId = visited.getOrNull(currentIndex - 1)
                    navController.navigate(previousId?.let { Route.Question.path(it) } ?: Route.Welcome.route) {
                        popUpTo(previousId?.let { Route.Question.path(it) } ?: Route.Welcome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNext = {
                    if (spec.isValid(answer)) {
                        navController.navigate(nextId?.let { Route.Question.path(it) } ?: Route.Summary.route) {
                            launchSingleTop = true
                        }
                    }
                },
                onBranchToId = { branchId ->
                    navController.navigate(Route.Question.path(branchId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Summary Screen
        animComposable(Route.Summary.route) {
            SummaryScreen(
                vm = viewModel,
                onBack = {
                    val lastVisited = viewModel.visited.value.lastOrNull()
                    if (lastVisited != null) {
                        navController.navigate(Route.Question.path(lastVisited)) {
                            launchSingleTop = true
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onFinish = {
                    navController.navigate(Route.Thanks.route) {
                        popUpTo(Route.Welcome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Thank You Screen
        animComposable(Route.Thanks.route) {
            ThankPageScreen(
                onRestart = {
                    navController.navigate(Route.Welcome.route) {
                        popUpTo(Route.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onClose = { /* Activity.finish() handled externally */ }
            )
        }
    }
}
