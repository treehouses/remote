package io.treehouses.remote.Views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import io.treehouses.remote.R

class CustomPref @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0)
    : Preference(context, attrs, defStyle) {

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val nightMode = when(AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_YES -> true
            else -> false
        }
        val title = (holder?.findViewById(android.R.id.title) as TextView)
        title.setTextColor(if (nightMode) Color.GRAY else Color.BLACK)
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.text_size_mid))
        title.setPadding(context.resources.getDimensionPixelSize(R.dimen.margin_huge), title.paddingTop, title.paddingRight, title.paddingBottom)

        val description = (holder.findViewById(android.R.id.summary) as TextView)
        description.setTextColor(if (nightMode) Color.DKGRAY else Color.GRAY)
        description.setPadding(context.resources.getDimensionPixelSize(R.dimen.margin_huge), title.paddingTop, title.paddingRight, title.paddingBottom)
    }

}