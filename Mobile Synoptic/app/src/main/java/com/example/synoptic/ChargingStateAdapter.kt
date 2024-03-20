package com.example.synoptic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChargingStateAdapter(private val chargingStateItems: List<String>) :
    RecyclerView.Adapter<ChargingStateAdapter.ChargingStateViewHolder>() {

    inner class ChargingStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.txtvTimeStamp)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChargingStateAdapter.ChargingStateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_list, parent, false)
        return ChargingStateViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChargingStateAdapter.ChargingStateViewHolder, position: Int) {
        val chargingState = chargingStateItems[position]
        holder.textView.text = chargingState
    }

    override fun getItemCount(): Int {
        return chargingStateItems.size

    }
}