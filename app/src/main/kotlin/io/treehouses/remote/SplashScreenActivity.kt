package io.treehouses.remote

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import io.treehouses.remote.utils.SaveUtils

class SplashScreenActivity : AppCompatActivity() {
    private var logoAnimation: Animation? = null
    private var textAnimation: Animation? = null
    private var logo: ImageView? = null
    private var logoText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this@SplashScreenActivity)
        nightMode()
        if (preferences.getBoolean("splashScreen", true)) {
            setContentView(R.layout.activity_splash_screen)
            logo = findViewById(R.id.splash_logo)
            logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim)
            logo?.animation = logoAnimation
            logoText = findViewById(R.id.logo_text)
            textAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_text_anim)
            logoText?.animation = textAnimation
            Handler().postDelayed({
                goToNextActivity()
            }, SPLASH_TIME_OUT.toLong())
        } else { goToNextActivity() }
    }

    private fun goToNextActivity() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(SaveUtils.Screens.FIRST_TIME.name, true)) {
            startActivity(Intent(this, IntroActivity::class.java))
            val editor = preferences.edit()
            editor.putBoolean(SaveUtils.Screens.FIRST_TIME.name, false)
            editor.apply()
        }
        else {
            startActivity(Intent(this@SplashScreenActivity, InitialActivity::class.java))
        }
        finish()
    }

    companion object {
        private const val SPLASH_TIME_OUT = 2000
    }

    private fun nightMode() {
        val preference = PreferenceManager.getDefaultSharedPreferences(this).getString("dark_mode", "Follow System")
        val options = listOf(*resources.getStringArray(R.array.dark_mode_options))
        resources.getStringArray(R.array.led_options_commands)
        when (options.indexOf(preference)) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun fontSize()
    {
        val fontSize = PreferenceManager.getDefaultSharedPreferences(this).getInt("font_size", 1)
    }
}