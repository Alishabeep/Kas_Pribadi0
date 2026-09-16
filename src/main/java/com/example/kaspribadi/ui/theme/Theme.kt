package com.example.kaspribadi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Warna Hijau Utama
val GreenPrimary = Color(0xFF558B2F)
val GreenSecondary = Color(0xFF689F38)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    background = Color(0xFFF5F5F5),
    surface = Color.White, // Memaksa dialog & card berlatar putih netral (bukan pink)
    onPrimary = Color.White,
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E), // Latar dialog mode gelap
    onPrimary = Color.Black,
    onSurface = Color.White
)

@Composable
fun KasPribadiTheme(
    themeOption: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeOption) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme() // Ikut mode hemat baterai/sistem HP
    }

    // Matikan dynamicColor agar warna HP tidak merusak skema warna hijau
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}