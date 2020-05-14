package io.treehouses.remote.utils

import android.content.Context
import android.content.pm.PackageManager

object VersionUtils {
    fun getVersionCode(context: Context?): Int {
        try {
            return if (context != null) {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionCode
            } else {
                -1
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getVersionName(context: Context?): String {
        try {
            return if (context != null) {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName
            } else {
                "ERROR"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}