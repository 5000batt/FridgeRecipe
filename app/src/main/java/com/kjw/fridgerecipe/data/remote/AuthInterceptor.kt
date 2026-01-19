package com.kjw.fridgerecipe.data.remote

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.kjw.fridgerecipe.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("key", BuildConfig.API_KEY)
            .build()

        val packageName = context.packageName
        val signature = getAppSignature(context, packageName)

        val newRequestBuilder = originalRequest.newBuilder()
            .url(newUrl)
            .addHeader("X-Android-Package", packageName)

        if (signature != null) {
            newRequestBuilder.addHeader("X-Android-Cert", signature)
        }

        return chain.proceed(newRequestBuilder.build())
    }

    private fun getAppSignature(context: Context, packageName: String): String? {
        return try {
            val packageManager = context.packageManager
            // Android 9(P) 이상 vs 미만 분기 처리
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                packageInfo.signatures
            }

            val signature = signatures?.firstOrNull() ?: return null

            // SHA-1 해시 계산
            val md = MessageDigest.getInstance("SHA-1")
            val digest = md.digest(signature.toByteArray())

            // byte array -> hex string 변환 (예: A1:B2:C3...)
            digest.joinToString("") { "%02X".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}