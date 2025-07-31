package com.negi.survey

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔁 ロケールを再適用（起動時の初期Contextに反映）
        AppCompatDelegate.setApplicationLocales(AppCompatDelegate.getApplicationLocales())

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
