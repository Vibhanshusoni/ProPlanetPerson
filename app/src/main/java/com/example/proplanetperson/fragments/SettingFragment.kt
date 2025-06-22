package com.example.proplanetperson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.materialswitch.MaterialSwitch
import androidx.fragment.app.Fragment

class SettingFragment : Fragment() {

    private lateinit var switchVisibility: MaterialSwitch // Change this to MaterialSwitch
    private lateinit var editEmail: EditText
    private lateinit var editMobile: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize the switch as MaterialSwitch
        switchVisibility = view.findViewById(R.id.switch_visibility)
        editEmail = view.findViewById(R.id.edit_email)
        editMobile = view.findViewById(R.id.edit_mobile)
        editPassword = view.findViewById(R.id.edit_password)
        btnSave = view.findViewById(R.id.btn_save_settings)

        btnSave.setOnClickListener {
            val isPublic = switchVisibility.isChecked
            val email = editEmail.text.toString()
            val mobile = editMobile.text.toString()
            val password = editPassword.text.toString()

            // For now, just show a Toast (you can connect Firebase later)
            Toast.makeText(
                requireContext(),
                "Saved:\nPublic: $isPublic\nEmail: $email\nMobile: $mobile\nPassword: $password",
                Toast.LENGTH_LONG
            ).show()
        }

        return view
    }
}
