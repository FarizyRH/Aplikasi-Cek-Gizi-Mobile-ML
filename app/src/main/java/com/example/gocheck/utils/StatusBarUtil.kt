package com.example.gocheck.utils

import android.app.Activity
import androidx.annotation.ColorInt
import androidx.core.view.WindowCompat

object StatusBarUtil {

    /**
     * Ubah Warna Background Status Bar
     */
    fun setStatusBarColor(activity: Activity, @ColorInt color: Int) {
        activity.window.statusBarColor = color
    }

    /**
     * Mode LIGHT Status Bar (Background Terang -> Ikon HITAM)
     * Gunakan ini saat background layar PUTIH.
     */
    fun useDarkIcons(activity: Activity) {
        val window = activity.window
        val wic = WindowCompat.getInsetsController(window, window.decorView)
        // true = I am a light status bar, so please make icons dark
        wic.isAppearanceLightStatusBars = true
    }

    /**
     * Mode DARK Status Bar (Background Gelap -> Ikon PUTIH)
     * Gunakan ini saat background layar HIJAU/GELAP.
     */
    fun useLightIcons(activity: Activity) {
        val window = activity.window
        val wic = WindowCompat.getInsetsController(window, window.decorView)
        // false = I am NOT a light status bar, make icons light/white
        wic.isAppearanceLightStatusBars = false
    }
}