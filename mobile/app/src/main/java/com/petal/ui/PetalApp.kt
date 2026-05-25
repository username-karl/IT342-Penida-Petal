package com.petal.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.petal.PetalApplication
import com.petal.data.cart.CartItemResponse
import com.petal.data.catalog.CatalogFormatters
import com.petal.data.catalog.PetalMoods
import com.petal.data.catalog.ProductResponse
import com.petal.data.checkout.CheckoutCardMessageValidator
import com.petal.data.checkout.CheckoutScheduleValidator
import com.petal.data.checkout.CheckoutStep3Validator
import com.petal.data.checkout.BuyerOrderItemResponse
import com.petal.data.checkout.BuyerOrderResponse
import com.petal.data.checkout.DeliverySlotAvailabilityResponse
import com.petal.data.checkout.OrderDisplayFormatters
import com.petal.ui.components.EmptyPanel
import com.petal.ui.components.ErrorBanner
import com.petal.ui.components.LoadingProductGrid
import com.petal.ui.components.MoodChip
import com.petal.ui.components.PetalHeader
import com.petal.ui.components.PetalPrimaryButton
import com.petal.ui.components.PetalSecondaryButton
import com.petal.ui.components.PetalTextField
import com.petal.ui.components.ProductCard
import com.petal.ui.components.SuccessBanner
import com.petal.ui.theme.BlushSoft
import com.petal.ui.theme.Paper
import com.petal.ui.theme.PetalTheme
import com.petal.ui.theme.SageSoft
import com.petal.ui.theme.Stone200
import com.petal.ui.theme.Stone500
import com.petal.ui.theme.Stone700
import com.petal.ui.theme.Stone950
import com.petal.ui.theme.SurfaceWarm
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Moods = "moods"
    const val Products = "products/{mood}"
    const val Product = "product/{id}"
    const val Cart = "cart"
    const val CheckoutRecipient = "checkout/recipient"
    const val CheckoutCardMessage = "checkout/card-message"
    const val CheckoutSchedulePay = "checkout/schedule-pay"
    const val OrderConfirmation = "checkout/confirmation"
    const val OrderHistory = "orders"
    const val OrderDetail = "orders/{id}"

    fun products(mood: String) = "products/${Uri.encode(mood)}"
    fun product(id: Long) = "product/$id"
    fun orderDetail(id: Long) = "orders/$id"
}

