package com.shopora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF1B1B1F)
private val Brand = Color(0xFF2F5D50)
private val Paper = Color(0xFFF6F3EE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Brand, background = Paper, surface = Color.White, onSurface = Ink)) {
                Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
                    ShoporaApp()
                }
            }
        }
    }
}

private enum class Screen { Shop, Cart, Done }

@Composable
fun ShoporaApp() {
    var screen by remember { mutableStateOf(Screen.Shop) }
    var cart by remember { mutableStateOf(Cart()) }
    var paidCents by remember { mutableStateOf(0) }

    BackHandler(enabled = screen != Screen.Shop) { screen = Screen.Shop }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Header(
            title = when (screen) {
                Screen.Shop -> "Shopora"
                Screen.Cart -> "Your cart"
                Screen.Done -> "Order placed"
            },
            cartCount = cart.itemCount,
            onCart = { screen = Screen.Cart },
            showCart = screen == Screen.Shop,
        )
        when (screen) {
            Screen.Shop -> ShopScreen(onAdd = { cart = cart.add(it) })
            Screen.Cart -> CartScreen(
                cart = cart,
                onRemove = { cart = cart.remove(it) },
                onKeepShopping = { screen = Screen.Shop },
                onCheckout = { totalCents ->
                    paidCents = totalCents
                    cart = Cart()
                    screen = Screen.Done
                },
            )
            Screen.Done -> DoneScreen(paidCents = paidCents, onBack = { screen = Screen.Shop })
        }
    }
}

@Composable
private fun Header(title: String, cartCount: Int, onCart: () -> Unit, showCart: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Brand).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (showCart) {
            OutlinedButton(onClick = onCart, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                Text("Cart ($cartCount)")
            }
        }
    }
}

@Composable
private fun ShopScreen(onAdd: (Product) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(catalog, key = { it.id }) { product ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Ink)
                        Text(money(product.priceCents), fontSize = 15.sp, color = Brand)
                    }
                    Button(onClick = { onAdd(product) }) { Text("Add ${product.name}") }
                }
            }
        }
    }
}

@Composable
private fun CartScreen(cart: Cart, onRemove: (String) -> Unit, onKeepShopping: () -> Unit, onCheckout: (Int) -> Unit) {
    var discount by remember { mutableStateOf<Discount?>(null) }
    var discountError by remember { mutableStateOf<String?>(null) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (cart.lines.isEmpty()) {
            Text("Your cart is empty.", fontSize = 17.sp, color = Ink)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onKeepShopping) { Text("Keep shopping") }
            return
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cart.lines, key = { it.product.id }) { line ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${line.product.name} × ${line.quantity}", fontSize = 16.sp, color = Ink)
                        Text(money(line.product.priceCents * line.quantity), color = Brand)
                    }
                    TextButton(onClick = { onRemove(line.product.id) }) { Text("Remove ${line.product.name}") }
                }
            }
        }
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))
        DiscountField(applied = discount, error = discountError, onApply = { input ->
            val found = findDiscount(input)
            discount = found
            discountError = if (found == null) "\"${input.trim()}\" isn't a valid code" else null
        })
        Spacer(Modifier.height(12.dp))
        val total = totalCents(cart.subtotalCents, discount)
        Text("Subtotal: ${money(cart.subtotalCents)}", fontSize = 16.sp, color = Ink)
        discount?.let { Text("Discount (${it.code}, ${it.label}): -${money(it.amountCents(cart.subtotalCents))}", fontSize = 16.sp, color = Brand) }
        Text("Total: ${money(total)}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { onCheckout(total) }, modifier = Modifier.fillMaxWidth()) { Text("Checkout") }
    }
}

@Composable
private fun DiscountField(applied: Discount?, error: String?, onApply: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Discount code") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = { onApply(input) }) { Text("Apply") }
    }
    when {
        error != null -> Text(error, color = Color(0xFFB3261E), fontSize = 14.sp)
        applied != null -> Text("${applied.code} applied", color = Brand, fontSize = 14.sp)
    }
}

@Composable
private fun DoneScreen(paidCents: Int, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Thanks! You paid ${money(paidCents)}.", fontSize = 20.sp, color = Ink)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Back to shop") }
    }
}
