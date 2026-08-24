package com.sebassmith.cringemoment

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MomentDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moment_detail)

        val title = intent.getStringExtra("title") ?: ""
        val desc = intent.getStringExtra("desc") ?: ""
        val imageResId = intent.getIntExtra("imageResId", 0)

        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailDesc).text = desc
        findViewById<ImageView>(R.id.ivDetailImage).setImageResource(imageResId)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}