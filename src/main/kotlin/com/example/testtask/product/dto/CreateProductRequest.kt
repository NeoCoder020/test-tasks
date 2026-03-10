package com.example.testtask.product.dto

data class CreateProductRequest(
    val title: String,
    val type: String?,
    val description: String?,
    val imageUrl: String?
)
