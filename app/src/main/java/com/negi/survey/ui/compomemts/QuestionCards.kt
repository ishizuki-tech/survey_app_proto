package com.negi.survey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.negi.survey.ui.theme.*

/** 「質問タイトル」を表示するカード */
@Composable
fun QuestionTitleCard(titleRes: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, BorderGray),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = AccentBlue)
            Spacer(Modifier.width(14.dp))
            Text(
                stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/** 質問本文（回答 UI）を内包するカード */
@Composable
fun QuestionBodyCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(2.dp, BorderGray),
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            content = content
        )
    }
}

/** 必須項目エラー用の赤い警告ボックス */
@Composable
fun RequiredErrorBox(message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, ErrorRed, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = ErrorRed)
        Spacer(Modifier.width(6.dp))
        Text(
            message,
            color = ErrorRed,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
