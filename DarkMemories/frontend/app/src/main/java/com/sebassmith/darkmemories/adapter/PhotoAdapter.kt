package com.sebassmith.darkmemories.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sebassmith.darkmemories.DetailActivity
import com.sebassmith.darkmemories.R
import com.sebassmith.darkmemories.model.Photo


class PhotoAdapter(context: Context, photos: List<Photo>): ArrayAdapter<Photo>(context, 0, photos) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_photo, parent, false)
        val photo = getItem(position)!!

        val ivPhoto = view.findViewById<ImageView>(R.id.ivPhoto)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)

        tvTitle.text = photo.title
        tvDate.text = photo.date

        Glide.with(context)
            .load(photo.imageURL)
            .into(ivPhoto)


        view.setOnClickListener {
            val intent = android.content.Intent(context, DetailActivity::class.java).apply {
                putExtra("title", photo.title)
                putExtra("desc", photo.desc)

                putExtra("date", photo.date)
                putExtra("img_url", photo.imageURL)

            }
            context.startActivity(intent)
        }

        return  view
    }

}
