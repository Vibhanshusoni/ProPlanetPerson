package com.example.proplanetperson.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proplanetperson.R
class StoryPagerAdapter(
    private val context: Context,
    private val storyImages: List<String>  // Now using URLs
) : RecyclerView.Adapter<StoryPagerAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val storyImage: ImageView = view.findViewById(R.id.storyImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_story_page, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val imageUrl = storyImages[position]
        Glide.with(context)
            .load(imageUrl)
            .into(holder.storyImage)
    }

    override fun getItemCount() = storyImages.size
}
