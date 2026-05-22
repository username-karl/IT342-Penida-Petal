package com.petal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.petal.core.session.SessionStore
import com.petal.data.auth.AuthFormValidator
import com.petal.data.auth.AuthRepository
import com.petal.data.catalog.CatalogRepository
import com.petal.data.catalog.PetalMoods
import com.petal.data.catalog.ProductResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun setEmail(value: String) = _state.update { it.copy(email = value, error = null) }
    fun setPassword(value: String) = _state.update { it.copy(password = value, error = null) }
    fun setName(value: String) = _state.update { it.copy(name = value, error = null) }
    fun setSuccess(message: String?) = _state.update { it.copy(successMessage = message) }

    fun login(onSuccess: () -> Unit) {
        val current = _state.value
        AuthFormValidator.loginError(current.email, current.password)?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repository.login(current.email, current.password)
                .onSuccess {
                    _state.update { AuthUiState() }
                    onSuccess()
                }
                .onFailure { failure ->
                    _state.update { it.copy(loading = false, error = failure.message ?: "Unable to sign in.") }
                }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val current = _state.value
        AuthFormValidator.registerError(current.name, current.email, current.password)?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repository.register(current.name, current.email, current.password)
                .onSuccess {
                    _state.update { AuthUiState(successMessage = "Account created. Sign in to continue.") }
                    onSuccess()
                }
                .onFailure { failure ->
                    _state.update { it.copy(loading = false, error = failure.message ?: "Unable to create account.") }
                }
        }
    }
}

data class CatalogUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val products: List<ProductResponse> = emptyList(),
    val selectedProduct: ProductResponse? = null
)

class CatalogViewModel(private val repository: CatalogRepository) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    fun loadProducts(mood: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, selectedProduct = null) }
            repository.products(mood)
                .onSuccess { products ->
                    _state.update { it.copy(loading = false, products = products) }
                }
                .onFailure { failure ->
                    _state.update { it.copy(loading = false, products = emptyList(), error = failure.message) }
                }
        }
    }

    fun loadProduct(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, selectedProduct = null) }
            repository.product(id)
                .onSuccess { product ->
                    _state.update { it.copy(loading = false, selectedProduct = product) }
                }
                .onFailure { failure ->
                    _state.update { it.copy(loading = false, error = failure.message ?: "This arrangement is unavailable.") }
                }
        }
    }
}

class SessionViewModel(private val sessionStore: SessionStore) : ViewModel() {
    val hasToken: Boolean = sessionStore.token() != null
    val displayName: String = sessionStore.userName()?.substringBefore(" ") ?: "Buyer"

    fun clear() {
        sessionStore.clear()
    }
}

class PetalViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val catalogRepository: CatalogRepository? = null,
    private val sessionStore: SessionStore? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(requireNotNull(authRepository)) as T
            modelClass.isAssignableFrom(CatalogViewModel::class.java) ->
                CatalogViewModel(requireNotNull(catalogRepository)) as T
            modelClass.isAssignableFrom(SessionViewModel::class.java) ->
                SessionViewModel(requireNotNull(sessionStore)) as T
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
    }
}

fun moodLabel(value: String): String = PetalMoods.firstOrNull { it.value == value }?.label ?: value
