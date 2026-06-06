package com.example.studbuddy.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.studbuddy.R
import com.example.studbuddy.StudBuddyApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppearanceSettingsFragment : Fragment() {

    private lateinit var rgThemeMode: RadioGroup
    private lateinit var rbSystem: RadioButton
    private lateinit var rbLight: RadioButton
    private lateinit var rbDark: RadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_appearance_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsManager = (requireActivity().application as StudBuddyApp).settingsManager

        rgThemeMode = view.findViewById(R.id.rgThemeMode)
        rbSystem = view.findViewById(R.id.rbSystem)
        rbLight = view.findViewById(R.id.rbLight)
        rbDark = view.findViewById(R.id.rbDark)

        // Initialize from SettingsManager
        viewLifecycleOwner.lifecycleScope.launch {
            val mode = settingsManager.themeMode.first()
            when (mode) {
                AppCompatDelegate.MODE_NIGHT_NO -> rbLight.isChecked = true
                AppCompatDelegate.MODE_NIGHT_YES -> rbDark.isChecked = true
                else -> rbSystem.isChecked = true
            }
        }

        rgThemeMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            viewLifecycleOwner.lifecycleScope.launch {
                settingsManager.setThemeMode(mode)
            }
        }
    }
}
