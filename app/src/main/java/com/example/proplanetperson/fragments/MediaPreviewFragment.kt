package com.example.proplanetperson.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.VideoView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.proplanetperson.R
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MediaPreviewFragment : Fragment(R.layout.fragment_media_preview) {

    private var mediaUri: Uri? = null
    private var mediaType: String? = null // "image" or "video"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = view.findViewById<ImageView>(R.id.previewImage)
        val videoView = view.findViewById<VideoView>(R.id.previewVideo)
        val uploadButton = view.findViewById<Button>(R.id.btnUpload)

        mediaUri = arguments?.getParcelable("mediaUri")
        mediaType = arguments?.getString("mediaType")

        if (mediaType == "image") {
            videoView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            Glide.with(this).load(mediaUri).into(imageView)
        } else if (mediaType == "video") {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(mediaUri)
            videoView.start()
        }

        uploadButton.setOnClickListener {
            mediaUri?.let {
                uploadToFirebase(it)
            }
        }
    }

    private fun uploadToFirebase(uri: Uri) {
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("uploads/$fileName")

        ref.putFile(uri)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Upload successful!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    companion object {
        fun newInstance(uri: Uri, type: String): MediaPreviewFragment {
            val fragment = MediaPreviewFragment()
            val args = Bundle()
            args.putParcelable("mediaUri", uri)
            args.putString("mediaType", type)
            fragment.arguments = args
            return fragment
        }
    }
}
