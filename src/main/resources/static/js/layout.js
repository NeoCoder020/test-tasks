function updateTopBarButtons() {
    const backBtn = document.getElementById("backToProductNavBtn");
    const loadBtn = document.querySelector('wa-tab[hx-get="/products"]');
    const createBtn = document.getElementById("createProductBtn");
    const searchBtn = document.getElementById("searchProductsBtn");
    const addBtn = document.getElementById("addProductBtn");

    const path = window.location.pathname;
    const params = new URLSearchParams(window.location.search);

    const isProductsPage = path === "/products";
    const isProductSearchPage = path === "/products/search";
    const isCreateProductPage = path === "/product/create";
    const isEditProductPage = /^\/product\/\d+\/edit$/.test(path);
    const isProductDetailPage = /^\/product\/\d+$/.test(path);
    const isAddVariantPage = path === "/product/add" && params.has("productId");

    loadBtn.style.display = "inline-flex";
    searchBtn.style.display = "inline-flex";
    createBtn.style.display = (isProductsPage || isCreateProductPage) ? "inline-flex" : "none";
    addBtn.style.display = (isProductDetailPage || isAddVariantPage) ? "inline-flex" : "none";
    if (backBtn) {
        backBtn.style.display = (isAddVariantPage || isEditProductPage) ? "inline-flex" : "none";
    }

    loadBtn.removeAttribute("active");
    searchBtn.removeAttribute("active");
    createBtn.removeAttribute("active");
    addBtn.removeAttribute("active");

    if (isProductsPage) {
        loadBtn.setAttribute("active", "");
    }

    if (isProductSearchPage) {
        searchBtn.setAttribute("active", "");
    }

    if (isCreateProductPage) {
        createBtn.setAttribute("active", "");
    }

    if (isAddVariantPage) {
        addBtn.setAttribute("active", "");
    }

    let productId = null;

    if (isProductDetailPage || isEditProductPage) {
        productId = path.split("/").pop();
        if (isEditProductPage) {
            productId = path.split("/")[2];
        }
    } else if (isAddVariantPage) {
        productId = params.get("productId");
    }



    if (productId) {
        addBtn.setAttribute("hx-get", `/product/add?productId=${productId}`);
        addBtn.setAttribute("hx-target", "#content");
        addBtn.setAttribute("hx-swap", "innerHTML");
        addBtn.setAttribute("hx-push-url", "true");
        if (backBtn) {
            backBtn.setAttribute("hx-get", `/product/${productId}`);
            backBtn.setAttribute("hx-target", "#content");
            backBtn.setAttribute("hx-swap", "innerHTML");
            backBtn.setAttribute("hx-push-url", "true");
        }
    }

    createBtn.setAttribute("hx-get", "/product/create");
    createBtn.setAttribute("hx-target", "#content");
    createBtn.setAttribute("hx-swap", "innerHTML");
    createBtn.setAttribute("hx-push-url", "true");

    searchBtn.setAttribute("hx-get", "/products/search");
    searchBtn.setAttribute("hx-target", "#content");
    searchBtn.setAttribute("hx-swap", "innerHTML");
    searchBtn.setAttribute("hx-push-url", "true");

    if (window.htmx) {
        htmx.process(document.body);
    }
}

document.addEventListener("DOMContentLoaded", updateTopBarButtons);
document.body.addEventListener("htmx:afterSwap", updateTopBarButtons);
window.addEventListener("popstate", updateTopBarButtons);

function getInitialUrl() {
    const path = window.location.pathname;
    const query = window.location.search;

    if (path === "/" || path === "") {
        return null;
    }

    return path + query;
}

function loadInitialContent() {
    const url = getInitialUrl();

    if (!url) {
        return;
    }

    htmx.ajax("GET", url, {
        target: "#content",
        swap: "innerHTML"
    });
}

document.addEventListener("DOMContentLoaded", loadInitialContent);
