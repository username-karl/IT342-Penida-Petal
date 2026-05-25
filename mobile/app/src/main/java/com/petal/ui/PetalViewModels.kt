package com.petal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.petal.core.session.SessionStore
import com.petal.data.auth.AuthFormValidator
import com.petal.data.auth.AuthRepository
import com.petal.data.cart.CartItemResponse
import com.petal.data.cart.CartRepository
import com.petal.data.cart.CartResponse
import com.petal.data.catalog.CatalogRepository
import com.petal.data.catalog.PetalMoods
import com.petal.data.catalog.ProductResponse
import com.petal.data.checkout.CheckoutCardMessageValidator
import com.petal.data.checkout.CheckoutRecipientValidator
import com.petal.data.checkout.CheckoutRepository
import com.petal.data.checkout.CheckoutScheduleValidator
import com.petal.data.checkout.CheckoutStep3Validator
import com.petal.data.checkout.DeliverySlotAvailabilityResponse
import com.petal.data.checkout.OrderResponse
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

data class CartUiState(
    val loading: Boolean = false,
    val actionLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val cart: CartResponse = CartResponse()
)

class CartViewModel(private val repository: CartRepository) : ViewModel() {
    private val _state = MutableStateFlow(CartUiState())
    val state: StateFlow<CartUiState> = _state.asStateFlow()

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }

    fun loadCart() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, successMessage = null) }
            repository.getCart()
                .onSuccess { cart ->
                    _state.update { it.copy(loading = false, cart = cart) }
                }
                .onFailure { failure ->
                    _state.update { it.copy(loading = false, error = failure.message ?: "Unable to load cart.") }
                }
        }
    }

    fun addProduct(productId: Long) {
        if (_state.value.actionLoading) return
        _state.update { it.copy(actionLoading = true, error = null, successMessage = null) }

        viewModelScope.launch {
            repository.addItem(productId = productId, quantity = 1)
                .onSuccess {
                    _state.update { it.copy(actionLoading = false, successMessage = "Added to cart.") }
                }
                .onFailure { failure ->
                    _state.update {
                        it.copy(
                            actionLoading = false,
                            error = failure.message ?: "Unable to add item to cart."
                        )
                    }
                }
        }
    }

    fun updateQuantity(item: CartItemResponse, quantity: Int) {
        if (quantity < 1) return
        if (_state.value.actionLoading) return
        _state.update { it.copy(actionLoading = true, error = null, successMessage = null) }

        viewModelScope.launch {
            repository.updateItem(item.id, quantity)
                .onSuccess {
                    _state.update { it.copy(actionLoading = false) }
                    loadCart()
                }
                .onFailure { failure ->
                    _state.update {
                        it.copy(
                            actionLoading = false,
                            error = failure.message ?: "Unable to update cart item."
                        )
                    }
                }
        }
    }

    fun removeItem(itemId: Long) {
        if (_state.value.actionLoading) return
        _state.update { it.copy(actionLoading = true, error = null, successMessage = null) }

        viewModelScope.launch {
            repository.removeItem(itemId)
                .onSuccess {
                    _state.update { it.copy(actionLoading = false, successMessage = "Item removed.") }
                    loadCart()
                }
                .onFailure { failure ->
                    _state.update {
                        it.copy(
                            actionLoading = false,
                            error = failure.message ?: "Unable to remove cart item."
                        )
                    }
                }
        }
    }
}

data class CheckoutUiState(
    val recipientName: String = "",
    val recipientAddress: String = "",
    val cardMessage: String = "",
    val deliveryEpochDay: Long? = null,
    val deliveryDateLabel: String = "",
    val timeSlot: String = "",
    val slotAvailability: DeliverySlotAvailabilityResponse? = null,
    val slotAvailabilityLoading: Boolean = false,
    val slotAvailabilityError: String? = null,
    val orderLoading: Boolean = false,
    val orderError: String? = null,
    val orderConfirmation: OrderConfirmationUiState? = null,
    val cardMessageError: String? = null,
    val scheduleError: String? = null,
    val error: String? = null
)

data class OrderConfirmationUiState(
    val order: OrderResponse,
    val recipientName: String,
    val recipientAddress: String
)

class CheckoutViewModel(private val repository: CheckoutRepository? = null) : ViewModel() {
    companion object {
        const val mockPaymentMethod = "CARD"
    }

    private val _state = MutableStateFlow(CheckoutUiState())
    val state: StateFlow<CheckoutUiState> = _state.asStateFlow()

    fun setRecipientName(value: String) = _state.update { it.copy(recipientName = value, error = null) }

    fun setRecipientAddress(value: String) = _state.update { it.copy(recipientAddress = value, error = null) }

    fun setCardMessage(value: String) = _state.update {
        it.copy(
            cardMessage = value.take(CheckoutCardMessageValidator.maxLength),
            cardMessageError = null
        )
    }

