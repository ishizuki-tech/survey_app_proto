/*
 * ui/theme/Typography.kt
 *
 * Material 3 用のフォントサイズ・ウェイト定義。
 * 必要最低限のスタイルだけを上書きし、
 * それ以外は MaterialTheme のデフォルト値を継承します。
 *
 * ❶ まず “本文” で多用する `bodyLarge`
 * ❷ 将来カスタムフォントを導入しやすいように FontFamily を外出し
 */
package com.negi.survey.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/* -------------------------------------------------------------
 *  ベース FontFamily
 *  - Google Fonts などを組み込む場合はここを差し替える
 * ---------------------------------------------------------- */
private val AppFontFamily = FontFamily.Default     // ← デフォルト System Font

/* -------------------------------------------------------------
 *  Typography 定義
 *  - 未指定のスタイルは Material3 標準値が適用される
 * ---------------------------------------------------------- */
val Typography = Typography(
    /* 本文 (16sp) */
    bodyLarge = TextStyle(
        fontFamily    = AppFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp
    ),

    /* 見出し (22sp) の一例。必要なら増やす */
    titleLarge = TextStyle(
        fontFamily    = AppFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),

    /* ボタンやラベルに使う小さめテキスト (11sp) */
    labelSmall = TextStyle(
        fontFamily    = AppFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    )
)
