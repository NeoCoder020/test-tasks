package com.example.testtask.product

import com.example.testtask.product.dto.CreateProductRequest
import com.example.testtask.product.dto.CreateVariantRequest
import com.example.testtask.product.model.Product
import com.example.testtask.product.model.Variant
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.ObjectMapper

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(initialDelay = 0)
    fun loadProductsFromJson() {
        try {
            val fileContent = webClient.get()
                .uri("https://famme.no/products.json").retrieve()
                .bodyToMono(String::class.java).block()
                ?: throw IllegalStateException("Product list is empty")

            val jsonTree = objectMapper.readTree(fileContent)
            jsonTree.path("products").take(50).forEach { product ->
                val rowProduct = Product(
                    id = product.path("id").asLong(),
                    title = product.path("title").asString(),
                    type = product.path("product_type").asString(),
                    description = Jsoup.parse(product.path("body_html").asString()).text(),
                    imageUrl = product.path("images").path(0).path("src").asString()
                )
                productRepository.importProduct(rowProduct)
                product.path("variants").forEach { variant ->
                    val rowVariant = Variant(
                        id = variant.path("id").asLong(),
                        productId = rowProduct.id,
                        title = variant.path("title").asString(),
                        position = variant.path("position").asInt().toShort(),
                        price = variant.path("price").asDecimal()
                    )
                    productRepository.importVariant(rowVariant)
                }
            }
            productRepository.syncSetValueProduct()
            productRepository.syncSetValueVariant()

        } catch (e: Exception) {
            logger.error("Failed to load products from JSON", e)
        }
    }

    fun loadProductsFromDb(): List<Product?> {
        return productRepository.findAllProducts()
    }

    fun loadProductById(productId: Long): Product? {
        return productRepository.findProductById(productId)
    }

    fun loadVariantsOfProduct(productId: Long): List<Variant?> {
        return productRepository.findAllVariants(productId)
    }

    fun addProduct(form: CreateVariantRequest): Long {
        return productRepository.createVariant(form)
    }

    fun createProduct(form: CreateProductRequest): Long {
        return productRepository.createProduct(form)
    }

}