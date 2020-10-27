package com.example.brazedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appboy.Appboy
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Appboy.getInstance(applicationContext).subscribeToContentCardsUpdates {
            var count = 0
            for (i in 0.until(it.cardCount)) {
                if (it.allCards[i].extras.containsKey("feed_type")) {
                    val feedType = it.allCards[i].extras["feed_type"]
                    val desiredFeedType = "Delhi"
                    if (desiredFeedType == feedType) {
                        count++
                    }
                }
            }
            headline.text = count.toString()
        }
    }
}