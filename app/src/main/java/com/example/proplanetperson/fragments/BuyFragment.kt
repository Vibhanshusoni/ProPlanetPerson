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

class BuyFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_buy, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewBuy)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Replace with your actual data loading logic
        val buyItems = listOf(
            Item("Plant 1", 10.99),
            Item("Fertilizer 500g", 5.50),
            // Add more buy items
        )

        recyclerView.adapter = ItemAdapter(buyItems)

        return view
    }
}