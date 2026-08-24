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
        val secretMoment = intent.getStringExtra("secret_moment")

        val moments = listOf(
            // tinggal tambahin datanya
            Moment("BreakDance di depan teman teman", "Hahahahahaha ingatt momenmu saat breakdance secara tiba tiba, gilak crinj bgt bjir", R.drawable.moment1),
            Moment("Duduk keren mengamati orang sekitar", "HAHAHHAHA duduk pose dingin lalu berharap orang lain melihatmu seperti pria dingin berbahaya dan misterius", R.drawable.moment2)
        )

//        if (secretMoment != null) {
//            moments.add(Moment("Locked Memory", secretMoment, R.drawable.moment_secret))
//        }

        rv.adapter = MomentsAdapter(moments)
    }
}

