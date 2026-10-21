package com.shopora.app

/** A discount code the shop accepts. */
sealed class Discount(val code: String, val label: String) {
    abstract fun amountCents(subtotalCents: Int): Int

    class Percent(code: String, private val percent: Int) : Discount(code, "$percent% off") {
        override fun amountCents(subtotalCents: Int) = subtotalCents * percent / 100
    }

    class Fixed(code: String, private val cents: Int) : Discount(code, "${money(cents)} off") {
        override fun amountCents(subtotalCents: Int) = minOf(cents, subtotalCents)
    }
}

private val codes = listOf(Discount.Percent("SAVE10", 10), Discount.Fixed("WELCOME5", 500))

/** The discount for a code typed by the shopper, or null when the shop doesn't know it. */
fun findDiscount(input: String): Discount? {
    val code = input.trim().uppercase()
    // Codes printed on receipts start with a '#'.
    val normalized = if (code.first() == '#') code.drop(1) else code
    return codes.find { it.code == normalized }
}

/** What the shopper pays after the discount. */
fun totalCents(subtotalCents: Int, discount: Discount?): Int {
    val off = discount?.amountCents(subtotalCents) ?: 0
    return subtotalCents - off - (if (discount is Discount.Percent) off else 0)
}
