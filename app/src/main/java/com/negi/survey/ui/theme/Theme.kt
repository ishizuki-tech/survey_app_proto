/*
 * ui/theme/Theme.kt
 *
 * アプリ全体の Material3 テーマ設定をまとめたファイル。
 * - ダイナミックカラー (Android 12+) に対応
 * - ダーク／ライトテーマを自動切替
 * - 透過背景を使う軽量テーマ (AppTheme) も用意
 *
 * 依存リソース:
 *   Purple40, Purple80, PurpleGrey40, PurpleGrey80, Pink40, Pink80
 *   └→ これらは別途 Color 定義ファイルで宣言してください。
 */
package com.negi.survey.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/* -------------------------------------------------------------
 *  1) ベースカラーセット
 * ---------------------------------------------------------- */
// --- ダークモード用 ---
private val DarkColorScheme = darkColorScheme(
    primary   = Purple80,
    secondary = PurpleGrey80,
    tertiary  = Pink80
)

// --- ライトモード用 ---
private val LightColorScheme = lightColorScheme(
    primary   = Purple40,
    secondary = PurpleGrey40,
    tertiary  = Pink40
)

/* -------------------------------------------------------------
 *  2) SurveyTheme
 *     - ダーク／ライト ＆ ダイナミックカラー (Android 12+) 対応
 * ---------------------------------------------------------- */
@Composable
fun SurveyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,          // Android 12 以上で動的カラーを使うか
    content: @Composable () -> Unit
) {
    val colors = when {
        // ❶ 動的カラーが有効＆OSが Android 12 以上
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx)
            else           dynamicLightColorScheme(ctx)
        }
        // ❷ 手動で指定されたダークテーマ
        darkTheme -> DarkColorScheme
        // ❸ それ以外はライトテーマ
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography  = Typography,          // ← 事前に Typography.kt で定義
        content     = content
    )
}

/* -------------------------------------------------------------
 *  3) AppTheme
 *     - 透過背景を使う “軽量オーバーレイ” 用の簡易テーマ
 *       (Welcome 画面やダイアログ等で利用)
 * ---------------------------------------------------------- */
private val OverlayColorScheme = lightColorScheme(
    background   = Color.Transparent,                   // 背景は完全透過
    surface      = Color.White.copy(alpha = 0.85f),     // 半透明パネル
    primary      = Color(0xFF2E7D32),                   // グリーン系アクセント
    onPrimary    = Color.White,
    onBackground = Color.Black
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OverlayColorScheme,
        typography  = Typography,
        content     = content
    )
}
