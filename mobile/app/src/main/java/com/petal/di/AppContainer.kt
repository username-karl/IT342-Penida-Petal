package com.petal.di

import android.content.Context
import com.petal.core.network.ApiClient
import com.petal.core.session.EncryptedSessionStore
import com.petal.core.session.SessionStore
import com.petal.data.auth.AuthRepository
import com.petal.data.cart.CartRepository
import com.petal.data.catalog.CatalogRepository
import com.petal.data.checkout.CheckoutRepository

class AppContainer(context: Context) {
    val sessionStore: SessionStore = EncryptedSessionStore(context)
    private val apiService = ApiClient.create(sessionStore)

    val authRepository = AuthRepository(apiService, sessionStore)
    val catalogRepository = CatalogRepository(apiService)
    val cartRepository = CartRepository(apiService)
    val checkoutRepository = CheckoutRepository(apiService)
}
