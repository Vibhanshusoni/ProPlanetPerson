package com.example.proplanetperson.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proplanetperson.R

class UploadPostFragment : Fragment(R.layout.fragment_upload_post) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uploadButton = view.findViewById<Button>(R.id.uploadButton)
        uploadButton.setOnClickListener {
            val uploadSheet = UploadOptionsBottomSheet()
            uploadSheet.show(parentFragmentManager, "UploadOptions")
            // Placeholder: Handle the upload post functionality
            Toast.makeText(requireContext(), "Upload Post clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
