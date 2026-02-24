package com.kjw.fridgerecipe.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.remoteconfig.remoteConfig
import javax.inject.Inject

class GeminiModelProvider
    @Inject
    constructor() {
        fun getModel(): GenerativeModel {
            val modelName =
                Firebase.remoteConfig.getString("gemini_model_name").ifBlank {
                    "gemini-2.5-flash-lite"
                }

            Log.d("RecipeRepo", "AI 모델 명 : $modelName")

            return Firebase
                .ai(backend = GenerativeBackend.googleAI())
                .generativeModel(modelName)
        }
    }
