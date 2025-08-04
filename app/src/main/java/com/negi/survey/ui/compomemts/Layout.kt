package com.negi.survey.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * 画面全体に背景画像を敷き、その上に任意の内容を重ねる Box。
 */
@Composable
fun BackgroundImageBox(
    painter: Painter,
    content: @Composable BoxScope.() -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = "background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize(), content = content)
    }
}

/**
 * 質問カード群を画面上部寄せで中央寄せしたいときのレイアウト。
 */
@Composable
fun CenteredQuestionColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            content = content
        )
    }
}
