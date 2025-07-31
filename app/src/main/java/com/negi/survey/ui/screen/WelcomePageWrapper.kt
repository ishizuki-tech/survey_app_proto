package com.negi.survey.ui.screen

import androidx.compose.runtime.Composable

@Composable
fun WelcomePageWrapper(
    canResume: Boolean,
    onStart: () -> Unit,
    onResume: () -> Unit,
    onLocaleChanged: () -> Unit
) {
    WelcomePageScreen(
        canResume = canResume,
        onStart = onStart,
        onResume = onResume,
        onLocaleChanged = onLocaleChanged
    )
}
