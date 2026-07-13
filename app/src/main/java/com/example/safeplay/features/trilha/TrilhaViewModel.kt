package com.example.safeplay.features.trilha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeplay.data.model.Modulos
import com.example.safeplay.data.model.ModuloComProgresso
import com.example.safeplay.data.model.ProgressoAluno
import com.example.safeplay.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TrilhaState {
    object Loading : TrilhaState()
    // Adicionado os parâmetros de pontuação e nível no estado de sucesso
    data class Success(
        val modulos: List<ModuloComProgresso>,
        val pontuacaoTotal: Int,
        val nivelAtual: Int
    ) : TrilhaState()
    data class Error(val message: String) : TrilhaState()
}

class TrilhaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TrilhaState>(TrilhaState.Loading)
    val uiState: StateFlow<TrilhaState> = _uiState.asStateFlow()

    init {
        atualizarTrilha()
    }

    // Transformado em método público para ser chamado sempre que a tela ganhar foco
    fun atualizarTrilha() {
        viewModelScope.launch {
            try {
                // 1. Pega o ID do aluno que está logado
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Utilizador não autenticado.")

                // 2. Busca todos os módulos ordenados
                val modulos = SupabaseClient.client.postgrest["modulos"]
                    .select { order("ordem_trilha", order = Order.ASCENDING) }
                    .decodeList<Modulos>()

                // 3. Busca o progresso DESTE aluno
                val progressos = SupabaseClient.client.postgrest["progresso_aluno"]
                    .select { filter { eq("id_aluno", userId) } }
                    .decodeList<ProgressoAluno>()

                // 4. Lógica de Gamificação: Calcular Pontuação e Nível
                // Soma todos os pontos obtidos até o momento (tratando null como 0)
                val pontuacaoAcumulada = progressos.sumOf { it.pontuacao_obtida ?: 0 }

                // O nível do aluno será equivalente à quantidade de módulos concluídos + 1
                val modulosConcluidos = progressos.count { it.status == "Concluído" }
                val nivelDoAluno = modulosConcluidos + 1

                // 5. Junta as informações
                val trilhaDoAluno = modulos.map { modulo ->
                    val progressoDesteModulo = progressos.find { it.id_modulo == modulo.id_modulo }

                    ModuloComProgresso(
                        id_modulo = modulo.id_modulo,
                        titulo = modulo.titulo,
                        ordem_trilha = modulo.ordem_trilha,
                        status = progressoDesteModulo?.status ?: "Bloqueado"
                    )
                }

                _uiState.value = TrilhaState.Success(
                    modulos = trilhaDoAluno,
                    pontuacaoTotal = pontuacaoAcumulada,
                    nivelAtual = nivelDoAluno
                )

            } catch (e: Exception) {
                _uiState.value = TrilhaState.Error(e.message ?: "Erro desconhecido ao carregar trilha")
            }
        }
    }
}