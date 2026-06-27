package com.example.gocheck.ui.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.gocheck.R
import com.example.gocheck.databinding.ActivityMainBinding
import com.example.gocheck.ui.fragment.CameraFragment
import com.example.gocheck.ui.fragment.HistoryFragment
import com.example.gocheck.ui.fragment.HomeFragment
import com.example.gocheck.ui.fragment.InputFragment
import com.example.gocheck.ui.fragment.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isCameraActive = false
    private var currentFragmentTag: String = TAG_HOME

    // Fragment instances
    private val homeFragment by lazy { HomeFragment() }
    private val searchFragment by lazy { SearchFragment() }
    private val inputFragment by lazy { InputFragment() }
    private val cameraFragment by lazy { CameraFragment() }
    private val historyFragment by lazy { HistoryFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengatur agar layout bisa meluas ke area sistem (tapi kita batasi dengan padding nanti)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setupWindowInsets() // Setup padding agar TIDAK tertimpa status bar
        setupBackHandling()

        if (savedInstanceState == null) {
            replaceFragment(homeFragment, TAG_HOME)
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            updateStatusBarForFragment(TAG_HOME)
            updateCameraFabState()
        } else {
            currentFragmentTag = savedInstanceState.getString(KEY_CURRENT_FRAGMENT, TAG_HOME)
            isCameraActive = savedInstanceState.getBoolean(KEY_CAMERA_ACTIVE, false)

            updateCameraFabState()

            if (isCameraActive) {
                clearBottomNavSelection()
                updateStatusBarForFragment(TAG_CAMERA)
            } else {
                updateStatusBarForFragment(currentFragmentTag)
            }
        }

        setupBottomNavigation()
        setupCameraFab()
    }

    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isCameraActive) {
                    returnToPreviousFragment()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // PERBAIKAN UTAMA: Padding SELALU diterapkan untuk semua fragment
    private fun setupWindowInsets() {
        // Handle Padding Atas (Status Bar) untuk Container Fragment
        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Kita SELALU ambil top inset, agar tidak tertimpa
            view.updatePadding(top = systemBars.top)
            windowInsets
        }

        // Handle Padding Bawah (Nav Bar) untuk BottomAppBar
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomAppBar) { view, windowInsets ->
            val navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navBars.bottom)
            windowInsets
        }
    }

    private fun updateStatusBarForFragment(tag: String) {
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val wic = WindowCompat.getInsetsController(window, window.decorView)


        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        wic.isAppearanceLightStatusBars = true // Icon Hitam
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            if (isCameraActive) {
                isCameraActive = false
                updateCameraFabState()
            }

            val (fragment, tag) = when (menuItem.itemId) {
                R.id.nav_home -> homeFragment to TAG_HOME
                R.id.nav_search -> searchFragment to TAG_SEARCH
                R.id.nav_input -> inputFragment to TAG_INPUT
                R.id.nav_history -> historyFragment to TAG_HISTORY
                else -> homeFragment to TAG_HOME
            }

            replaceFragment(fragment, tag)
            updateStatusBarForFragment(tag)
            true
        }
    }

    // Di dalam class MainActivity

    private fun setupCameraFab() {
        binding.fabCamera.setOnClickListener {

            if (isCameraActive) {

                if (cameraFragment.isAdded) {
                    cameraFragment.resetCamera()

                    // Opsional: Animasi memutar ikon sebentar sebagai feedback visual
                    binding.fabCamera.animate().rotationBy(360f).setDuration(400).start()
                }

                return@setOnClickListener
            }

            // JIKA KAMERA BELUM AKTIF (Masuk ke mode kamera)
            isCameraActive = true
            updateCameraFabState()
            clearBottomNavSelection()

            replaceFragment(cameraFragment, TAG_CAMERA)
            updateStatusBarForFragment(TAG_CAMERA)
        }
    }

    private fun returnToPreviousFragment() {
        isCameraActive = false
        updateCameraFabState()

        val navId = when (currentFragmentTag) {
            TAG_HOME -> R.id.nav_home
            TAG_SEARCH -> R.id.nav_search
            TAG_INPUT -> R.id.nav_input
            TAG_HISTORY -> R.id.nav_history
            else -> R.id.nav_home
        }

        binding.bottomNavigation.selectedItemId = navId
    }

    private fun clearBottomNavSelection() {
        binding.bottomNavigation.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigation.menu.size()) {
            binding.bottomNavigation.menu.getItem(i).isChecked = false
        }
        binding.bottomNavigation.menu.setGroupCheckable(0, true, true)
    }

    private fun updateCameraFabState() {
        val targetColorRes = if (isCameraActive) R.color.primary_green else R.color.white
        val targetIconColorRes = if (isCameraActive) R.color.white else R.color.text_secondary
        val targetRotation = if (isCameraActive) 45f else 0f

        binding.fabCamera.backgroundTintList = ContextCompat.getColorStateList(this, targetColorRes)
        binding.fabCamera.imageTintList = ContextCompat.getColorStateList(this, targetIconColorRes)
        binding.fabCamera.animate().rotation(targetRotation).setDuration(200).start()
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        if (tag != TAG_CAMERA) {
            currentFragmentTag = tag
        }

        supportFragmentManager.beginTransaction().apply {
            // Animasi Fade sederhana agar transisi halus
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            replace(R.id.fragment_container, fragment, tag)
            commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_FRAGMENT, currentFragmentTag)
        outState.putBoolean(KEY_CAMERA_ACTIVE, isCameraActive)
    }

    companion object {
        private const val TAG_HOME = "HOME"
        private const val TAG_SEARCH = "SEARCH"
        private const val TAG_INPUT = "INPUT"
        private const val TAG_CAMERA = "CAMERA"
        private const val TAG_HISTORY = "HISTORY"

        private const val KEY_CURRENT_FRAGMENT = "current_fragment"
        private const val KEY_CAMERA_ACTIVE = "camera_active"
    }
}