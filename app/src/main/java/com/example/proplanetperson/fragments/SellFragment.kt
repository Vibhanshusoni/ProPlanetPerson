package com.example.proplanetperson.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.R
import com.example.proplanetperson.adapters.ItemAdapter
import com.example.proplanetperson.models.Item

class SellFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sell, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewSell)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Replace with your actual data loading logic for selling items
        val sellItems = listOf(
            Item("Homegrown Tomato Seeds", 2.50),
            Item("Used Gardening Gloves", 3.00),
            // Add more sell items
        )

        recyclerView.adapter = ItemAdapter(sellItems)

        return view
    }
}