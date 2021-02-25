package io.treehouses.remote

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.logD


class SplashScreenActivity : AppCompatActivity() {
    private var logoAnimation: Animation? = null
    private var textAnimation: Animation? = null
    private var logo: ImageView? = null
    private var logoText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this@SplashScreenActivity)
        nightMode()
        adjustFontScale(resources.configuration, fontSize())
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

    fun adjustFontScale(configuration: Configuration?, fontSize: Int) {
        configuration?.let {
            it.fontScale = 0.05F*fontSize.toFloat()
            val metrics: DisplayMetrics = resources.displayMetrics
            val wm: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = configuration.fontScale * metrics.density

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                baseContext.applicationContext.createConfigurationContext(it)
            }
            baseContext.resources.displayMetrics.setTo(metrics)

        }
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

    private fun fontSize(): Int
    {
        logD("FONT SIZE " + PreferenceManager.getDefaultSharedPreferences(this).getInt("font_size", 2).toString())
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("font_size", 14)
    }
}