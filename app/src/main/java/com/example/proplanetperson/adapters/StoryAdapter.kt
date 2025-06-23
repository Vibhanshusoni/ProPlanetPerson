package com.example.proplanetperson.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log // Import Log for better error logging
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast // Import Toast for user feedback
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.AddStoryActivity
import com.example.proplanetperson.R
import com.example.proplanetperson.StoryActivity
import com.example.proplanetperson.models.Story
import com.example.proplanetperson.models.User
// Removed Firebase Auth imports
// import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.proplanetperson.utils.SessionManager // NEW: Import SessionManager
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

// Removed @NonNull from ViewHolder constructor for Kotlin idiomatic code
class StoryAdapter (private val mContent: Context, private val mStory: List<Story>) :
    RecyclerView.Adapter<StoryAdapter.ViewHolder>()
{
    // NEW: SessionManager instance
    private lateinit var sessionManager: SessionManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Initialize SessionManager here, it's safe as Context is available
        sessionManager = SessionManager(mContent)

        return if (viewType == 0)
        {
            val view = LayoutInflater.from(mContent).inflate(R.layout.add_story_item, parent, false)
            ViewHolder(view)
        }
        else
        {
            val view = LayoutInflater.from(mContent).inflate(R.layout.story_item, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = mStory[position]
        val currentUserId = sessionManager.getUserId() // Get current user ID from SessionManager

        // Access properties directly
        userInfo(holder, story.userId, position)


        if (holder.adapterPosition != 0)
        {
            // Access properties directly
            seenStory(holder, story.userId, currentUserId) // Pass currentUserId to seenStory
        }
        if (holder.adapterPosition == 0)
        {
            // Access properties directly
            // Ensure add_story_text and story_plus_btn are handled correctly when null
            myStory(holder.add_story_text, holder.story_plus_btn, false, currentUserId) // Pass currentUserId
        }


        holder.itemView.setOnClickListener{
            if (holder.adapterPosition == 0)
            {
                // Ensure add_story_text and story_plus_btn are handled correctly when null
                myStory(holder.add_story_text, holder.story_plus_btn, true, currentUserId) // Pass currentUserId
            }
            else
            {
                val intent = Intent(mContent, StoryActivity::class.java)
                // Access properties directly
                intent.putExtra("userId", story.userId)
                mContent.startActivity(intent)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) // Removed @NonNull
    {
        //In the following inner class we are acessing two layouts in a single Adapter(Stories/Add Story Function)

        //Story Item
        var story_image_seen: CircleImageView? = null
        var story_image: CircleImageView? = null
        var story_user_name: TextView? = null


        //Add Story Item layout
        var story_plus_btn: ImageView? = null
        var add_story_text: TextView? = null

        init {
            //Story Item
            story_image_seen = itemView.findViewById(R.id.story_image_seen)
            story_image = itemView.findViewById(R.id.story_image)
            story_user_name = itemView.findViewById(R.id.story_username)


            //Add Story Item layout
            story_plus_btn = itemView.findViewById(R.id.story_add)
            add_story_text = itemView.findViewById(R.id.add_story_text)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) {
            0 // For "Add Story" item
        } else {
            1 // For regular story item
        }
    }

    private fun userInfo(viewHolder: ViewHolder, userid:String, position: Int)
    {
        // IMPORTANT: Changed "Users" to "users" for consistency with other Firebase calls
        val userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue(User::class.java)

                    user?.let { nonNullUser -> // Use 'let' for safe access to 'user'
                        // Use safe calls for nullable ImageView/TextView
                        Picasso.get().load(nonNullUser.image).placeholder(R.drawable.profile).into(viewHolder.story_image!!) // !! is safe here because this is for story_item and story_image is present

                        if (position != 0){
                            Picasso.get().load(nonNullUser.image).placeholder(R.drawable.profile).into(viewHolder.story_image_seen!!) // !! safe
                            viewHolder.story_user_name!!.text = nonNullUser.username // !! safe
                        }
                    } ?: run {
                        Log.w("StoryAdapter", "User data found but object is null for UID: $userid")
                        // Fallback for null user object
                        viewHolder.story_image?.setImageResource(R.drawable.profile)
                        viewHolder.story_image_seen?.setImageResource(R.drawable.profile)
                        if (position != 0) viewHolder.story_user_name?.text = "Unknown"
                    }
                } else {
                    Log.d("StoryAdapter", "User data not found for UID: $userid")
                    // Fallback for non-existent user
                    viewHolder.story_image?.setImageResource(R.drawable.profile)
                    viewHolder.story_image_seen?.setImageResource(R.drawable.profile)
                    if (position != 0) viewHolder.story_user_name?.text = "Removed User"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StoryAdapter", "Failed to load user info for story: ${error.message}", error.toException())
                // Provide user feedback
                Toast.makeText(mContent, "Error loading user info.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Added currentUserId as a parameter, made TextView and ImageView nullable
    private fun myStory(textView: TextView?, imageView: ImageView?, click:Boolean, currentUserId: String?)
    {
        if (currentUserId.isNullOrEmpty()) {
            textView?.text = "Login to Add Story"
            imageView?.visibility = View.GONE
            if (click) Toast.makeText(mContent, "Please log in to add a story.", Toast.LENGTH_SHORT).show()
            return
        }

        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")
            .child(currentUserId) // Replaced FirebaseAuth with currentUserId

        storyRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {

                var counter = 0
                val timeCurrent = System.currentTimeMillis()

                for (snapshot in datasnapshot.children)
                {
                    val story = snapshot.getValue(Story::class.java)

                    story?.let { // Use 'let' for safe access to 'story'
                        // Access properties directly
                        if(timeCurrent > it.timeStart && timeCurrent < it.timeEnd)
                        {
                            counter++
                        }
                    }
                }

                if(click)
                {
                    if (counter > 0){
                        val alertDialog = AlertDialog.Builder(mContent).create()
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"View Story")
                        {
                                dialog, _ ->
                            val intent = Intent(mContent, StoryActivity::class.java)
                            intent.putExtra("userId", currentUserId) // Use currentUserId
                            mContent.startActivity(intent)
                            dialog.dismiss()
                        }
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Add Story")
                        {
                                dialog, _ ->
                            val intent = Intent(mContent, AddStoryActivity::class.java)
                            intent.putExtra("userId", currentUserId) // Use currentUserId
                            mContent.startActivity(intent)
                            dialog.dismiss()
                        }
                        alertDialog.show()

                    }
                    else
                    {
                        val intent = Intent(mContent, AddStoryActivity::class.java)
                        intent.putExtra("userId", currentUserId) // Use currentUserId
                        mContent.startActivity(intent)
                    }
                }
                else
                {
                    if (counter > 0){
                        textView?.text = "My Story"
                        imageView?.visibility = View.GONE
                    }
                    else
                    {
                        textView?.text = "Add Story"
                        imageView?.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StoryAdapter", "Failed to load user's story: ${error.message}", error.toException())
                Toast.makeText(mContent, "Error loading your story.", Toast.LENGTH_SHORT).show()
                textView?.text = "Error"
                imageView?.visibility = View.GONE
            }
        })
    }


    // Added currentUserId as a parameter for checking views
    private fun seenStory(viewHolder:ViewHolder, userId: String, currentUserId: String?) //to check whether story is seen or not
    {
        if (currentUserId.isNullOrEmpty()) {
            // Cannot determine seen status without current user, assume not seen or handle as needed
            viewHolder.story_image?.visibility = View.VISIBLE
            viewHolder.story_image_seen?.visibility = View.GONE
            return
        }

        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")
            .child(userId)

        storyRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {

                var i = 0

                for(snapshot in datasnapshot.children)
                {
                    val story = snapshot.getValue(Story::class.java)
                    story?.let { // Use 'let' for safe access to 'story'
                        // Check if current user has viewed the story
                        if (!snapshot.child("views").child(currentUserId).exists() // Replaced FirebaseAuth
                            && System.currentTimeMillis() < it.timeEnd) //checking if not seen and not expired
                        {
                            i++
                        }
                    }
                }

                if (i > 0)
                {
                    viewHolder.story_image?.visibility = View.VISIBLE
                    viewHolder.story_image_seen?.visibility = View.GONE
                }
                else
                {
                    viewHolder.story_image?.visibility = View.GONE
                    viewHolder.story_image_seen?.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StoryAdapter", "Failed to check seen status: ${error.message}", error.toException())
                Toast.makeText(mContent, "Error checking story status.", Toast.LENGTH_SHORT).show()
                viewHolder.story_image?.visibility = View.VISIBLE // Default to showing as unseen on error
                viewHolder.story_image_seen?.visibility = View.GONE
            }
        })
    }
}