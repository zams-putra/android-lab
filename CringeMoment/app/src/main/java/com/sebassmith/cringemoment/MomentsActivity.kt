package com.sebassmith.cringemoment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MomentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moments)

        val rv = findViewById<RecyclerView>(R.id.rvMoments)
        rv.layoutManager = LinearLayoutManager(this)

        val moments = listOf(
            Moment("First title kamu", "Deskripsi singkat momen ini.", R.drawable.moment1),
            Moment("Judul kedua", "Deskripsi lain di sini.", R.drawable.moment2)
            // tambahin sesuai jumlah momen yang kamu mau
        )

        rv.adapter = MomentsAdapter(moments)
    }
}