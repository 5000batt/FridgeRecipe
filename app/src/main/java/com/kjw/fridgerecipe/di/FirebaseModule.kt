package com.kjw.fridgerecipe.di

import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        val defaultDefaults = mapOf(
            "gemini_model_endpoint" to "v1beta/models/gemini-2.5-flash-lite:generateContent",
            "admob_banner_id" to "ca-app-pub-2909354341085431/6849543362",
            "admob_reward_id" to "ca-app-pub-2909354341085431/6649221055"
        )
        remoteConfig.setDefaultsAsync(defaultDefaults)

        return remoteConfig
    }
}