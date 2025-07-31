
package com.negi.survey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.negi.survey.ui.SurveyApp
import com.negi.survey.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                SurveyAppWithBackground()
            }
        }
    }
}

@Composable
fun SurveyAppWithBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // 🔽 背景画像
        Image(
            painter = painterResource(id = R.drawable.welcome_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 🔼 アプリ全体のUI（SurveyApp）を重ねる
        SurveyApp()
    }
}
