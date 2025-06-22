package com.example.proplanetperson

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class CommentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val imageView = findViewById<ImageView>(R.id.postImageView)
        val captionView = findViewById<TextView>(R.id.captionTextView)

        val postUrl = intent.getStringExtra("postUrl")
        val caption = intent.getStringExtra("caption")

        captionView.text = caption
        Glide.with(this).load(postUrl).into(imageView)
    }
}
