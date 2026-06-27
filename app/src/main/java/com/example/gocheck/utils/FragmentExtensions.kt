package com.example.gocheck.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.gocheck.R

/**
 * Extension functions untuk Fragment
 * Memudahkan handling bottom navigation dan window insets
 */

/**
 * Apply bottom navigation padding ke view
 * Gunakan ini di fragment yang memiliki button/content di bagian bawah
 *
 * Usage:
 * binding.myButton.applyBottomNavPadding()
 */
fun View.applyBottomNavPadding(extraPadding: Int = 0) {
    val bottomNavHeight = context.resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
    updatePadding(
        bottom = paddingBottom + bottomNavHeight + extraPadding
    )
}

/**
 * Setup window insets untuk fragment
 * Otomatis handle system bars dan navigation bars
 *
 * Usage:
 * binding.root.setupFragmentInsets()
 */
fun View.setupFragmentInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val systemBars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.navigationBars()
        )

        view.updatePadding(
            top = systemBars.top,
            bottom = systemBars.bottom
        )

        windowInsets
    }
}

/**
 * Setup window insets hanya untuk bottom (navigation bar)
 * Gunakan ini jika hanya perlu handle bottom navigation
 *
 * Usage:
 * binding.actionButtons.setupBottomInsets()
 */
fun View.setupBottomInsets(additionalPadding: Int = 0) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

        view.updatePadding(
            bottom = paddingBottom + navBars.bottom + additionalPadding
        )

        WindowInsetsCompat.CONSUMED
    }
}

/**
 * Get bottom navigation height from resources
 */
fun Fragment.getBottomNavHeight(): Int {
    return resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
}

/**
 * Get navigation bar height (system navigation)
 */
fun Fragment.getNavigationBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else {
        0
    }
}

/**
 * Get total bottom spacing (bottom nav + system nav)
 */
fun Fragment.getTotalBottomSpacing(): Int {
    return getBottomNavHeight() + getNavigationBarHeight()
}

/**
 * Hide bottom navigation (jika diperlukan, misal untuk fullscreen camera)
 */
fun Fragment.hideBottomNavigation() {
    (activity as? com.example.gocheck.ui.activity.MainActivity)?.apply {
        // Implementation di MainActivity
    }
}

/**
 * Show bottom navigation
 */
fun Fragment.showBottomNavigation() {
    (activity as? com.example.gocheck.ui.activity.MainActivity)?.apply {
        // Implementation di MainActivity
    }
}

/**
 * Check if fragment is visible
 */
fun Fragment.isFragmentVisible(): Boolean {
    return isVisible && isAdded && !isHidden
}

/**
 * Safe navigate dengan null check
 */
inline fun Fragment.safeNavigate(action: () -> Unit) {
    if (isFragmentVisible()) {
        try {
            action()
        } catch (e: Exception) {
            // Handle navigation exception
            e.printStackTrace()
        }
    }
}