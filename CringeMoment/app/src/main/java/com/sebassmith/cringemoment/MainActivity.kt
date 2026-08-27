package com.sebassmith.cringemoment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPin = findViewById<EditText>(R.id.etPin)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)
        val hardcodedPin = "6969"


        btnUnlock.setOnClickListener {
            if (RootCheck.isBlocked()) {
//                custom alert cuy hhhhh
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Access Denied")
                    .setMessage("Rooted/emulator environment detected. Vault access blocked.")
                    .setPositiveButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                return@setOnClickListener
            }

            val secret = VaultNative.decodeSecret()

            if (etPin.text.toString() == hardcodedPin) {
                val intent = Intent(this, MomentsActivity::class.java)
                intent.putExtra("secret_moment", secret)
                startActivity(intent)
            } else {
                tvResult.text = "PIN salah, gausah perlu tau moment crinj ku."
            }
        }
    }
}