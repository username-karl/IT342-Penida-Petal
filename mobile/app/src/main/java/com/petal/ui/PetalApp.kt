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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
import com.petal.data.catalog.CatalogFormatters
import com.petal.data.catalog.PetalMoods
import com.petal.data.catalog.ProductResponse
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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

private object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Moods = "moods"
    const val Products = "products/{mood}"
    const val Product = "product/{id}"

    fun products(mood: String) = "products/${Uri.encode(mood)}"
    fun product(id: Long) = "product/$id"
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
                        navController = navController
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
private fun MoodGridScreen(displayName: String, onMood: (String) -> Unit, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(
            title = "Mood Catalog",
            subtitle = "Welcome, $displayName",
            action = { PetalSecondaryButton("Sign out", onLogout) }
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
    onProduct: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(mood) {
        viewModel.loadProducts(mood)
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = moodLabel(mood), subtitle = "Arrangements for this feeling", action = {
            PetalSecondaryButton("Back", onBack)
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
private fun ProductDetailScreen(id: Long, viewModel: CatalogViewModel, navController: NavHostController) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(id) {
        viewModel.loadProduct(id)
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
            state.selectedProduct != null -> ProductDetailContent(state.selectedProduct!!)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailContent(product: ProductResponse) {
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
