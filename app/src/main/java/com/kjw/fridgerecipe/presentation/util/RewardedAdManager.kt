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
import com.kjw.fridgerecipe.BuildConfig

class RewardedAdManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private val AD_UNIT_ID = BuildConfig.ADMOB_REWARD_ID

    fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("RewardedAdManager", "광고 로드 실패: ${adError.message}")
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("RewardedAdManager", "광고 로드 성공")
                rewardedAd = ad
            }
        })
    }

    fun showAd(activity: Activity, onRewardEarned: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
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

            rewardedAd?.show(activity, OnUserEarnedRewardListener { rewardItem ->
                Log.d("RewardedAdManager", "보상 지급: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            })
        } else {
            Log.d("RewardedAdManager", "준비된 광고가 없음")
            loadAd()
        }
    }
}