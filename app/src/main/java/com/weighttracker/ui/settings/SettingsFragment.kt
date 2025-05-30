package com.weighttracker.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.weighttracker.databinding.FragmentSettingsBinding
import com.weighttracker.notification.NotificationScheduler
import com.weighttracker.util.PreferenceManager
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // 设置提醒开关状态
        binding.switchReminder.isChecked = preferenceManager.isReminderEnabled
        
        // 显示当前提醒时间
        updateTimeDisplay()
    }

    private fun setupClickListeners() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.isReminderEnabled = isChecked
            
            if (isChecked) {
                // 启用提醒
                NotificationScheduler.scheduleNotification(
                    requireContext(),
                    preferenceManager.reminderHour,
                    preferenceManager.reminderMinute
                )
            } else {
                // 取消提醒
                NotificationScheduler.cancelNotification(requireContext())
            }
        }

        binding.layoutTimePicker.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showTimePicker() {
        val currentHour = preferenceManager.reminderHour
        val currentMinute = preferenceManager.reminderMinute

        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                preferenceManager.reminderHour = hour
                preferenceManager.reminderMinute = minute
                updateTimeDisplay()
                
                // 如果提醒已启用，重新设置时间
                if (preferenceManager.isReminderEnabled) {
                    NotificationScheduler.scheduleNotification(
                        requireContext(),
                        hour,
                        minute
                    )
                }
            },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    private fun updateTimeDisplay() {
        val hour = preferenceManager.reminderHour
        val minute = preferenceManager.reminderMinute
        binding.tvReminderTime.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 