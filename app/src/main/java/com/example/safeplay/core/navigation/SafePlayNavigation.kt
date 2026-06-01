package com.example.safeplay.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safeplay.features.auth.AuthScreen
import com.example.safeplay.features.auth.RegisterScreen
import com.example.safeplay.features.trilha.TrilhaScreen

// Objecto que guarda os nomes exactos das nossas rotas para evitar erros de digitação
object Rotas {
    const val LOGIN = "login"
    const val REGISTO = "registo"
    const val TRILHA_ALUNO = "trilha_aluno"
    const val PAINEL_EDUCADOR = "painel_educador"
}

@Composable
fun SafePlayNavigation() {
    // Este é o "motorista" da nossa aplicação
    val navController = rememberNavController()

    // O NavHost é o mapa. Dizemos-lhe que o ecrã inicial (startDestination) é o LOGIN
    NavHost(navController = navController, startDestination = Rotas.LOGIN) {

        // 1. ROTA DE LOGIN
        composable(Rotas.LOGIN) {
            AuthScreen(
                onNavigateToRegister = {
                    // Vai para o ecrã de registo
                    navController.navigate(Rotas.REGISTO)
                },
                onLoginSuccess = { papel ->
                    // Regra de Negócio: Se for aluno, vai para a Trilha. Se for educador, vai para o Painel.
                    val rotaDestino = if (papel == "aluno") Rotas.TRILHA_ALUNO else Rotas.PAINEL_EDUCADOR

                    navController.navigate(rotaDestino) {
                        // popUpTo garante que o utilizador não consegue voltar ao ecrã de Login ao premir o botão "Voltar" do telemóvel
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 2. ROTA DE REGISTO
        composable(Rotas.REGISTO) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { papel ->
                    // Exatamente a mesma lógica de sucesso do Login
                    val rotaDestino = if (papel == "aluno") Rotas.TRILHA_ALUNO else Rotas.PAINEL_EDUCADOR

                    navController.navigate(rotaDestino) {
                        popUpTo(Rotas.LOGIN) { inclusive = true } // Limpa a pilha para não voltar ao login
                    }
                }
            )
        }

        // 3. ROTAS FUTURAS (Espaços reservados para os próximos ecrãs)
        composable(Rotas.TRILHA_ALUNO) {
                // Chamamos o ecrã real da Trilha
                TrilhaScreen()
        }


        composable(Rotas.PAINEL_EDUCADOR) {
            // TODO: Substituir pelo ecrã real do Educador
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bem-vindo ao Painel, Educador!")
            }
        }
    }
}