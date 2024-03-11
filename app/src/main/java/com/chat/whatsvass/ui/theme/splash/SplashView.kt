package com.chat.whatsvass.ui.theme.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.chat.whatsvass.R
import com.chat.whatsvass.ui.theme.login.LoginView

class SplashView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.light)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }
            startActivity(Intent(this, LoginView::class.java))
            finish()
    }
}