package com.example.safeplay.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: Quando tiver a fonte exata do design (ex: Poppins, Nunito),
// importaremos os ficheiros .ttf para a pasta res/font/ e criaremos a variável abaixo.
/*
val SafePlayFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_bold, FontWeight.Bold)
)
*/

// Configuração da tipografia baseada no Material 3 e adaptada para o SafePlay
val Typography = Typography(
    // Títulos grandes e de destaque (ex: "SafePlay", "Nível 5")
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default, // TODO: Trocar por SafePlayFontFamily futuramente
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    // Títulos de secções e ecrãs (ex: "Vamos Começar?", "Crie um Escudo!")
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Texto normal, corpo e instruções (ex: narrativas e perguntas dos desafios)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Texto dos botões primários (ex: "Entrar no SafePlay", "Confirmar")
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Texto para dicas de erro e textos mais pequenos
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)