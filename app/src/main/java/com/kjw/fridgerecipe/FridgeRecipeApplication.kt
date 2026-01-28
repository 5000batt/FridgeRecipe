package com.kjw.fridgerecipe

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
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

        // App Check 초기화
        if (BuildConfig.DEBUG) {
            try {
                val debugFactoryClass = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                val factory = debugFactoryClass.getMethod("getInstance").invoke(null) as com.google.firebase.appcheck.AppCheckProviderFactory

                Firebase.appCheck.installAppCheckProviderFactory(factory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // 실제 배포 시 Play Integrity 사용
            Firebase.appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        // AdMob SDK 초기화
        MobileAds.initialize(this) {

        }

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