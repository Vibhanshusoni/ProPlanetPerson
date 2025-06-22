package com.example.proplanetperson.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText // Import EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.adapters.UserAdapter
import com.example.proplanetperson.models.User
import com.example.proplanetperson.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
// Removed deprecated kotlinx.android.synthetic imports
// import kotlinx.android.synthetic.main.fragment_search.*
// import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null
    private lateinit var searchEditText: EditText // Declare EditText for search input

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recyclerview_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        //to show a user on search
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        // Initialize the EditText
        searchEditText = view.findViewById(R.id.searchitem) // Assuming this ID in fragment_search.xml
        // IMPORTANT: Make sure the ID 'search_input_edit_text' exists in your fragment_search.xml
        // If your EditText has a different ID (e.g., 'searchitem'), update it here.

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (searchEditText.text.toString().isEmpty()) { // Use searchEditText
                    // Handle empty search case, e.g., clear results or show all users
                    mUser?.clear()
                    userAdapter?.notifyDataSetChanged()
                    recyclerView?.visibility = View.GONE // Hide RecyclerView when search is empty
                } else {
                    recyclerView?.visibility = View.VISIBLE
                    retrieveUser() // This might be redundant if searchUser covers all cases
                    searchUser(s.toString().lowercase()) // Use searchEditText
                }
            }
        })
        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("username")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                // Log the error for debugging
                Log.e("SearchFragment", "Database error in searchUser: ${error.message}", error.toException())
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                mUser?.clear() // Safe call

                for (snapshot in datasnapshot.children) {
                    //searching all users
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        mUser?.add(user) // Safe call
                    }
                }
                userAdapter?.notifyDataSetChanged() // Safe call
            }
        })
    }

    private fun retrieveUser() {
        val usersSearchRef = FirebaseDatabase.getInstance().reference.child("Users") //table name:Users
        usersSearchRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Could not read from Database", Toast.LENGTH_LONG).show()
                Log.e("SearchFragment", "Database error in retrieveUser: ${error.message}", error.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Check if the search field is empty. This logic might be better handled in onTextChanged.
                // If this function is only called when search is not empty, this if-condition is problematic.
                // Assuming it's meant to show all users when search is empty.
                if (searchEditText.text.toString().isEmpty()) { // Use searchEditText
                    mUser?.clear() // Safe call
                    for (snapShot in dataSnapshot.children) {
                        val user = snapShot.getValue(User::class.java)
                        if (user != null) {
                            mUser?.add(user) // Safe call
                        }
                    }
                    userAdapter?.notifyDataSetChanged() // Safe call
                }
            }
        })
    }
}
