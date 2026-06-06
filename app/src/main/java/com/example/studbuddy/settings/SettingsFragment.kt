package com.example.studbuddy.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import com.google.android.material.card.MaterialCardView

class SettingsFragment : Fragment() {

    private lateinit var cbDarkMode: CheckBox
    private lateinit var cardNotifications: MaterialCardView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsManager = (requireActivity().application as StudBuddyApp).settingsManager

        cbDarkMode = view.findViewById(R.id.cbDarkMode)
        cardNotifications = view.findViewById(R.id.cardNotifications)

        // Dark Mode Logic
        cbDarkMode.isChecked = settingsManager.darkModeEnabled
        cbDarkMode.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.darkModeEnabled = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Navigate to Notifications Sub-page
        cardNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_notificationSettingsFragment)
        }
    }
}
