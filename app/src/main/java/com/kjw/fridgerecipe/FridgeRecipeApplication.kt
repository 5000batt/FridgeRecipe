package com.kjw.fridgerecipe

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kjw.fridgerecipe.worker.ExpirationCheckWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FridgeRecipeApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleExpirationCheck()
    }

    private fun scheduleExpirationCheck() {
        val expirationCheckRequest = PeriodicWorkRequestBuilder<ExpirationCheckWorker>(
            1, TimeUnit.DAYS
        ).build()


        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ExpirationCheckWork",
            ExistingPeriodicWorkPolicy.KEEP,
            expirationCheckRequest
        )
    }
}