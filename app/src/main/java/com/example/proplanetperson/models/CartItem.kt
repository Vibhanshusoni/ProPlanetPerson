package com.example.proplanetperson.models

// data class for an item in the user's shopping cart
data class CartItem(
    val productId: String = "",       // Unique ID of the product (e.g., from your product database)
    val productName: String = "",     // Name of the product
    val imageUrl: String = "",        // URL to the product's image
    val price: Double = 0.0,          // Price per unit of the product
    var quantity: Int = 0,            // Number of this product in the cart (mutable as quantity can change)
    val userId: String = ""           // ID of the user who added this item to their cart
)