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

            QuestionScreen(
                spec = spec,
                answer = answer,
                onAnswer = { viewModel.setAnswer(questionId, it) },
                onBack = {
                    val previousId = visited.getOrNull(visited.indexOf(questionId) - 1)
                    navController.navigate(previousId?.let { Route.Question.path(it) } ?: Route.Welcome.route) {
                        popUpTo(previousId?.let { Route.Question.path(it) } ?: Route.Welcome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNext = {
                    if (spec.isValid(answer)) {
                        val nextId = when (spec) {
                            is YesNoSpec -> when (answer) {
                                spec.yesKey -> spec.nextIdIfYes
                                spec.noKey -> spec.nextIdIfNo
                                else -> null
                            }
                            is SingleBranchSpec -> spec.nextIdByKey[answer]
                            else -> viewModel.decideNext(questionId)
                        }

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
