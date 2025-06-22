package com.example.proplanetperson.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.proplanetperson.databinding.BottomSheetUploadOptionsBinding
import com.example.proplanetperson.MediaUploadActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UploadOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetUploadOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetUploadOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            binding.btnReel.setOnClickListener {
                launchMediaUpload("reel")
            }

            binding.btnPhoto.setOnClickListener {
                launchMediaUpload("photo")
            }

            binding.btnStory.setOnClickListener {
                launchMediaUpload("story")
            }
        }

    private fun launchMediaUpload(type: String) {
        val intent = Intent(requireContext(), MediaUploadActivity::class.java)
        intent.putExtra("mediaType", type)
        startActivity(intent)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}