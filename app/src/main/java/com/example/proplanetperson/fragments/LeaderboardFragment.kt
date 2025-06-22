package com.example.proplanetperson.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.proplanetperson.R

class LeaderboardFragment : Fragment(R.layout.fragment_leader_board) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Example: Show leaderboard scores (mocked)
        val leaderboardText = view.findViewById<TextView>(R.id.leaderboardText)
        leaderboardText.text = "Leaderboard: 1st - John Doe, 2nd - Jane Smith"
    }
}
