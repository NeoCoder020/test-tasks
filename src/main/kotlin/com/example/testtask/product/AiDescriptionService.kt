package com.example.testtask.product

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class AiDescriptionService(
    private val restClient: RestClient,
    @Value("\${openai.api-key:}") private val openAiApiKey: String,
    @Value("\${openai.model:gpt-4.1-mini}") private val openAiModel: String,
    @Value("\${openai.base-url:https://api.openai.com}") private val openAiBaseUrl: String,
) {

    fun generateDescription(rawKeywords: List<String>): String {
        val keywords = normalizeKeywords(rawKeywords)

        // Version 1: placeholder description generator
        // Previous implementation kept for reviewer/demo comparison
        /*
        val focusList = when (keywords.size) {
            1 -> keywords.first()
            2 -> "${keywords[0]} and ${keywords[1]}"
            else -> keywords.dropLast(1).joinToString(", ") + ", and " + keywords.last()
        }

        val lead = keywords.first().replaceFirstChar { char ->
            if (char.isLowerCase()) {
                char.titlecase()
            } else {
                char.toString()
            }
        }

        return "$lead is designed for customers who want $focusList in one product. " +
            "It combines a clean look with dependable everyday performance, making it easy to use from the first try. " +
            "The result is a polished, confidence-building product description that feels ready for a storefront."
        */

        // Version 2: OpenAI-backed description generator with fallback to Version 1.
        return generateWithOpenAi(keywords) ?: generatePlaceholderDescription(keywords)
    }

    private fun normalizeKeywords(rawKeywords: List<String>): List<String> {
        val keywords = rawKeywords
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        require(keywords.isNotEmpty()) { "At least one keyword is required." }
        require(keywords.size <= 5) { "Use up to five keywords or short phrases." }

        return keywords
    }

    private fun generateWithOpenAi(keywords: List<String>): String? {
        if (openAiApiKey.isBlank()) {
            logger.warn("OPENAI_API_KEY is not configured. Falling back to the local placeholder description generator.")
            return null
        }

        return try {
            val response = restClient.post()
                .uri("${openAiBaseUrl.trimEnd('/')}/v1/chat/completions")
                .header("Authorization", "Bearer $openAiApiKey")
                .header("Content-Type", "application/json")
                .body(
                    ChatCompletionRequest(
                        model = openAiModel,
                        messages = listOf(
                            ChatMessage(
                                role = "developer",
                                content = "You write concise ecommerce product descriptions. Return one polished paragraph between 45 and 80 words. Do not use bullet points, headings, markdown, or quotation marks."
                            ),
                            ChatMessage(
                                role = "user",
                                content = "Write a product description using these keywords or short phrases: ${keywords.joinToString(", ")}."
                            ),
                        ),
                    )
                )
                .retrieve()
                .body(ChatCompletionResponse::class.java)

            response?.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        } catch (exception: Exception) {
            logger.warn("OpenAI description generation failed. Falling back to the local placeholder generator.", exception)
            null
        }
    }

    private fun generatePlaceholderDescription(keywords: List<String>): String {
        val focusList = when (keywords.size) {
            1 -> keywords.first()
            2 -> "${keywords[0]} and ${keywords[1]}"
            else -> keywords.dropLast(1).joinToString(", ") + ", and " + keywords.last()
        }

        val lead = keywords.first().replaceFirstChar { char ->
            if (char.isLowerCase()) {
                char.titlecase()
            } else {
                char.toString()
            }
        }

        return "$lead is designed for customers who want $focusList in one product. " +
            "It combines a clean look with dependable everyday performance, making it easy to use from the first try. " +
            "The result is a polished, confidence-building product description that feels ready for a storefront."
    }

    private data class ChatCompletionRequest(
        val model: String,
        val messages: List<ChatMessage>,
    )

    private data class ChatMessage(
        val role: String,
        val content: String,
    )

    private data class ChatCompletionResponse(
        val choices: List<ChatChoice> = emptyList(),
    )

    private data class ChatChoice(
        val message: ChatMessageResponse? = null,
    )

    private data class ChatMessageResponse(
        val content: String? = null,
    )

    companion object {
        private val logger = LoggerFactory.getLogger(AiDescriptionService::class.java)
    }
}
