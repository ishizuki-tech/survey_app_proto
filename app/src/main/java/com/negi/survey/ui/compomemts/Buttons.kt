package com.negi.survey.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.negi.survey.R
import com.negi.survey.ui.theme.*

/** 戻るボタン */
@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.padding(8.dp)
    ) {
        Icon(Icons.Filled.ArrowBack, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.action_back))
    }
}

/** 次へボタン */
@Composable
fun NextButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) AccentBlue else BorderGray,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(stringResource(R.string.action_next), fontWeight = FontWeight.Bold)
    }
}
