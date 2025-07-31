package com.negi.survey

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // システムロケールを使う（初期設定）
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }
}
