package com.example.safeplay.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safeplay.features.auth.AuthScreen
import com.example.safeplay.features.auth.RegisterScreen
import com.example.safeplay.features.educador.DashboardEducadorScreen
import com.example.safeplay.features.educador.CriarTurmaScreen
import com.example.safeplay.features.trilha.TrilhaScreen
import com.example.safeplay.features.turma.EntrarTurmaScreen


object Rotas {
    const val LOGIN = "login"
    const val REGISTO = "registo"
    const val ENTRAR_TURMA = "entrar_turma"
    const val TRILHA_ALUNO = "trilha_aluno"
    const val DASHBOARD_EDUCADOR = "dashboard_educador"
    const val CRIAR_TURMA = "criar_turma"
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
                    val rotaDestino = if (papel == "aluno") Rotas.ENTRAR_TURMA else Rotas.DASHBOARD_EDUCADOR

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
                    val rotaDestino = if (papel == "aluno") Rotas.ENTRAR_TURMA else Rotas.DASHBOARD_EDUCADOR

                    navController.navigate(rotaDestino) {
                        popUpTo(Rotas.LOGIN) { inclusive = true } // Limpa a pilha para não voltar ao login
                    }
                }
            )
        }

        // 3. ROTA DE VÍNCULO DA TURMA
        composable(Rotas.ENTRAR_TURMA) {
            EntrarTurmaScreen(
                onTurmaVinculada = {
                    navController.navigate(Rotas.TRILHA_ALUNO) {
                        popUpTo(Rotas.ENTRAR_TURMA) { inclusive = true } // Impede de voltar ao ecrã de código com o botão de voltar do telemóvel
                    }
                }
            )
        }

        // 3. ROTAS FUTURAS (Espaços reservados para os próximos ecrãs)
        composable(Rotas.TRILHA_ALUNO) {
                // Chamamos o ecrã real da Trilha
                TrilhaScreen()
        }


        composable(Rotas.DASHBOARD_EDUCADOR) {
            DashboardEducadorScreen(
                onNovaTurmaClick = {
                    navController.navigate(Rotas.CRIAR_TURMA)
                }
            )
        }

        // TELA DE CRIAÇÃO (A antiga PainelEducadorScreen)
        composable(Rotas.CRIAR_TURMA) {
            CriarTurmaScreen() // Lembre-se de renomear a função antiga para este nome
        }

    }
}