@Composable
fun PetalApp() {
    val application = LocalContext.current.applicationContext as PetalApplication
    val container = application.container
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(
        factory = PetalViewModelFactory(authRepository = container.authRepository)
    )
    val catalogViewModel: CatalogViewModel = viewModel(
        factory = PetalViewModelFactory(catalogRepository = container.catalogRepository)
    )
    val cartViewModel: CartViewModel = viewModel(
        factory = PetalViewModelFactory(cartRepository = container.cartRepository)
    )
    val checkoutViewModel: CheckoutViewModel = viewModel(
        factory = PetalViewModelFactory(checkoutRepository = container.checkoutRepository)
    )
    val orderHistoryViewModel: OrderHistoryViewModel = viewModel(
        factory = PetalViewModelFactory(orderRepository = container.orderRepository)
    )
    val sessionViewModel: SessionViewModel = viewModel(
        factory = PetalViewModelFactory(sessionStore = container.sessionStore)
    )

    PetalTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
            NavHost(
                navController = navController,
                startDestination = if (sessionViewModel.hasToken) Routes.Moods else Routes.Login
            ) {
                composable(Routes.Login) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLogin = {
                            navController.navigate(Routes.Moods) {
                                popUpTo(Routes.Login) { inclusive = true }
                            }
                        },
                        onRegister = { navController.navigate(Routes.Register) }
                    )
                }
                composable(Routes.Register) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onBack = { navController.popBackStack() },
                        onRegistered = {
                            navController.popBackStack()
                            authViewModel.setSuccess("Account created. Sign in to continue.")
                        }
                    )
                }
                composable(Routes.Moods) {
                    MoodGridScreen(
                        displayName = sessionViewModel.displayName,
                        onMood = { mood -> navController.navigate(Routes.products(mood)) },
                        onOrders = { navController.navigate(Routes.OrderHistory) },
                        onLogout = {
                            sessionViewModel.clear()
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Moods) { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    route = Routes.Products,
                    arguments = listOf(navArgument("mood") { type = NavType.StringType })
                ) { entry ->
                    val mood = Uri.decode(entry.arguments?.getString("mood").orEmpty())
                    ProductListScreen(
                        mood = mood,
                        viewModel = catalogViewModel,
                        onBack = { navController.popBackStack() },
                        onCart = { navController.navigate(Routes.Cart) },
                        onProduct = { productId -> navController.navigate(Routes.product(productId)) }
                    )
                }
                composable(
                    route = Routes.Product,
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { entry ->
                    ProductDetailScreen(
                        id = entry.arguments?.getLong("id") ?: 0L,
                        viewModel = catalogViewModel,
                        cartViewModel = cartViewModel,
                        navController = navController
                    )
                }
                composable(Routes.Cart) {
                    CartScreen(
                        viewModel = cartViewModel,
                        onBack = { navController.popBackStack() },
                        onCheckout = { navController.navigate(Routes.CheckoutRecipient) },
                        onProduct = { productId -> navController.navigate(Routes.product(productId)) },
                        onBrowse = { navController.navigate(Routes.Moods) }
                    )
                }
                composable(Routes.CheckoutRecipient) {
                    CheckoutRecipientScreen(
                        viewModel = checkoutViewModel,
                        onBack = { navController.popBackStack() },
                        onNext = { navController.navigate(Routes.CheckoutCardMessage) }
                    )
                }
                composable(Routes.CheckoutCardMessage) {
                    CheckoutCardMessageScreen(
                        viewModel = checkoutViewModel,
                        onBack = { navController.popBackStack() },
                        onNext = { navController.navigate(Routes.CheckoutSchedulePay) }
                    )
                }
                composable(Routes.CheckoutSchedulePay) {
                    CheckoutSchedulePayScreen(
                        viewModel = checkoutViewModel,
                        cartViewModel = cartViewModel,
                        onBack = { navController.popBackStack() },
                        onOrderSuccess = {
                            cartViewModel.loadCart()
                            navController.navigate(Routes.OrderConfirmation) {
                                popUpTo(Routes.CheckoutRecipient) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.OrderConfirmation) {
                    OrderConfirmationScreen(
                        confirmation = checkoutViewModel.state.collectAsState().value.orderConfirmation,
                        onOrders = {
                            checkoutViewModel.clearOrderConfirmation()
                            navController.navigate(Routes.OrderHistory) {
                                popUpTo(Routes.Moods)
                            }
                        },
                        onHome = {
                            checkoutViewModel.clearOrderConfirmation()
                            navController.navigate(Routes.Moods) {
                                popUpTo(Routes.Moods) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.OrderHistory) {
                    OrderHistoryScreen(
                        viewModel = orderHistoryViewModel,
                        onBack = { navController.popBackStack() },
                        onOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
                        onBrowse = { navController.navigate(Routes.Moods) }
                    )
                }
                composable(
                    route = Routes.OrderDetail,
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { entry ->
                    OrderDetailScreen(
                        id = entry.arguments?.getLong("id") ?: 0L,
                        viewModel = orderHistoryViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(viewModel: AuthViewModel, onLogin: () -> Unit, onRegister: () -> Unit) {
    val state by viewModel.state.collectAsState()
    AuthScaffold(eyebrow = "Buyer App", title = "Send flowers by feeling.") {
        state.successMessage?.let {
            SuccessBanner(message = it, modifier = Modifier.padding(bottom = 16.dp))
        }
        PetalTextField("Email", state.email, viewModel::setEmail)
        Spacer(Modifier.height(16.dp))
        PetalTextField(
            label = "Password",
            value = state.password,
            onValueChange = viewModel::setPassword,
            visualTransformation = PasswordVisualTransformation()
        )
        state.error?.let {
            Spacer(Modifier.height(16.dp))
            ErrorBanner(it)
        }
        Spacer(Modifier.height(22.dp))
        PetalPrimaryButton(
            text = "Sign in",
            onClick = { viewModel.login(onLogin) },
            loading = state.loading,
            modifier = Modifier.fillMaxWidth()
        )
        PetalSecondaryButton("Create buyer account", onRegister, Modifier.fillMaxWidth())
    }
}

@Composable
private fun RegisterScreen(viewModel: AuthViewModel, onBack: () -> Unit, onRegistered: () -> Unit) {
    val state by viewModel.state.collectAsState()
    AuthScaffold(eyebrow = "Buyer Account", title = "Join Petal as a gift sender.") {
        PetalTextField("Full name", state.name, viewModel::setName)
        Spacer(Modifier.height(16.dp))
        PetalTextField("Email", state.email, viewModel::setEmail)
        Spacer(Modifier.height(16.dp))
        PetalTextField(
            label = "Password",
            value = state.password,
            onValueChange = viewModel::setPassword,
            visualTransformation = PasswordVisualTransformation()
        )
        Text(
            "Use 8 or more characters with a number and special character. Role is fixed to buyer.",
            modifier = Modifier.padding(top = 8.dp),
            color = Stone500,
            style = MaterialTheme.typography.bodyMedium
        )
        state.error?.let {
            Spacer(Modifier.height(16.dp))
            ErrorBanner(it)
        }
        Spacer(Modifier.height(22.dp))
        PetalPrimaryButton(
            text = "Create account",
            onClick = { viewModel.register(onRegistered) },
            loading = state.loading,
            modifier = Modifier.fillMaxWidth()
        )
        PetalSecondaryButton("Back to sign in", onBack, Modifier.fillMaxWidth())
    }
}

@Composable
private fun AuthScaffold(eyebrow: String, title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text("Petal", style = MaterialTheme.typography.displayMedium)
        Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = Stone500)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(28.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                .background(SurfaceWarm, RoundedCornerShape(4.dp))
                .padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun MoodGridScreen(
    displayName: String,
    onMood: (String) -> Unit,
    onOrders: () -> Unit,
    onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(
            title = "Mood Catalog",
            subtitle = "Welcome, $displayName",
            action = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PetalSecondaryButton("Orders", onOrders)
                    PetalSecondaryButton("Sign out", onLogout)
                }
            }
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(PetalMoods) { mood ->
                val tint = if (mood.value in listOf("romance", "apology")) BlushSoft else SageSoft
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onMood(mood.value) }
                        .background(tint)
                        .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                        .padding(16.dp)
                ) {
                    Text(mood.label, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(mood.note, color = Stone700, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ProductListScreen(
    mood: String,
    viewModel: CatalogViewModel,
    onBack: () -> Unit,
    onCart: () -> Unit,
    onProduct: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(mood) {
        viewModel.loadProducts(mood)
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = moodLabel(mood), subtitle = "Arrangements for this feeling", action = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PetalSecondaryButton("Cart", onCart)
                PetalSecondaryButton("Back", onBack)
            }
        })
        when {
            state.loading -> LoadingProductGrid(Modifier.padding(20.dp))
            state.error != null -> ErrorBanner(state.error.orEmpty(), Modifier.padding(20.dp))
            state.products.isEmpty() -> EmptyPanel(
                title = "No arrangements found",
                message = "Try another mood to discover more local creations.",
                modifier = Modifier.padding(20.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                items(state.products) { product ->
                    ProductCard(product = product, onClick = { onProduct(product.id) })
                }
            }
        }
    }
}

@Composable
private fun ProductDetailScreen(
    id: Long,
    viewModel: CatalogViewModel,
    cartViewModel: CartViewModel,
    navController: NavHostController
) {
    val state by viewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    LaunchedEffect(id) {
        viewModel.loadProduct(id)
        cartViewModel.clearMessages()
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Arrangement Detail", action = {
            PetalSecondaryButton("Back", onClick = { navController.popBackStack() })
        })
        when {
            state.loading -> LoadingProductGrid(Modifier.padding(20.dp))
            state.error != null -> EmptyPanel(
                title = "Product unavailable",
                message = state.error.orEmpty(),
                modifier = Modifier.padding(20.dp)
            )
            state.selectedProduct != null -> ProductDetailContent(
                product = state.selectedProduct!!,
                cartState = cartState,
                onAddToCart = { cartViewModel.addProduct(state.selectedProduct!!.id) },
                onViewCart = { navController.navigate(Routes.Cart) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailContent(
    product: ProductResponse,
    cartState: CartUiState,
    onAddToCart: () -> Unit,
    onViewCart: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .background(SurfaceWarm)
                    .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
            )
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                product.moodTags.forEach { tag -> MoodChip(tag) }
            }
            Spacer(Modifier.height(12.dp))
            Text(product.name, style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(8.dp))
            Text(CatalogFormatters.formatPeso(product.price), color = Stone950, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(if (product.inStock) "In stock" else "Currently unavailable", color = if (product.inStock) Stone700 else Stone500)
        }
        item {
            InfoPanel(title = product.floristName ?: "Local Petal Florist", body = product.floristBio ?: "Prepared by a local Petal florist.")
        }
        item {
            InfoPanel(title = "Product Description", body = product.description)
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceWarm, RoundedCornerShape(4.dp))
                    .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                    .padding(18.dp)
            ) {
                cartState.successMessage?.let {
                    SuccessBanner(message = it, modifier = Modifier.padding(bottom = 12.dp))
                }
                cartState.error?.let {
                    ErrorBanner(message = it, modifier = Modifier.padding(bottom = 12.dp))
                }
                PetalPrimaryButton(
                    text = if (product.inStock) "Add to Cart" else "Currently Unavailable",
                    onClick = onAddToCart,
                    enabled = product.inStock,
                    loading = cartState.actionLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                if (cartState.successMessage != null) {
                    PetalSecondaryButton("View Cart", onViewCart, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun CartScreen(
    viewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onProduct: (Long) -> Unit,
    onBrowse: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Your Petal Basket", subtitle = "Review gifts before checkout", action = {
            PetalSecondaryButton("Back", onBack)
        })
        when {
            state.loading -> LoadingProductGrid(Modifier.padding(20.dp))
            state.error != null && state.cart.items.isEmpty() -> ErrorBanner(state.error.orEmpty(), Modifier.padding(20.dp))
            state.cart.items.isEmpty() -> EmptyPanel(
                title = "Your basket is empty",
                message = "Add an arrangement to begin checkout.",
                modifier = Modifier.padding(20.dp)
            )
            else -> CartContent(
                state = state,
                onProduct = onProduct,
                onQuantity = viewModel::updateQuantity,
                onRemove = viewModel::removeItem,
                onCheckout = onCheckout
            )
        }
        if (!state.loading && state.cart.items.isEmpty()) {
            PetalPrimaryButton("Browse Moods", onBrowse, Modifier.padding(20.dp).fillMaxWidth())
        }
    }
}

@Composable
private fun CartContent(
    state: CartUiState,
    onProduct: (Long) -> Unit,
    onQuantity: (CartItemResponse, Int) -> Unit,
    onRemove: (Long) -> Unit,
    onCheckout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        state.error?.let { error ->
            item { ErrorBanner(error) }
        }
        state.successMessage?.let { message ->
            item { SuccessBanner(message) }
        }
        items(state.cart.items) { item ->
            CartItemCard(
                item = item,
                actionLoading = state.actionLoading,
                onProduct = { onProduct(item.productId) },
                onQuantity = { quantity -> onQuantity(item, quantity) },
                onRemove = { onRemove(item.id) }
            )
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceWarm, RoundedCornerShape(4.dp))
                    .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                    .padding(18.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = Stone500)
                    Text(CatalogFormatters.formatPeso(state.cart.subtotal), color = Stone950, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleLarge)
                    Text(CatalogFormatters.formatPeso(state.cart.subtotal), style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(16.dp))
                PetalPrimaryButton(
                    text = "Proceed to Checkout",
                    onClick = onCheckout,
                    enabled = !state.actionLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemResponse,
    actionLoading: Boolean,
    onProduct: () -> Unit,
    onQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWarm, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = item.productImageUrl,
            contentDescription = item.productName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceWarm)
                .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                .clickable(onClick = onProduct)
        )
        Column(Modifier.weight(1f)) {
            Text(item.floristName ?: "Local Petal Florist", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(item.productName, style = MaterialTheme.typography.titleLarge)
            Text(CatalogFormatters.formatPeso(item.unitPrice), color = Stone700)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PetalSecondaryButton("-", onClick = { onQuantity(item.quantity - 1) }, modifier = Modifier.size(44.dp))
                Text(item.quantity.toString(), color = Stone950, fontWeight = FontWeight.SemiBold)
                PetalSecondaryButton("+", onClick = { onQuantity(item.quantity + 1) }, modifier = Modifier.size(44.dp))
                PetalSecondaryButton("Remove", onClick = onRemove)
            }
            if (actionLoading) {
                Text("Updating...", color = Stone500, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(CatalogFormatters.formatPeso(item.lineTotal), color = Stone950, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CheckoutRecipientScreen(viewModel: CheckoutViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Checkout", subtitle = "Step 1 of 3", action = {
            PetalSecondaryButton("Back", onBack)
        })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Recipient Details", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(18.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceWarm, RoundedCornerShape(4.dp))
                    .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                    .padding(18.dp)
            ) {
                PetalTextField("Recipient Name", state.recipientName, viewModel::setRecipientName)
                Spacer(Modifier.height(16.dp))
                PetalTextField(
                    label = "Recipient Address",
                    value = state.recipientAddress,
                    onValueChange = viewModel::setRecipientAddress,
                    singleLine = false
                )
                state.error?.let {
                    Spacer(Modifier.height(16.dp))
                    ErrorBanner(it)
                }
                Spacer(Modifier.height(22.dp))
                PetalPrimaryButton(
                    text = "Next",
                    onClick = {
                        if (viewModel.validateRecipientDetails()) {
                            onNext()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CheckoutCardMessageScreen(viewModel: CheckoutViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Checkout", subtitle = "Step 2 of 3", action = {
            PetalSecondaryButton("Back", onBack)
        })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Card Message", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(18.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceWarm, RoundedCornerShape(4.dp))
                    .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                    .padding(18.dp)
            ) {
                PetalTextField(
                    label = "Message",
                    value = state.cardMessage,
                    onValueChange = viewModel::setCardMessage,
                    singleLine = false
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Optional", color = Stone500, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${state.cardMessage.length}/${CheckoutCardMessageValidator.maxLength}",
                        color = Stone500,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                state.cardMessageError?.let {
                    Spacer(Modifier.height(16.dp))
                    ErrorBanner(it)
                }
                Spacer(Modifier.height(18.dp))
                CardMessagePreview(message = state.cardMessage)
                Spacer(Modifier.height(22.dp))
                PetalPrimaryButton(
                    text = "Continue",
                    onClick = {
                        if (viewModel.validateCardMessage()) {
                            onNext()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CardMessagePreview(message: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BlushSoft, RoundedCornerShape(8.dp))
            .border(1.dp, SageSoft, RoundedCornerShape(8.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Petal", style = MaterialTheme.typography.titleLarge, color = Stone950)
        Spacer(Modifier.height(12.dp))
        Text(
            if (message.isBlank()) "Your note will be tucked beside the bouquet."
            else message.trim(),
            color = Stone700,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CheckoutSchedulePayScreen(
    viewModel: CheckoutViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()
    val scheduleValid = CheckoutStep3Validator.canConfirm(
        deliveryEpochDay = state.deliveryEpochDay,
        timeSlot = state.timeSlot,
        availability = state.slotAvailability,
        cartHasItems = cartState.cart.items.isNotEmpty(),
        recipientName = state.recipientName,
        recipientAddress = state.recipientAddress
    ) && !state.orderLoading

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Checkout", subtitle = "Step 3 of 3", action = {
            PetalSecondaryButton("Back", onBack)
        })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Schedule & Pay", style = MaterialTheme.typography.displayMedium)
            }
            item {
                SchedulePicker(
                    selectedDate = state.deliveryEpochDay,
                    selectedSlot = state.timeSlot,
                    availability = state.slotAvailability,
                    loading = state.slotAvailabilityLoading,
                    availabilityError = state.slotAvailabilityError,
                    error = state.scheduleError,
                    onDate = { epochDay, label -> viewModel.selectDeliveryDate(epochDay, label, cartState.cart) },
                    onSlot = viewModel::setTimeSlot
                )
            }
            item {
                CheckoutOrderSummary(
                    cartState = cartState,
                    checkoutState = state
                )
            }
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceWarm, RoundedCornerShape(4.dp))
                        .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                        .padding(18.dp)
                ) {
                    Text("Mock payment", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This submits the order with a mock card payment. No real payment is processed.",
                        color = Stone700,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    state.orderError?.let {
                        Spacer(Modifier.height(16.dp))
                        ErrorBanner(it)
                    }
                    Spacer(Modifier.height(18.dp))
                    PetalPrimaryButton(
                        text = "Confirm & Pay (Mock)",
                        onClick = { viewModel.placeOrder(cartState.cart, onOrderSuccess) },
                        enabled = scheduleValid,
                        loading = state.orderLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderConfirmationScreen(
    confirmation: OrderConfirmationUiState?,
    onOrders: () -> Unit,
    onHome: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Order Confirmed", subtitle = "Your gift is on its way")
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (confirmation == null) {
                EmptyPanel(
                    title = "Confirmation unavailable",
                    message = "Return home to continue shopping."
                )
                Spacer(Modifier.height(18.dp))
                PetalPrimaryButton("Return Home", onHome, Modifier.fillMaxWidth())
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(SageSoft, RoundedCornerShape(8.dp))
                        .border(1.dp, Stone200, RoundedCornerShape(8.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✓", style = MaterialTheme.typography.displayMedium, color = Stone950)
                    Spacer(Modifier.height(8.dp))
                    Text("Order placed", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(confirmation.order.message ?: "Your order has been received.", color = Stone700)
                }
                Spacer(Modifier.height(18.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceWarm, RoundedCornerShape(4.dp))
                        .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                        .padding(18.dp)
                ) {
                    SummaryRow("Order ID", confirmation.order.id.toString())
                    SummaryRow("Status", confirmation.order.status)
                    SummaryRow("Delivery Date", confirmation.order.deliveryDate ?: "Not set")
                    SummaryRow("Time Slot", confirmation.order.timeSlot ?: "Not set")
                    SummaryRow("Payment Method", confirmation.order.paymentMethod ?: CheckoutViewModel.mockPaymentMethod)
                    SummaryDivider()
                    SummaryTextBlock(
                        title = confirmation.recipientName,
                        body = confirmation.recipientAddress
                    )
                }
                Spacer(Modifier.height(18.dp))
                PetalPrimaryButton("Track My Orders", onOrders, Modifier.fillMaxWidth())
                PetalSecondaryButton("Return Home", onHome, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun OrderHistoryScreen(
    viewModel: OrderHistoryViewModel,
    onBack: () -> Unit,
    onOrder: (Long) -> Unit,
    onBrowse: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Order History", subtitle = "Track gifts after checkout", action = {
            PetalSecondaryButton("Back", onBack)
        })
        when {
            state.loading -> LoadingProductGrid(Modifier.padding(20.dp))
            state.error != null -> ErrorBanner(state.error.orEmpty(), Modifier.padding(20.dp))
            state.orders.isEmpty() -> {
                EmptyPanel(
                    title = "No orders yet",
                    message = "Placed gifts will appear here after checkout.",
                    modifier = Modifier.padding(20.dp)
                )
                PetalPrimaryButton("Browse Moods", onBrowse, Modifier.padding(20.dp).fillMaxWidth())
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(state.orders) { order ->
                    OrderHistoryCard(order = order, onClick = { onOrder(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(order: BuyerOrderResponse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .background(SurfaceWarm, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(order.orderNumber ?: "Order #${order.id}", style = MaterialTheme.typography.titleLarge)
                Text(order.itemSummary ?: "Petal gift order", color = Stone500, style = MaterialTheme.typography.bodyMedium)
            }
            StatusBadge(order.status)
        }
        Spacer(Modifier.height(14.dp))
        SummaryRow("Order ID", order.id.toString())
        SummaryRow("Delivery Date", OrderDisplayFormatters.orFallback(order.deliveryDate, "Not set"))
        SummaryRow("Time Slot", OrderDisplayFormatters.orFallback(order.timeSlot, "Not set"))
        SummaryRow("Total", OrderDisplayFormatters.formatOptionalPeso(order.totalAmount))
        SummaryRow("Payment Method", OrderDisplayFormatters.orFallback(order.paymentMethod))
    }
}

@Composable
private fun OrderDetailScreen(id: Long, viewModel: OrderHistoryViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(id) {
        viewModel.loadOrder(id)
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Order Detail", subtitle = "Gift status and delivery details", action = {
            PetalSecondaryButton("Back", onBack)
        })
        when {
            state.detailLoading && state.selectedOrder == null -> LoadingProductGrid(Modifier.padding(20.dp))
            state.detailError != null && state.selectedOrder == null -> ErrorBanner(state.detailError.orEmpty(), Modifier.padding(20.dp))
            state.selectedOrder != null -> OrderDetailContent(order = state.selectedOrder!!, detailError = state.detailError)
        }
    }
}

@Composable
private fun OrderDetailContent(order: BuyerOrderResponse, detailError: String?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        detailError?.let { error ->
            item { ErrorBanner(error) }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(SageSoft, RoundedCornerShape(8.dp))
                    .border(1.dp, Stone200, RoundedCornerShape(8.dp))
                    .padding(18.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(order.orderNumber ?: "Order #${order.id}", style = MaterialTheme.typography.headlineSmall)
                        Text("Order ID ${order.id}", color = Stone700)
                    }
                    StatusBadge(order.status)
                }
            }
        }
        item {
            InfoPanel(
                title = "Schedule",
                body = listOf(
                    "Delivery date: ${OrderDisplayFormatters.orFallback(order.deliveryDate, "Not set")}",
                    "Time slot: ${OrderDisplayFormatters.orFallback(order.timeSlot, "Not set")}",
                    "Payment: ${OrderDisplayFormatters.orFallback(order.paymentMethod)}",
                    "Total: ${OrderDisplayFormatters.formatOptionalPeso(order.totalAmount)}"
                ).joinToString("\n")
            )
        }
        item {
            InfoPanel(
                title = OrderDisplayFormatters.orFallback(order.recipientName, "Recipient"),
                body = OrderDisplayFormatters.orFallback(order.recipientAddress, "Recipient address not provided")
            )
        }
        order.cardMessage?.trim()?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                InfoPanel(title = "Card Message", body = message)
            }
        }
        if (order.items.isNotEmpty()) {
            item {
                Text("Items", style = MaterialTheme.typography.headlineSmall)
            }
            items(order.items) { item ->
                OrderItemRow(item)
            }
        } else {
            item {
                InfoPanel(title = "Items", body = order.itemSummary ?: "No item details returned.")
            }
        }
        order.shipping?.let { shipping ->
            item {
                InfoPanel(
                    title = "Delivery Status",
                    body = listOf(
                        "Courier: ${OrderDisplayFormatters.orFallback(shipping.courierName)}",
                        "Tracking: ${OrderDisplayFormatters.orFallback(shipping.trackingNumber)}",
                        "Latest: ${OrderDisplayFormatters.orFallback(shipping.latestStatus, OrderDisplayFormatters.statusLabel(order.status))}",
                        "Estimated: ${OrderDisplayFormatters.orFallback(shipping.estimatedDeliveryDate, "Not set")}"
                    ).joinToString("\n")
                )
            }
            if (shipping.events.isNotEmpty()) {
                item {
                    Text("Tracking Updates", style = MaterialTheme.typography.headlineSmall)
                }
                items(shipping.events) { event ->
                    InfoPanel(
                        title = OrderDisplayFormatters.orFallback(event.status, "Update"),
                        body = listOfNotNull(
                            event.description?.trim()?.takeIf { it.isNotBlank() },
                            event.timestamp?.trim()?.takeIf { it.isNotBlank() }
                        ).joinToString("\n").ifBlank { "Tracking update received." }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: BuyerOrderItemResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWarm, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.productName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceWarm)
                .border(1.dp, Stone200, RoundedCornerShape(4.dp))
        )
        Column(Modifier.weight(1f)) {
            Text(item.floristName ?: "Local Petal Florist", style = MaterialTheme.typography.labelSmall)
            Text(item.productName ?: "Petal arrangement", style = MaterialTheme.typography.titleLarge)
            Text("Qty ${item.quantity}", color = Stone500, style = MaterialTheme.typography.bodyMedium)
        }
        Text(OrderDisplayFormatters.formatOptionalPeso(item.lineTotal), color = Stone950, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val normalized = status?.trim()?.uppercase(Locale.US)
    val background = when (normalized) {
        "DELIVERED", "COMPLETED" -> SageSoft
        "ARRANGING", "PREPARING", "ACCEPTED", "READY_FOR_PICKUP", "OUT_FOR_DELIVERY", "SHIPPED" -> BlushSoft
        "CANCELLED" -> Stone200
        else -> SurfaceWarm
    }
    val textColor = when (normalized) {
        "CANCELLED" -> Stone500
        else -> Stone950
    }
    Text(
        text = OrderDisplayFormatters.statusLabel(status),
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .border(1.dp, Stone200, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = textColor,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun SchedulePicker(
    selectedDate: Long?,
    selectedSlot: String,
    availability: DeliverySlotAvailabilityResponse?,
    loading: Boolean,
    availabilityError: String?,
    error: String?,
    onDate: (Long, String) -> Unit,
    onSlot: (String) -> Unit
) {
    val options = deliveryDateOptions()

    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceWarm, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(18.dp)
    ) {
        Text("Delivery Date", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        options.forEach { option ->
            val selected = selectedDate == option.epochDay
            DateOptionRow(
                label = option.label,
                detail = option.detail,
                selected = selected,
                onClick = { onDate(option.epochDay, option.detail) }
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text("Time Slot", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (loading) {
            Text("Loading delivery slots...", color = Stone500, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
        }
        availabilityError?.let {
            ErrorBanner(it)
            Spacer(Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            val amAvailable = availability?.am?.available == true
            val pmAvailable = availability?.pm?.available == true
            SlotButton(
                text = slotLabel("AM", availability?.am?.remaining),
                selected = selectedSlot == CheckoutScheduleValidator.morningSlot,
                onClick = { onSlot(CheckoutScheduleValidator.morningSlot) },
                enabled = amAvailable && !loading,
                modifier = Modifier.weight(1f)
            )
            SlotButton(
                text = slotLabel("PM", availability?.pm?.remaining),
                selected = selectedSlot == CheckoutScheduleValidator.afternoonSlot,
                onClick = { onSlot(CheckoutScheduleValidator.afternoonSlot) },
                enabled = pmAvailable && !loading,
                modifier = Modifier.weight(1f)
            )
        }
        error?.let {
            Spacer(Modifier.height(16.dp))
            ErrorBanner(it)
        }
    }
}

private fun slotLabel(slot: String, remaining: Int?): String {
    return if (remaining == null) {
        slot
    } else {
        "$slot\n$remaining left"
    }
}

private data class DeliveryDateOption(
    val epochDay: Long,
    val label: String,
    val detail: String
)

private fun deliveryDateOptions(): List<DeliveryDateOption> {
    val base = Calendar.getInstance()
    val detailFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val labelFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    return (0..4).map { offset ->
        val calendar = base.clone() as Calendar
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        DeliveryDateOption(
            epochDay = CheckoutScheduleValidator.epochDay(calendar),
            label = when (offset) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> labelFormat.format(calendar.time)
            },
            detail = detailFormat.format(calendar.time)
        )
    }
}

@Composable
private fun DateOptionRow(label: String, detail: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) SageSoft else Paper
    val border = if (selected) Stone950 else Stone200
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .background(background, RoundedCornerShape(4.dp))
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Stone950, fontWeight = FontWeight.SemiBold)
        Text(detail, color = Stone500, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SlotButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val background = when {
        selected -> BlushSoft
        enabled -> Paper
        else -> SurfaceWarm
    }
    val border = if (selected) Stone950 else Stone200
    val contentColor = if (enabled) Stone950 else Stone500
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .background(background, RoundedCornerShape(4.dp))
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, color = contentColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CheckoutOrderSummary(cartState: CartUiState, checkoutState: CheckoutUiState) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceWarm, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(18.dp)
    ) {
        Text("Order Summary", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (cartState.cart.items.isEmpty()) {
            Text("Your cart summary will appear here after adding flowers.", color = Stone500)
        } else {
            cartState.cart.items.forEach { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(item.productName, color = Stone950, fontWeight = FontWeight.SemiBold)
                        Text("Qty ${item.quantity}", color = Stone500, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(CatalogFormatters.formatPeso(item.lineTotal), color = Stone950)
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        SummaryDivider()
        SummaryRow("Subtotal", CatalogFormatters.formatPeso(cartState.cart.subtotal))
        SummaryRow("Total", CatalogFormatters.formatPeso(cartState.cart.subtotal))
        SummaryDivider()
        SummaryTextBlock(
            title = checkoutState.recipientName.trim().ifBlank { "Recipient" },
            body = checkoutState.recipientAddress.trim().ifBlank { "Recipient address not set" }
        )
        Spacer(Modifier.height(12.dp))
        SummaryTextBlock(
            title = "Card Message",
            body = checkoutState.cardMessage.trim().ifBlank { "No card message added." }
        )
        Spacer(Modifier.height(12.dp))
        SummaryTextBlock(
            title = "Delivery",
            body = listOf(checkoutState.deliveryDateLabel, checkoutState.timeSlot)
                .filter { it.isNotBlank() }
                .joinToString(" - ")
                .ifBlank { "Delivery schedule not selected." }
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Stone500)
        Text(value, color = Stone950, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SummaryDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Stone200)
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun SummaryTextBlock(title: String, body: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(body, color = Stone700, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun InfoPanel(title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceWarm, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(18.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(body, color = Stone700, style = MaterialTheme.typography.bodyMedium)
    }
}
