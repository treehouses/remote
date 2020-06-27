package io.treehouses.remote.Views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import io.treehouses.remote.R

class CustomSwitchPref @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0)
    : SwitchPreferenceCompat(context, attrs, defStyle) {
    private var switchCompat: SwitchCompat? = null
    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val nightMode = when(AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_YES -> true
            else -> false
        }
        val titleTv = (holder?.findViewById(R.id.title) as TextView)
        titleTv.text = title
        titleTv.setTextColor(if (nightMode) Color.GRAY else Color.BLACK)
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.text_size_mid))
        titleTv.setPadding(context.resources.getDimensionPixelSize(R.dimen.margin_huge), titleTv.paddingTop, titleTv.paddingRight, titleTv.paddingBottom)

        val summaryTv = (holder.findViewById(R.id.summary) as TextView)
        summaryTv.text = summary
        summaryTv.setTextColor(if (nightMode) Color.DKGRAY else Color.GRAY)
        summaryTv.setPadding(context.resources.getDimensionPixelSize(R.dimen.margin_huge), titleTv.paddingTop, titleTv.paddingRight, titleTv.paddingBottom)

        switchCompat = (holder.findViewById(R.id.switchPref) as SwitchCompat)
        switchCompat?.isChecked = this.isChecked
    }

    override fun onClick() {
        super.onClick()
        switchCompat?.isChecked = this.isChecked
    }
}