package com.kjw.fridgerecipe.presentation.ui.components.common

import android.annotation.SuppressLint
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

    key(screenWidthDp) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(adSize)
                    adUnitId = BuildConfig.ADMOB_BANNER_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}