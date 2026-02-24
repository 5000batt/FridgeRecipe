package com.kjw.fridgerecipe.presentation.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kjw.fridgerecipe.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val remoteConfig: FirebaseRemoteConfig,
    ) {
        private var rewardedAd: RewardedAd? = null

        private fun getAdUnitId(): String {
            if (BuildConfig.DEBUG) {
                return BuildConfig.ADMOB_REWARD_ID
            } else {
                val id = remoteConfig.getString("admob_reward_id")
                return id.ifBlank { BuildConfig.ADMOB_REWARD_ID }
            }
        }

        fun loadAd() {
            Log.d("RewardedAdManager", "사용 중인 admob_reward_id: ${getAdUnitId()}")
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                getAdUnitId(),
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("RewardedAdManager", "광고 로드 실패: ${adError.message}")
                        rewardedAd = null
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d("RewardedAdManager", "광고 로드 성공")
                        rewardedAd = ad
                    }
                },
            )
        }

        fun showAd(
            activity: Activity,
            onRewardEarned: () -> Unit,
        ) {
            if (rewardedAd != null) {
                rewardedAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("RewardedAdManager", "광고 닫힘 -> 새 광고 로드")
                            rewardedAd = null
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("RewardedAdManager", "광고 표시 실패")
                            rewardedAd = null
                        }
                    }

                rewardedAd?.show(
                    activity,
                    OnUserEarnedRewardListener { rewardItem ->
                        Log.d("RewardedAdManager", "보상 지급: ${rewardItem.amount} ${rewardItem.type}")
                        onRewardEarned()
                    },
                )
            } else {
                Log.d("RewardedAdManager", "준비된 광고가 없음")
                loadAd()
            }
        }
    }
