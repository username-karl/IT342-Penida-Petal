package com.petal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petal.data.catalog.CatalogFormatters
import com.petal.data.catalog.ProductResponse
import com.petal.ui.theme.BlushSoft
import com.petal.ui.theme.Error
import com.petal.ui.theme.ErrorSoft
import com.petal.ui.theme.Paper
import com.petal.ui.theme.Sage
import com.petal.ui.theme.SageSoft
import com.petal.ui.theme.Stone100
import com.petal.ui.theme.Stone200
import com.petal.ui.theme.Stone300
import com.petal.ui.theme.Stone500
import com.petal.ui.theme.Stone700
import com.petal.ui.theme.Stone950
import com.petal.ui.theme.Success
import com.petal.ui.theme.Surface
import com.petal.ui.theme.SurfaceWarm

@Composable
fun PetalTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedIndicatorColor = Stone950,
                unfocusedIndicatorColor = Stone200,
                cursorColor = Stone950
            ),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun PetalPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Stone950,
            contentColor = Color.White,
            disabledContainerColor = Stone300,
            disabledContentColor = Stone700
        )
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Text(text, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PetalSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.height(48.dp)) {
        Text(text, color = Stone950, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ErrorSoft,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Error.copy(alpha = 0.18f))
    ) {
        Text(message, color = Error, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SuccessBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SageSoft,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Success.copy(alpha = 0.2f))
    ) {
        Text(message, color = Success, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EmptyPanel(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Stone300, RoundedCornerShape(4.dp))
            .background(SurfaceWarm, RoundedCornerShape(4.dp))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(message, color = Stone500)
    }
}

@Composable
fun LoadingProductGrid(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        repeat(4) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Stone100, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun ProductCard(product: ProductResponse, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .background(Stone100)
                .border(1.dp, Stone200, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.height(12.dp))
        Text(
            (product.floristName ?: "Local Petal Florist").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                product.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(CatalogFormatters.formatPeso(product.price), color = Stone950, fontWeight = FontWeight.Medium)
        }
        Text(
            product.description,
            color = Stone500,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MoodChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier
            .background(BlushSoft, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = Stone700,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun PetalHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paper)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text("Petal", style = MaterialTheme.typography.titleLarge)
            Text(title, color = Stone500, style = MaterialTheme.typography.labelSmall)
            if (subtitle != null) {
                Text(subtitle, color = Sage, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (action != null) action()
    }
}
