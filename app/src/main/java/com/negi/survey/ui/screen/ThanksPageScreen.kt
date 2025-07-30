package com.negi.survey.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.negi.survey.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThankPageScreen(
    onRestart: () -> Unit,
    onClose: () -> Unit
) {
    val activity = LocalContext.current as? Activity

    // 戻るボタンを無効化（任意。戻れる仕様にしたい場合は削除）
    BackHandler(enabled = true) { /* no-op: ここで戻りを抑止 */ }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.title_thanks)) })
        },
        bottomBar = {
            BottomAppBar {
                OutlinedButton(onClick = onRestart) {
                    Text(stringResource(R.string.action_restart))
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = {
                    onClose()
                    activity?.finish() // そのまま終了（任意）
                }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.msg_thanks),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.msg_thanks_sub),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
