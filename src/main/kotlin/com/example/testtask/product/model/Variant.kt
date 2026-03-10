package com.example.testtask.product.model

import java.math.BigDecimal

data class Variant(
    val id: Long,
    val productId: Long,
    val title: String,
    val position: Short?,
    val price: BigDecimal
)
