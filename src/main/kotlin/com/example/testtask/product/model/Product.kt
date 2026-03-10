package com.example.testtask.product.model

data class Product(
    val id: Long,
    val title: String,
    val type: String?,
    val description: String?,
    val imageUrl: String?
)
