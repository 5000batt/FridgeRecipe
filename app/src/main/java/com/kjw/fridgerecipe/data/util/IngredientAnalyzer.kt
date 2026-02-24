package com.kjw.fridgerecipe.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.shineware.nlp.komoran.core.Komoran
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class IngredientAnalyzer
    @Inject
    constructor(
        // Provider를 사용하면 주입 시점이 아니라 get()을 호출하는 시점에 객체가 생성됩니다.
        private val komoranProvider: Provider<Komoran>,
    ) {
        /**
         * 재료 이름에서 수식어를 제외한 명사 목록을 반환합니다.
         */
        suspend fun getIngredientNouns(text: String): List<String> =
            withContext(Dispatchers.Default) {
                if (text.isBlank()) return@withContext emptyList()

                try {
                    // get() 호출 시점에 코모란 사전 로딩이 시작됩니다. (Dispatchers.Default 내에서 실행됨)
                    komoranProvider
                        .get()
                        .analyze(text)
                        .tokenList
                        .filter { it.pos == "NNG" || it.pos == "NNP" }
                        .map { it.morph }
                        .filter { it.isNotBlank() }
                } catch (e: Exception) {
                    listOf(text.replace("\\s".toRegex(), ""))
                }
            }
    }
