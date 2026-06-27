package com.kjw.fridgerecipe.data.util

import com.kjw.fridgerecipe.data.remote.GeminiException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonExtractorTest {

    // ── 코드블록 경로 ───────────────────────────────────────────────────────

    @Test
    fun `json 코드블록 응답에서 JSON 추출`() {
        val input = "```json\n{\"title\": \"김치찌개\"}\n```"
        assertEquals("{\"title\": \"김치찌개\"}", extractJsonFromAiResponse(input))
    }

    @Test
    fun `언어 표시 없는 코드블록 응답에서 JSON 추출`() {
        val input = "```\n{\"title\": \"된장찌개\"}\n```"
        assertEquals("{\"title\": \"된장찌개\"}", extractJsonFromAiResponse(input))
    }

    @Test
    fun `코드블록 앞뒤에 설명 텍스트가 있어도 JSON 추출`() {
        val input = "레시피를 찾았습니다.\n```json\n{\"title\": \"불고기\"}\n```\n이상입니다."
        assertEquals("{\"title\": \"불고기\"}", extractJsonFromAiResponse(input))
    }

    @Test
    fun `중첩 JSON 구조를 코드블록에서 올바르게 추출`() {
        val input = "```json\n{\"title\": \"찌개\", \"steps\": [{\"order\": 1, \"desc\": \"끓인다\"}]}\n```"
        val expected = "{\"title\": \"찌개\", \"steps\": [{\"order\": 1, \"desc\": \"끓인다\"}]}"
        assertEquals(expected, extractJsonFromAiResponse(input))
    }

    // ── 코드블록 없는 경로 ─────────────────────────────────────────────────

    @Test
    fun `코드블록 없는 raw JSON 응답에서 JSON 추출`() {
        val input = "{\"title\": \"비빔밥\"}"
        assertEquals("{\"title\": \"비빔밥\"}", extractJsonFromAiResponse(input))
    }

    @Test
    fun `raw JSON 앞뒤에 텍스트가 있어도 JSON 추출`() {
        val input = "여기 레시피입니다: {\"title\": \"순두부\"} 감사합니다."
        assertEquals("{\"title\": \"순두부\"}", extractJsonFromAiResponse(input))
    }

    // ── 오류 경로 ──────────────────────────────────────────────────────────

    @Test
    fun `JSON 없으면 ParsingError 발생`() {
        assertFailsWith<GeminiException.ParsingError> {
            extractJsonFromAiResponse("JSON이 없는 응답입니다.")
        }
    }

    @Test
    fun `빈 문자열이면 ParsingError 발생`() {
        assertFailsWith<GeminiException.ParsingError> {
            extractJsonFromAiResponse("")
        }
    }

    @Test
    fun `중괄호 순서가 잘못되면 ParsingError 발생`() {
        assertFailsWith<GeminiException.ParsingError> {
            extractJsonFromAiResponse("} 잘못된 순서 {")
        }
    }
}
