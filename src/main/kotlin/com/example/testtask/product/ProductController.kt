package com.example.testtask.product

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun home(): String {
        return "layout"
    }

    @GetMapping("/products")
    fun showProducts(model: Model): String {
        val products = productService.loadProductsFromDb()
        model.addAttribute("products", products)
        return "fragments/products-table :: products-table-fragment"
    }
}