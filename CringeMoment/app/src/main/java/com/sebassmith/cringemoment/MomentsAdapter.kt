package com.sebassmith.cringemoment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MomentsAdapter(private val moments: List<Moment>) :
    RecyclerView.Adapter<MomentsAdapter.MomentViewHolder>() {

    class MomentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.ivMoment)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val desc: TextView = view.findViewById(R.id.tvDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MomentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_moment, parent, false)
        return MomentViewHolder(view)
    }

    override fun onBindViewHolder(holder: MomentViewHolder, position: Int) {
        val moment = moments[position]
        holder.title.text = moment.title
        holder.desc.text = moment.desc
        holder.img.setImageResource(moment.imageResId)

        // intent ke detail
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MomentDetailActivity::class.java)
            intent.putExtra("title", moment.title)
            intent.putExtra("desc", moment.desc)
            intent.putExtra("imageResId", moment.imageResId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = moments.size
}