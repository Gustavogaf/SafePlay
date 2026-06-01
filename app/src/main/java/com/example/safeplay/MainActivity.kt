package com.example.safeplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// Certifique-se de que estes imports correspondem à estrutura das suas pastas
import com.example.safeplay.core.theme.SafePlayTheme
import com.example.safeplay.features.auth.AuthScreen
import com.example.safeplay.features.auth.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafePlayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Chama a navegação central em vez de um ecrã específico!
                    com.example.safeplay.core.navigation.SafePlayNavigation()
                }
            }
        }
    }
}