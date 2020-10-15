package com.example.brazedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.segment.analytics.Analytics
import com.segment.analytics.Properties

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.click)

        button.setOnClickListener {

            Analytics.with(applicationContext)
                .track("Full Name", Properties().putName("Mayank Malhotra"))

            Analytics.with(applicationContext)
                .track("Currency", Properties().putCurrency("INR"))

            Analytics.with(applicationContext)
                .track("Gender", Properties().putCurrency("Male"))

            Analytics.with(applicationContext)
                .track("Price", Properties().putCurrency("20$"))


        }
    }
}