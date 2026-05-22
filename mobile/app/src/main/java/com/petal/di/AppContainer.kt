package com.petal.di

import android.content.Context
import com.petal.core.network.ApiClient
import com.petal.core.session.EncryptedSessionStore
import com.petal.core.session.SessionStore
import com.petal.data.auth.AuthRepository
import com.petal.data.catalog.CatalogRepository

class AppContainer(context: Context) {
    val sessionStore: SessionStore = EncryptedSessionStore(context)
    private val apiService = ApiClient.create(sessionStore)

    val authRepository = AuthRepository(apiService, sessionStore)
    val catalogRepository = CatalogRepository(apiService)
}
