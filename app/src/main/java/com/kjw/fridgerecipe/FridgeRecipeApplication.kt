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
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
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
        Firebase.appCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                // 에뮬레이터나 디버그 빌드에서 테스트하기 위한 설정
                DebugAppCheckProviderFactory.getInstance()
            } else {
                // 실제 배포 시 Play Integrity 사용
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

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