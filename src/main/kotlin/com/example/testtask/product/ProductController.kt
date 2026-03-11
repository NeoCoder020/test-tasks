package com.example.testtask.product

import com.example.testtask.product.dto.CreateProductRequest
import com.example.testtask.product.dto.CreateVariantRequest
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

}