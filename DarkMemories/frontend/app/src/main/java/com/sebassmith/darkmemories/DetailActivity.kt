package com.sebassmith.darkmemories

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DetailActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val title = intent.getStringExtra("title") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val desc = intent.getStringExtra("desc") ?: ""
        val imgURL = intent.getStringExtra("img_url") ?: ""


        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailDate).text = date
        findViewById<TextView>(R.id.tvDetailDesc).text = desc

        Glide.with(this)
            .load(imgURL)
            .into(findViewById<ImageView>(R.id.ivDetailPhoto))
    }
}