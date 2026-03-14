package com.example.testtask.product.dto

data class GenerateDescriptionRequest(
    val keyword1: String? = null,
    val keyword2: String? = null,
    val keyword3: String? = null,
    val keyword4: String? = null,
    val keyword5: String? = null,
) {
    fun keywords(): List<String> = listOf(
        keyword1,
        keyword2,
        keyword3,
        keyword4,
        keyword5,
    ).mapNotNull { it?.trim() }
        .filter { it.isNotEmpty() }
}
