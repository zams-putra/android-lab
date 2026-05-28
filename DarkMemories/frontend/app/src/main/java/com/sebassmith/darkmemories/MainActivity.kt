package com.sebassmith.darkmemories

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sebassmith.darkmemories.adapter.PhotoAdapter
import com.sebassmith.darkmemories.network.ApiClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val lvPhotos = findViewById<ListView>(R.id.lvPhotos)

        lifecycleScope.launch {
            try {
                val resp = ApiClient.service.getPhotos(ApiClient.API_KEY)
                val adapter = PhotoAdapter(this@MainActivity, resp.photos)
                lvPhotos.adapter = adapter
                tvStatus.text = "Loaded ${resp.photos.size} photos"
            } catch (e: Exception){
                tvStatus.text = "Error: ${e.message}"
            }

        }

    }
}