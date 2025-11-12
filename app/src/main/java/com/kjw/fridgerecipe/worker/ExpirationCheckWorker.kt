package com.kjw.fridgerecipe.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kjw.fridgerecipe.MainActivity
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class ExpirationCheckWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ingredientRepository: IngredientRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val allIngredients = ingredientRepository.getAllIngredientsSuspend()
            val today = LocalDate.now()

            val expiringSoon = allIngredients.filter {
                !it.expirationDate.isBefore(today) &&
                it.expirationDate.isBefore(today.plusDays(4))
            }

            if (expiringSoon.isNotEmpty()) {
                val ingredientNames = expiringSoon.joinToString(", ") { it.name }

                makeNotification(
                    title = "소비기한 임박 알림!",
                    message = "냉장고의 $ingredientNames 소비기한이 3일 이내입니다!"
                )
            }

            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("ExpirationCheckWorker", "Worker failed", e)

            return Result.failure()
        }
    }

    private fun makeNotification(title: String, message: String) {
        val channelId = "EXPIRATION_NOTIFICATION_CHANNEL"
        val notificationId = 123

        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "소비기한 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "소비기한이 임박한 재료를 알려줍니다."
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.default_image)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}