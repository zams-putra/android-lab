package com.sebassmith.rahasiasidi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val secret_uname = "Sebusman"
        val secret_pass = "Rahas14_S3BuS"
        btnLogin.setOnClickListener{
            val uname = etUsername.text.toString()
            val pass = etPassword.text.toString()
            if(uname == secret_uname && pass == secret_pass) {
                Toast.makeText(this, "FLAG{606e01fb1d845d8130c4aa9712a76f07}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid Creds gabole masuk", Toast.LENGTH_SHORT).show()
            }
        }
    }
}