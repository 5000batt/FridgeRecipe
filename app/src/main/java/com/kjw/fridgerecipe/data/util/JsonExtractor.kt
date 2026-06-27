package com.kjw.fridgerecipe.data.util

import com.kjw.fridgerecipe.data.remote.GeminiException

internal fun extractJsonFromAiResponse(text: String): String {
    val codeBlockRegex = Regex("```(?:json)?\\s*(\\{[\\s\\S]*\\})\\s*```")
    codeBlockRegex.find(text)?.let { return it.groupValues[1] }

    val startIndex = text.indexOf('{')
    val endIndex = text.lastIndexOf('}')
    if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
        throw GeminiException.ParsingError()
    }
    return text.substring(startIndex, endIndex + 1)
}
