package com.kjw.fridgerecipe.presentation.ui.components.common

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.kjw.fridgerecipe.BuildConfig

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
        context,
        screenWidthDp,
    )

    val adUnitId = if (BuildConfig.DEBUG) BuildConfig.ADMOB_BANNER_ID
        else Firebase.remoteConfig.getString("admob_banner_id").ifBlank {
        BuildConfig.ADMOB_BANNER_ID
    }

    Log.d("AdMobBanner", "사용 중인 admob_banner_id: $adUnitId")

    key(screenWidthDp) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(adSize)
                    this.adUnitId = adUnitId
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}