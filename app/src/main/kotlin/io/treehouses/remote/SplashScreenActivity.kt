package io.treehouses.remote

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import io.treehouses.remote.databinding.ActivitySplashScreenBinding
import io.treehouses.remote.utils.SaveUtils.Screens.FIRST_TIME

class SplashScreenActivity : AppCompatActivity() {
    lateinit var activitySplashBinding: ActivitySplashScreenBinding
    private var logoAnimation: Animation? = null
    private var textAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding = ActivitySplashScreenBinding.inflate(layoutInflater)

        // Install the splash screen
        val splashScreen = installSplashScreen()

        // Apply customizations (e.g., night mode, font scale)
        nightMode()
        adjustFontScale(resources.configuration, fontSize())

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("splashScreen", true)) {
            // Keep the splash screen for the duration
            splashScreen.setKeepOnScreenCondition { true }
            setContentView(activitySplashBinding.root)
            textAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_text_anim)
            activitySplashBinding.logoText.animation = textAnimation
            goToNextActivityAfterDelay()
        } else {
            goToNextActivity()
        }
    }

    private fun adjustFontScale(configuration: Configuration?, fontSize: Int) {
        configuration?.let {
            it.fontScale = 0.05F * fontSize.toFloat()
            val metrics: DisplayMetrics = resources.displayMetrics
            val wm: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = configuration.fontScale * metrics.density
            baseContext.applicationContext.createConfigurationContext(it)
            baseContext.resources.displayMetrics.setTo(metrics)
        }
    }

    private fun goToNextActivityAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({ goToNextActivity() }, SPLASH_TIME_OUT.toLong())
    }

    private fun goToNextActivity() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(FIRST_TIME.name, true)) {
            startActivity(Intent(this, IntroActivity::class.java))
            preferences.edit().putBoolean(FIRST_TIME.name, false).apply()
        } else {
            startActivity(Intent(this@SplashScreenActivity, InitialActivity::class.java))
        }
        finish()
    }

    private fun nightMode() {
        val preference = PreferenceManager.getDefaultSharedPreferences(this).getString("dark_mode", "Follow System")
        val options = resources.getStringArray(R.array.dark_mode_options).toList()
        when (options.indexOf(preference)) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun fontSize(): Int {
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("font_size", 18)
    }

    companion object {
        private const val SPLASH_TIME_OUT = 2000
    }
}
