package com.kjw.fridgerecipe.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.repository.ImageRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.util.UiText
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {
    override suspend fun saveImageToInternalStorage(uriString: String): DataResult<String> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val contentResolver = context.contentResolver

            // 이미지 크기 계산
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false

            // 비트맵 로드 및 압축 저장
            val scaledBitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            } ?: return@withContext DataResult.Error(UiText.StringResource(R.string.error_msg_generic))

            val directory = File(context.filesDir, "recipe_images")
            if (!directory.exists()) directory.mkdirs()

            val fileName = "IMG_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            FileOutputStream(file).use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            }

            scaledBitmap.recycle()
            DataResult.Success(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            DataResult.Error(UiText.StringResource(R.string.error_msg_generic), cause = e)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
