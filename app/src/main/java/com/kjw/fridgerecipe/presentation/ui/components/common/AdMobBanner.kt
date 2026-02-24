package com.kjw.fridgerecipe.presentation.ui.components.common

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.kjw.fridgerecipe.BuildConfig

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // 적응형 배너 크기 계산
    val adSize =
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context,
            screenWidthDp,
        )

    // 광고 단위 ID 설정
    val adUnitId =
        if (BuildConfig.DEBUG) {
            BuildConfig.ADMOB_BANNER_ID
        } else {
            Firebase.remoteConfig.getString("admob_banner_id").ifBlank {
                BuildConfig.ADMOB_BANNER_ID
            }
        }

    Log.d("AdMobBanner", "사용 중인 admob_banner_id: $adUnitId")

    AndroidView(
        modifier =
            modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(adSize)
                this.adUnitId = adUnitId

                // 광고 리스너 설정 (로딩 완료 콜백 연결)
                adListener =
                    object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            Log.d("AdMobBanner", "광고 로드 성공")
                            onAdLoaded()
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            Log.e("AdMobBanner", "광고 로드 실패: ${error.message}")
                        }
                    }

                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { adView ->
            // 메모리 누수 방지
            adView.destroy()
        },
    )
}
