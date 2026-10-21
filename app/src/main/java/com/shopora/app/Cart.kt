package com.shopora.app

data class Product(val id: String, val name: String, val priceCents: Int)

val catalog = listOf(
    Product("tote", "Canvas Tote", 2000),
    Product("mug", "Ceramic Mug", 1200),
    Product("notebook", "Dot Notebook", 800),
    Product("bottle", "Steel Bottle", 2500),
)

data class CartLine(val product: Product, val quantity: Int)

/** The shopper's cart. Lines keep the order items were first added. */
class Cart(val lines: List<CartLine> = emptyList()) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
    val subtotalCents: Int get() = lines.sumOf { it.product.priceCents * it.quantity }

    fun add(product: Product): Cart {
        val existing = lines.find { it.product.id == product.id }
        return if (existing == null) {
            Cart(lines + CartLine(product, 1))
        } else {
            Cart(lines.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it })
        }
    }

    fun remove(productId: String): Cart = Cart(lines.filterNot { it.product.id == productId })
}

fun money(cents: Int): String {
    val sign = if (cents < 0) "-" else ""
    val abs = kotlin.math.abs(cents)
    return "$sign$${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
}
