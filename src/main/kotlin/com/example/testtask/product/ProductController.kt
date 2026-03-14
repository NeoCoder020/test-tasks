package com.example.testtask.product

import com.example.testtask.product.dto.CreateProductRequest
import com.example.testtask.product.dto.CreateVariantRequest
import com.example.testtask.product.dto.GenerateDescriptionRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ProductController(
    private val productService: ProductService,
    private val aiDescriptionService: AiDescriptionService,
) {

    @GetMapping
    fun home(): String {
        return "layout"
    }

    @GetMapping("/products")
    fun showProducts(model: Model, @RequestHeader(value = "HX-Request", required = false) hxRequest: String?): String {
        try {
            val products = productService.loadProductsFromDb()
            model.addAttribute("products", products)
            if (hxRequest == "true") {
                return "fragments/products-table :: products-table-fragment"
            }
            else{
                return "layout"
            }
        }
        catch (e: Exception) {
            return "redirect:/"
        }
    }

    @GetMapping("/products/search")
    fun searchProducts(
        model: Model,
        @RequestParam(name = "q", required = false) query: String?,
        @RequestHeader(value = "HX-Request", required = false) hxRequest: String?,
        @RequestHeader(value = "HX-Target", required = false) hxTarget: String?,
    ): String {
        return try {
            val products = productService.searchProductsByTitle(query)
            model.addAttribute("products", products)
            model.addAttribute("searchQuery", query.orEmpty())

            if (hxRequest == "true" && hxTarget == "searchResults") {
                "fragments/products-table :: products-table-fragment"
            } else if (hxRequest == "true") {
                "fragments/product-search :: product-search-fragment"
            } else {
                "layout"
            }
        } catch (e: Exception) {
            "redirect:/products"
        }
    }

    @GetMapping("/product/{productId}")
    fun showProductDetail(@PathVariable productId: Long,
                          model: Model,
                          @RequestHeader(value = "HX-Request", required = false) hxRequest: String?
    ): String {
        try {
            val product = productService.loadProductById(productId)
            val variants = productService.loadVariantsOfProduct(productId)
            model.addAttribute("product", product)
            model.addAttribute("variants", variants)
            if (hxRequest == "true") {
            return "fragments/product-detail :: product-detail-fragment"
            }
            else {
                return "layout"
            }
        }
        catch (e: Exception) {
            return "redirect:/products"
        }
    }

    @GetMapping("/product/add")
    fun addProduct(model: Model,
                   @RequestParam(required = false) productId: Long?,
                   @RequestHeader(value = "HX-Request", required = false) hxRequest: String?
    ): String {
        val products = productService.loadProductsFromDb()
        model.addAttribute("products", products)

        model.addAttribute("selectedProductId", productId)
        if (hxRequest == "true") {
            return "fragments/product-add :: product-add-fragment"
        }
        else{
            return "layout"
        }
    }

    @PostMapping("/product/add")
    fun addProduct(form: CreateVariantRequest,
                   response: HttpServletResponse
    ) {
        productService.addProduct(form)
        response.setHeader("HX-Redirect", "/product/${form.productId}")
    }

    @GetMapping("/product/create")
    fun createProduct(
        @RequestHeader(value = "HX-Request", required = false) hxRequest: String?
    ): String {
        if (hxRequest == "true") {
            return "fragments/product-create :: product-create-fragment"
        }
        else{
            return "layout"
        }
    }

    @GetMapping("/product/{productId}/edit")
    fun editProduct(
        @PathVariable productId: Long,
        model: Model,
        @RequestHeader(value = "HX-Request", required = false) hxRequest: String?
    ): String {
        return try {
            val product = productService.loadProductById(productId) ?: return "redirect:/products"
            model.addAttribute("product", product)

            if (hxRequest == "true") {
                "fragments/product-edit :: product-edit-fragment"
            } else {
                "layout"
            }
        } catch (e: Exception) {
            "redirect:/products"
        }
    }

    @PostMapping("/product/create")
    fun createProduct(form: CreateProductRequest,
                      @RequestHeader(value = "HX-Request", required = false) hxRequest: String?
                      , model: Model
    ): String {
        productService.createProduct(form)
        val products = productService.loadProductsFromDb()
        model.addAttribute("products", products)
        if (hxRequest == "true") {
            return "fragments/products-table :: products-table-fragment"
        }
        else{
            return "redirect:/products"
        }
    }

    @PostMapping("/product/{productId}/edit")
    fun editProduct(
        @PathVariable productId: Long,
        form: CreateProductRequest,
        @RequestHeader(value = "HX-Request", required = false) hxRequest: String?,
        response: HttpServletResponse,
    ): String {
        productService.updateProduct(productId, form)

        return if (hxRequest == "true") {
            response.setHeader("HX-Redirect", "/products")
            ""
        } else {
            "redirect:/products"
        }
    }

    @PostMapping("/product/{productId}/delete")
    fun deleteProduct(
        @PathVariable productId: Long,
        @RequestParam(name = "q", required = false) query: String?,
        model: Model,
    ): String {
        productService.deleteProduct(productId)
        model.addAttribute("products", productService.searchProductsByTitle(query))
        return "fragments/products-table :: products-table-fragment"
    }

    @PostMapping("/product/description/generate")
    fun generateDescription(
        form: GenerateDescriptionRequest,
        model: Model,
    ): String {
        val keywords = form.keywords()
        model.addAttribute("keywords", keywords)

        return if (keywords.isEmpty()) {
            model.addAttribute("generationError", "Enter between 1 and 5 keywords or short phrases.")
            "fragments/description-generator :: description-generator-result"
        } else {
            model.addAttribute("generatedDescription", aiDescriptionService.generateDescription(keywords))
            "fragments/description-generator :: description-generator-result"
        }
    }

}
