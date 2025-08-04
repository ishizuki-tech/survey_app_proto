/*
 * ui/theme/Colors.kt
 *
 * アプリ全体で共通して使う “色” の定義をまとめたファイル。
 * MaterialTheme の ColorScheme とは切り離し、
 * 個別 UI コンポーネントから直接参照したいアクセント・ボーダー色も
 * ここに定義しておきます。
 */
package com.negi.survey.ui.theme

import androidx.compose.ui.graphics.Color

/* -------------------------------------------------------------
 * ① Material3 のベース色 (例: Purple 系 / Pink 系)
 *    - ダーク／ライトテーマ共通で使う “主題” カラー
 * ---------------------------------------------------------- */
val Purple80      = Color(0xFFD0BCFF)
val PurpleGrey80  = Color(0xFFCCC2DC)
val Pink80        = Color(0xFFEFB8C8)

val Purple40      = Color(0xFF6650A4)
val PurpleGrey40  = Color(0xFF625B71)
val Pink40        = Color(0xFF7D5260)

/* -------------------------------------------------------------
 * ② 汎用ユーティリティ色
 *    - ボーダー・アイコン・バリデーションなどで頻出
 * ---------------------------------------------------------- */
/** 罫線や無効状態で使うライトグレー */
val BorderGray = Color(0xFFB0B0B0)

/** ブランドのアクセントとして使うブルー (プライマリ 1 次色) */
val AccentBlue = Color(0xFF1976D2)

/** 入力エラーや警告で使うレッド */
val ErrorRed   = Color(0xFFD32F2F)