    fun selectDeliveryDate(epochDay: Long, label: String, cart: CartResponse) {
        _state.update {
            it.copy(
                deliveryEpochDay = epochDay,
                deliveryDateLabel = label,
                timeSlot = "",
                slotAvailability = null,
                slotAvailabilityLoading = false,
                slotAvailabilityError = null,
                orderError = null,
                scheduleError = null
            )
        }

        CheckoutStep3Validator.singleFloristId(cart)
            .onSuccess { floristId -> loadSlotAvailability(floristId, label) }
            .onFailure { failure ->
                _state.update { it.copy(scheduleError = failure.message ?: "Unable to resolve florist for checkout.") }
            }
    }

    fun setTimeSlot(value: String) = _state.update {
        it.copy(timeSlot = value, scheduleError = null, orderError = null)
    }

    fun validateRecipientDetails(): Boolean {
        val current = _state.value
        val error = CheckoutRecipientValidator.validate(current.recipientName, current.recipientAddress)
        _state.update { it.copy(error = error) }
        return error == null
    }

    fun validateCardMessage(): Boolean {
        val current = _state.value
        val error = CheckoutCardMessageValidator.validate(current.cardMessage)
        _state.update { it.copy(cardMessageError = error) }
        return error == null
    }

    fun validateSchedule(): Boolean {
        val current = _state.value
        val scheduleError = CheckoutScheduleValidator.validate(current.deliveryEpochDay, current.timeSlot)
        val availabilityError = if (
            scheduleError == null &&
            CheckoutStep3Validator.selectedSlot(current.slotAvailability, current.timeSlot)?.available != true
        ) {
            "Choose an available delivery time slot."
        } else {
            null
        }
        val error = scheduleError ?: availabilityError
        _state.update { it.copy(scheduleError = error) }
        return error == null
    }

    fun canConfirm(cart: CartResponse): Boolean {
        val current = _state.value
        if (current.orderLoading) return false
        return CheckoutStep3Validator.canConfirm(
            deliveryEpochDay = current.deliveryEpochDay,
            timeSlot = current.timeSlot,
            availability = current.slotAvailability,
            cartHasItems = cart.items.isNotEmpty(),
            recipientName = current.recipientName,
            recipientAddress = current.recipientAddress
        )
    }

    fun placeOrder(cart: CartResponse, onSuccess: () -> Unit) {
        val checkoutRepository = repository ?: return
        val current = _state.value
        if (current.orderLoading) return

        if (!validateSchedule()) {
            return
        }

        val request = CheckoutStep3Validator.createOrderRequest(
            cart = cart,
            recipientName = current.recipientName,
            recipientAddress = current.recipientAddress,
            cardMessage = current.cardMessage,
            deliveryDate = current.deliveryDateLabel,
            timeSlot = current.timeSlot,
            paymentMethod = mockPaymentMethod
        ).getOrElse { failure ->
            _state.update { it.copy(orderError = failure.message ?: "Unable to place order.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(orderLoading = true, orderError = null) }
            checkoutRepository.createOrder(request)
                .onSuccess { order ->
                    _state.update {
                        CheckoutUiState(
                            orderConfirmation = OrderConfirmationUiState(
                                order = order,
                                recipientName = request.recipientName,
                                recipientAddress = request.recipientAddress
                            )
                        )
                    }
                    onSuccess()
                }
                .onFailure { failure ->
                    _state.update {
                        it.copy(
                            orderLoading = false,
                            orderError = failure.message ?: "Unable to place order."
                        )
                    }
                }
        }
    }

    fun clearOrderConfirmation() {
        _state.update { it.copy(orderConfirmation = null) }
    }

    private fun loadSlotAvailability(floristId: Long, date: String) {
        val checkoutRepository = repository ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    slotAvailabilityLoading = true,
                    slotAvailabilityError = null,
                    slotAvailability = null
                )
            }
            checkoutRepository.slotAvailability(floristId = floristId, date = date)
                .onSuccess { availability ->
                    _state.update {
                        it.copy(
                            slotAvailabilityLoading = false,
                            slotAvailability = availability,
                            slotAvailabilityError = null
                        )
                    }
                }
                .onFailure { failure ->
                    _state.update {
                        it.copy(
                            slotAvailabilityLoading = false,
                            slotAvailabilityError = failure.message ?: "Unable to load delivery slots."
                        )
                    }
                }
        }
    }
}

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
    private val cartRepository: CartRepository? = null,
    private val checkoutRepository: CheckoutRepository? = null,
    private val sessionStore: SessionStore? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(requireNotNull(authRepository)) as T
            modelClass.isAssignableFrom(CatalogViewModel::class.java) ->
                CatalogViewModel(requireNotNull(catalogRepository)) as T
            modelClass.isAssignableFrom(CartViewModel::class.java) ->
                CartViewModel(requireNotNull(cartRepository)) as T
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) ->
                CheckoutViewModel(requireNotNull(checkoutRepository)) as T
            modelClass.isAssignableFrom(SessionViewModel::class.java) ->
                SessionViewModel(requireNotNull(sessionStore)) as T
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
    }
}

fun moodLabel(value: String): String = PetalMoods.firstOrNull { it.value == value }?.label ?: value
