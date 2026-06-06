package com.example.studbuddy.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studbuddy.R
import com.google.android.material.card.MaterialCardView

class SettingsFragment : Fragment() {

    private lateinit var cardNotifications: MaterialCardView
    private lateinit var cardAppearance: MaterialCardView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardNotifications = view.findViewById(R.id.cardNotifications)
        cardAppearance = view.findViewById(R.id.cardAppearance)

        // Navigate to Notifications Sub-page
        cardNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_notificationSettingsFragment)
        }

        // Navigate to Appearance Sub-page
        cardAppearance.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_appearanceSettingsFragment)
        }
    }
}
