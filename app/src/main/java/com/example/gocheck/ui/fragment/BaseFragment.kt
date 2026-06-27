package com.example.gocheck.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment

/**
 * Base Fragment untuk semua fragment di aplikasi
 * Menyediakan common functionality seperti:
 * - Window insets handling
 * - Bottom navigation awareness
 * - Loading state management
 */
abstract class BaseFragment : Fragment() {

    /**
     * Override ini untuk enable automatic bottom padding
     * Return view yang perlu diberi padding untuk avoid bottom navigation
     */
    protected open val viewsNeedingBottomPadding: List<View>
        get() = emptyList()

    /**
     * Override ini untuk enable automatic window insets
     */
    protected open val enableWindowInsets: Boolean
        get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (enableWindowInsets) {
            setupWindowInsets(view)
        }

        if (viewsNeedingBottomPadding.isNotEmpty()) {
            applyBottomPaddingToViews()
        }
    }

    /**
     * Setup window insets untuk fragment
     */
    private fun setupWindowInsets(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.navigationBars()
            )

            onInsetsReceived(systemBars.top, systemBars.bottom)

            windowInsets
        }
    }

    /**
     * Apply bottom padding untuk views yang membutuhkan
     */
    private fun applyBottomPaddingToViews() {
        val bottomNavHeight = getBottomNavHeight()
        val additionalPadding = getAdditionalBottomPadding()

        viewsNeedingBottomPadding.forEach { view ->
            view.updatePadding(
                bottom = view.paddingBottom + bottomNavHeight + additionalPadding
            )
        }
    }

    /**
     * Override ini untuk custom insets handling
     */
    protected open fun onInsetsReceived(topInset: Int, bottomInset: Int) {
        // Override in child fragments if needed
    }

    /**
     * Get bottom navigation height
     */
    protected fun getBottomNavHeight(): Int {
        return try {
            resources.getDimensionPixelSize(
                resources.getIdentifier("bottom_nav_height", "dimen", requireContext().packageName)
            )
        } catch (e: Exception) {
            56.dpToPx() // Default 56dp
        }
    }

    /**
     * Override untuk additional padding jika diperlukan
     */
    protected open fun getAdditionalBottomPadding(): Int = 16.dpToPx()

    /**
     * Convert dp to px
     */
    protected fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    /**
     * Show loading state
     */
    protected open fun showLoading() {
        // Override in child fragments
    }

    /**
     * Hide loading state
     */
    protected open fun hideLoading() {
        // Override in child fragments
    }

    /**
     * Show error message
     */
    protected open fun showError(message: String) {
        // Override in child fragments
    }
}