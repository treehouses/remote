package io.treehouses.remote.Views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import io.treehouses.remote.R


class CustomPrefCategory @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0)
    : PreferenceCategory(context, attrs, defStyle) {

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val color = when(AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> ContextCompat.getColor(context, R.color.accent)
            AppCompatDelegate.MODE_NIGHT_YES -> Color.WHITE
            else -> Color.RED
        }
        val title = holder?.findViewById(android.R.id.title) as TextView
        title.setTextColor(color)
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.text_size_mid))

    }
}