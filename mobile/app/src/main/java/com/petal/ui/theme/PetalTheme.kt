package com.petal.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Paper = Color(0xFFFDFCF8)
val Surface = Color(0xFFFFFFFF)
val SurfaceWarm = Color(0xFFFFFDF9)
val Stone950 = Color(0xFF1C1917)
val Stone700 = Color(0xFF44403C)
val Stone500 = Color(0xFF78716C)
val Stone300 = Color(0xFFD6D3D1)
val Stone200 = Color(0xFFE7E5E4)
val Stone100 = Color(0xFFF5F5F4)
val Blush = Color(0xFFD8A1A1)
val BlushSoft = Color(0xFFF7E4E1)
val Sage = Color(0xFF8A9A78)
val SageSoft = Color(0xFFEEF2E8)
val Error = Color(0xFFB42318)
val ErrorSoft = Color(0xFFFFEDEA)
val Success = Color(0xFF2F6F4E)

private val PetalColorScheme: ColorScheme = lightColorScheme(
    primary = Stone950,
    onPrimary = Color.White,
    secondary = Sage,
    onSecondary = Stone950,
    background = Paper,
    onBackground = Stone950,
    surface = Surface,
    onSurface = Stone950,
    surfaceVariant = SurfaceWarm,
    onSurfaceVariant = Stone700,
    error = Error,
    outline = Stone200
)

@Composable
fun PetalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PetalColorScheme,
        typography = MaterialTheme.typography.copy(
            displayMedium = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 42.sp,
                lineHeight = 42.sp,
                color = Stone950
            ),
            headlineSmall = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                color = Stone950
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                color = Stone950
            ),
            bodyMedium = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Stone700
            ),
            labelSmall = TextStyle(
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Medium,
                color = Stone500
            )
        ),
        content = content
    )
}
