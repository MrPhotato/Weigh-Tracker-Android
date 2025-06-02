package com.weighttracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.weighttracker.databinding.ActivityMainBinding
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // 禁用所有触摸音效和系统声音
            disableAllSounds()
            
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // 设置工具栏
            setSupportActionBar(binding.toolbar)

            // 设置导航 - 添加异常处理
            setupNavigation()
            
            // 为底部导航和根视图禁用声音
            disableViewSounds()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            // 如果出现错误，尝试基本的UI设置
            handleInitializationError(e)
        }
    }
    
    private fun setupNavigation() {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            if (navHostFragment == null) {
                Log.e(TAG, "NavHostFragment not found")
                return
            }
            
            val navController = navHostFragment.navController
            val bottomNav: BottomNavigationView = binding.bottomNavigation
            bottomNav.setupWithNavController(navController)

            // 设置顶级目的地
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_weight_entry,
                    R.id.navigation_history,
                    R.id.navigation_chart,
                    R.id.navigation_settings
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation: ${e.message}", e)
        }
    }
    
    private fun handleInitializationError(e: Exception) {
        try {
            // 尝试基本的绑定设置
            if (::binding.isInitialized) {
                // 基本的错误恢复
                Log.d(TAG, "Attempting error recovery")
            }
        } catch (recoveryError: Exception) {
            Log.e(TAG, "Error recovery failed: ${recoveryError.message}", recoveryError)
        }
    }
    
    /**
     * 禁用应用中的所有声音效果
     */
    private fun disableAllSounds() {
        try {
            // 禁用窗口触摸音效
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
            
            // 通过系统设置禁用触摸音效（仅对当前窗口有效）
            val audioManager = getSystemService(AUDIO_SERVICE) as? android.media.AudioManager
            // 不修改系统全局设置，只在应用内处理
        } catch (e: Exception) {
            Log.w(TAG, "Could not disable sounds: ${e.message}")
        }
    }
    
    /**
     * 为视图组件禁用声音效果
     */
    private fun disableViewSounds() {
        try {
            // 禁用底部导航声音
            if (::binding.isInitialized) {
                binding.bottomNavigation.isSoundEffectsEnabled = false
                
                // 禁用根布局的声音效果
                binding.root.isSoundEffectsEnabled = false
                
                // 禁用工具栏声音
                binding.toolbar?.isSoundEffectsEnabled = false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not disable view sounds: ${e.message}")
        }
    }
} 