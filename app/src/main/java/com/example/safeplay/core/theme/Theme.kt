package com.example.safeplay.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Mapeamento das nossas cores para o sistema do Material Design
private val SafePlayColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    background = BackgroundLight,
    surface = BackgroundLight,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    tertiary = GamificationPurple,
    error = WarningRed
)

@Composable
fun SafePlayTheme(
    // Como o foco é o público infantojuvenil com alto contraste (RF01),
    // vamos manter o tema claro como padrão para o MVP.
    content: @Composable () -> Unit
) {
    val colorScheme = SafePlayColorScheme
    val view = LocalView.current

    // Este bloco altera a cor da barra de estado (onde ficam as horas e a bateria no telemóvel)
    // para combinar com o Azul Principal do SafePlay.
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // Aplica as nossas configurações ao MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}