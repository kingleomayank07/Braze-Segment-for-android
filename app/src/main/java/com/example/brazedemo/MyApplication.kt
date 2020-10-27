package com.example.brazedemo

import android.app.Application
import com.appboy.Appboy
import com.appboy.AppboyLifecycleCallbackListener
import com.segment.analytics.Analytics
import com.segment.analytics.android.integrations.appboy.AppboyIntegration
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val analytics: Analytics = Analytics.Builder(
            this, getString(R.string.write_key)
        ) // Enable this to record certain application events automatically!
            .trackApplicationLifecycleEvents() // Enable this to record screen views automatically!
            .recordScreenViews()
            .use(AppboyIntegration.FACTORY)
            .use(FirebaseIntegration.FACTORY)
            .build()

        // Set the initialized instance as a globally accessible instance.
        Analytics.setSingletonInstance(analytics)

        registerActivityLifecycleCallbacks(
            AppboyLifecycleCallbackListener(
                true,
                true
            )
        )
        // createUserBraze(this, "KingMayank07", "", "")
        Appboy.getInstance(this).changeUser("KingMayank07")
    }
}