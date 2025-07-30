package com.negi.survey.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.negi.survey.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomePageScreen(
    canResume: Boolean,
    onStart: () -> Unit,
    onResume: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.title_welcome)) })
        },
        bottomBar = {
            BottomAppBar {
                // 言語ピッカー（任意）
                LanguagePicker(
                    languages = listOf(
                        LangItem("en", R.string.lang_en),
                        LangItem("ja", R.string.lang_ja),
                        LangItem("sw", R.string.lang_sw)
                    )
                )
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onStart) {
                    Text(stringResource(R.string.action_start))
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onResume, enabled = canResume) {
                    Text(stringResource(R.string.action_resume))
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.msg_welcome),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.msg_welcome_sub),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            // ここにロゴやイラストを置く場合は Image などを追加
        }
    }
}

private data class LangItem(val tag: String, val labelRes: Int)

@Composable
private fun LanguagePicker(languages: List<LangItem>) {
    var expanded by remember { mutableStateOf(false) }
    var current by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { "en" }) }

    fun setAppLocale(tag: String) {
        val locales = LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
        current = tag
    }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(stringResource(R.string.action_language))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(stringResource(lang.labelRes)) },
                    onClick = {
                        setAppLocale(lang.tag)
                        expanded = false
                    }
                )
            }
        }
    }
}
