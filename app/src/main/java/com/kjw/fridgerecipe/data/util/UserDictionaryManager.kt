package com.kjw.fridgerecipe.data.util

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDictionaryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fileName = "user_dic.txt"
    private val jsonParser = Json { ignoreUnknownKeys = true }

    /**
     * Remote Config에서 사용자 사전 데이터를 가져와 파일로 저장합니다.
     * JSON 형식 예: {"마라소스": "NNP", "굴소스": "NNG"}
     */
    fun updateDictionary(): String? {
        return try {
            val jsonString = Firebase.remoteConfig.getString("komoran_user_dic")
            if (jsonString.isBlank()) return null

            val jsonObject = jsonParser.parseToJsonElement(jsonString).jsonObject
            val dicFile = File(context.filesDir, fileName)

            dicFile.bufferedWriter().use { writer ->
                jsonObject.forEach { (word, pos) ->
                    // 코모란 사전 형식: 단어[Tab]품사
                    writer.write("${word}\t${pos.jsonPrimitive.content}")
                    writer.newLine()
                }
            }
            Log.d("UserDictionaryManager", "사용자 사전 업데이트 완료: ${dicFile.absolutePath}")
            dicFile.absolutePath
        } catch (e: Exception) {
            Log.e("UserDictionaryManager", "사용자 사전 업데이트 실패", e)
            null
        }
    }

    fun getDictionaryPath(): String? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file.absolutePath else null
    }
}
