package com.negi.survey.ui.screen

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.negi.survey.R
import androidx.compose.ui.graphics.Color

import android.util.Log
import com.negi.survey.ui.restartApp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomePageScreen(
    canResume: Boolean,
    onStart: () -> Unit,
    onResume: () -> Unit,
    onLocaleChanged: () -> Unit
) {
    // 👇 ここでロケール確認
    LaunchedEffect(Unit) {
        val appLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val defaultLocale = Locale.getDefault().toLanguageTag()
        Log.d("WelcomePage", "✅ AppCompatDelegate Locale: $appLocale")
        Log.d("WelcomePage", "✅ Locale.getDefault(): $defaultLocale")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_welcome),
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.Transparent) {
                LanguagePicker(
                    languages = listOf(
                        LangItem("en", R.string.lang_en),
                        LangItem("ja", R.string.lang_ja),
                        LangItem("sw", R.string.lang_sw)
                    ),
                    onLocaleChanged = onLocaleChanged
                )
                Spacer(Modifier.weight(1f))
                Button(onClick = onStart) {
                    Text(
                        text = stringResource(
                            if (canResume) R.string.action_start_over else R.string.action_start
                        )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onResume, enabled = canResume) {
                    Text(stringResource(R.string.action_resume))
                }
            }
        },
        contentColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.msg_welcome),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.msg_welcome_sub),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

private data class LangItem(val tag: String, val labelRes: Int)

@Composable
private fun LanguagePicker(
    languages: List<LangItem>,
    onLocaleChanged: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var expanded by remember { mutableStateOf(false) }

    fun setAppLocale(tag: String) {
        val locales = LocaleListCompat.forLanguageTags(tag)
        Log.d("LanguagePicker", "Setting locale to: $tag")

        AppCompatDelegate.setApplicationLocales(locales)

        if (activity != null) {
            Log.d("LanguagePicker", "Restarting app")
            restartApp(activity)  // ←ここで再起動
        } else {
            Log.w("LanguagePicker", "Activity is null, cannot restart")
        }
        onLocaleChanged()
        Log.d("LanguagePicker", "onLocaleChanged() triggered")
    }

    Box {
        Button(onClick = {
            expanded = true
            Log.d("LanguagePicker", "Language menu expanded")
        }) {
            Text(stringResource(R.string.action_language))
        }

        DropdownMenu(expanded = expanded, onDismissRequest = {
            expanded = false
            Log.d("LanguagePicker", "Language menu dismissed")
        }) {
            languages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(stringResource(lang.labelRes)) },
                    onClick = {
                        Log.d("LanguagePicker", "Language selected: ${lang.tag}")
                        setAppLocale(lang.tag)
                        expanded = false
                    }
                )
            }
        }
    }
}
