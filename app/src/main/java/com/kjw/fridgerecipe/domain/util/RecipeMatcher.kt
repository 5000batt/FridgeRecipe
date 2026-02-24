package com.kjw.fridgerecipe.domain.util

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kjw.fridgerecipe.data.util.IngredientAnalyzer
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeWithMatch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeMatcher
    @Inject
    constructor(
        private val analyzer: IngredientAnalyzer,
        private val remoteConfig: FirebaseRemoteConfig,
    ) {
        private val jsonParser = Json { ignoreUnknownKeys = true }

        /**
         * Remote Config에서 최신 동의어 맵을 가져옵니다.
         */
        private fun getRemoteSynonyms(): Map<String, String> {
            return try {
                val jsonString = remoteConfig.getString("ingredient_synonyms")
                if (jsonString.isBlank()) return emptyMap()

                val jsonObject = jsonParser.parseToJsonElement(jsonString).jsonObject
                jsonObject.mapValues { it.value.jsonPrimitive.content }
            } catch (e: Exception) {
                emptyMap()
            }
        }

        /**
         * 단어를 동의어 사전에 정의된 표준어로 변환합니다.
         */
        private fun toStandard(
            word: String,
            synonyms: Map<String, String>,
        ): String = synonyms[word] ?: word

        suspend fun calculateMatch(
            recipe: Recipe,
            fridgeIngredients: List<Ingredient>,
        ): RecipeWithMatch =
            coroutineScope {
                // 최신 동의어 사전 로드
                val synonyms = getRemoteSynonyms()

                // 1. 냉장고 재료 병렬 분석 (성능 최적화)
                val fridgeDataDeferred =
                    fridgeIngredients.map { ingredient ->
                        async { ingredient.name to analyzer.getIngredientNouns(ingredient.name) }
                    }
                val fridgeData = fridgeDataDeferred.awaitAll()

                // 2. 체크할 레시피 재료 선정
                val recipeIngredientsToCheck = recipe.ingredients.filter { it.isEssential }.ifEmpty { recipe.ingredients }

                // 3. 부족한 재료 찾기 (filterNot + any 조합으로 직관적 변경)
                val missingIngredients =
                    recipeIngredientsToCheck.filterNot { recipeIng ->
                        val recipeNouns = analyzer.getIngredientNouns(recipeIng.name)

                        fridgeData.any { (fridgeName, fridgeNouns) ->
                            isActualMatch(recipeIng.name, recipeNouns, fridgeName, fridgeNouns, synonyms)
                        }
                    }

                val totalCount = recipeIngredientsToCheck.size
                val matchCount = totalCount - missingIngredients.size
                val percentage = if (totalCount > 0) (matchCount * 100 / totalCount) else 0

                RecipeWithMatch(
                    recipe = recipe,
                    matchCount = matchCount,
                    totalCount = totalCount,
                    matchPercentage = percentage,
                    isCookable = missingIngredients.isEmpty(),
                    missingIngredients = missingIngredients.map { it.name },
                )
            }

        private fun isActualMatch(
            recipeIngredientName: String,
            recipeIngredientNouns: List<String>,
            fridgeIngredientName: String,
            fridgeIngredientNouns: List<String>,
            synonyms: Map<String, String>,
        ): Boolean {
            // 1. 전체 이름 비교 (표준화 후 비교)
            val standardRecipe = toStandard(recipeIngredientName.replace("\\s".toRegex(), ""), synonyms)
            val standardFridge = toStandard(fridgeIngredientName.replace("\\s".toRegex(), ""), synonyms)
            if (standardRecipe == standardFridge) return true

            if (recipeIngredientNouns.isEmpty() || fridgeIngredientNouns.isEmpty()) return false

            // 2. 핵심 명사(마지막 단어) 비교 (표준화 후 비교)
            val recipeHead = toStandard(recipeIngredientNouns.last(), synonyms)
            val fridgeHead = toStandard(fridgeIngredientNouns.last(), synonyms)
            if (recipeHead == fridgeHead) return true

            // 3. 포함 관계 확인 (냉장고 재료 명사 중 하나라도 표준화 시 레시피 핵심어와 같은지)
            return fridgeIngredientNouns.any { toStandard(it, synonyms) == recipeHead }
        }
    }
