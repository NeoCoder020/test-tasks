package com.example.testtask.product

import com.example.testtask.product.dto.CreateProductRequest
import com.example.testtask.product.dto.CreateVariantRequest
import com.example.testtask.product.model.Product
import com.example.testtask.product.model.Variant
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class ProductRepository(
    private val jdbcClient: JdbcClient,
) {

    fun findAllProducts(): List<Product?> {
        return jdbcClient.sql(
            "SELECT id, title, type, description, image_url FROM products"
        ).query(Product::class.java).list()
    }

    fun findProductsByTitle(titleQuery: String?): List<Product?> {
        val normalizedQuery = titleQuery?.trim().orEmpty()

        return if (normalizedQuery.isEmpty()) {
            findAllProducts()
        } else {
            jdbcClient.sql(
                "SELECT id, title, type, description, image_url " +
                    "FROM products " +
                    "WHERE title ILIKE :titleQuery " +
                    "ORDER BY title ASC"
            )
                .param("titleQuery", "%$normalizedQuery%")
                .query(Product::class.java)
                .list()
        }
    }

    fun findAllVariants(productId: Long): List<Variant?> {
        return jdbcClient.sql(
            "SELECT id, product_id, title, position, price FROM variants WHERE product_id = :productId"
        )
            .param("productId", productId)
            .query(Variant::class.java)
            .list()
    }

    fun findProductById(productId: Long): Product? {
        return jdbcClient.sql(
            "SELECT id, title, type, description, image_url FROM products WHERE id = :product_id"
        )
            .param("product_id", productId)
            .query(Product::class.java)
            .optional().orElse(null)
    }

    fun findVariantById(variantId: Long): Variant? {
        return jdbcClient.sql(
            "SELECT id, product_id, title, position, price FROM variants WHERE id = :variant_id"
        )
            .param("variant_id", variantId)
            .query(Variant::class.java).optional().orElse(null)
    }

    fun importProduct(productObject: Product) {
        jdbcClient.sql(
            "INSERT INTO products(id, title, type, description, image_url) " +
                    "VALUES (:id, :title, :type, :description, :image_url) " +
                    "ON CONFLICT(id) DO NOTHING;"
        ).params(
            mapOf<String, Any?>(
                "id" to productObject.id,
                "title" to productObject.title,
                "type" to productObject.type,
                "description" to productObject.description,
                "image_url" to productObject.imageUrl
            )
        )
            .update()
    }

    fun importVariant(variantObject: Variant) {
        jdbcClient.sql(
            "INSERT INTO variants(id, product_id, title, position, price) " +
                    "VALUES (:id, :product_id, :title, :position, :price) " +
                    "ON CONFLICT(id) DO NOTHING;"
        )
            .params(
                mapOf<String, Any?>(
                    "id" to variantObject.id,
                    "product_id" to variantObject.productId,
                    "title" to variantObject.title,
                    "position" to variantObject.position,
                    "price" to variantObject.price,
                )
            )
            .update()
    }


    fun createProduct(productForm: CreateProductRequest): Long {
        return jdbcClient.sql(
            "INSERT INTO products (title, type, description, image_url) " +
                    "VALUES (:title, :type, :description, :imageUrl) " +
                    "RETURNING id"
        )
            .params(
                mapOf(
                    "title" to productForm.title,
                    "type" to productForm.type,
                    "description" to productForm.description,
                    "imageUrl" to productForm.imageUrl
                )
            )
            .query(Long::class.java).single()
    }

    fun updateProduct(productId: Long, productForm: CreateProductRequest): Int {
        return jdbcClient.sql(
            "UPDATE products " +
                "SET title = :title, type = :type, description = :description, image_url = :imageUrl " +
                "WHERE id = :productId"
        )
            .params(
                mapOf(
                    "productId" to productId,
                    "title" to productForm.title,
                    "type" to productForm.type,
                    "description" to productForm.description,
                    "imageUrl" to productForm.imageUrl,
                )
            )
            .update()
    }

    fun deleteProduct(productId: Long): Int {
        return jdbcClient.sql(
            "DELETE FROM products WHERE id = :productId"
        )
            .param("productId", productId)
            .update()
    }

    fun createVariant(variantForm: CreateVariantRequest): Long {
        return jdbcClient.sql(
            "INSERT INTO variants (product_id, title, position, price) " +
                    "VALUES (:productId, :title, :position, :price) " +
                    "RETURNING id"
        )
            .params(
                mapOf(
                    "productId" to variantForm.productId,
                    "title" to variantForm.title,
                    "position" to variantForm.position,
                    "price" to variantForm.price,
                )
            ).query(Long::class.java).single()
    }

    fun syncSetValueProduct() {
        jdbcClient.sql(
            "SELECT setval(pg_get_serial_sequence('products', 'id')," +
                    "COALESCE(MAX(id), 0)) FROM products"
        )
            .query(Long::class.java).single()
    }

    fun syncSetValueVariant() {
        jdbcClient.sql(
            "SELECT setval(pg_get_serial_sequence('variants', 'id')," +
                    "COALESCE(MAX(id), 0)) FROM variants"
        )
            .query(Long::class.java).single()
    }


}
