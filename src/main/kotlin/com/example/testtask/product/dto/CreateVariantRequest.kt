package com.example.testtask.product.dto

import java.math.BigDecimal

data class CreateVariantRequest(
    val productId: Long,
    val title: String,
    val position: Short?,
    val price: BigDecimal
)
