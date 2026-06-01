package com.example.safeplay.features.educador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeplay.data.repository.TurmaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstatisticasDashboard(
    val totalAlunos: Int,
    val crescimentoSemana: Int,
    val atividadesPendentes: Int,
    val engajamentoMedio: Float
)

data class TurmaResumo(
    val id: String,
    val nome_turma: String,
    val codigo: String,
    val alunosAtivos: Int,
    val moduloAtual: String,
    val corDestaque: Long
)

// Usamos um estado selado para controlar a tela de carregamento (bolinha a girar)
sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val estatisticas: EstatisticasDashboard, val turmas: List<TurmaResumo>) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardEducadorViewModel(
    private val repository: TurmaRepository = TurmaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        carregarDashboard()
    }

    fun carregarDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardState.Loading

            val resultado = repository.listarTurmasDoEducador()

            resultado.fold(
                onSuccess = { turmasDoBanco ->
                    var totalAlunosGeral = 0
                    val turmasResumo = mutableListOf<TurmaResumo>()

                    // Paleta de cores para alternar o destaque lateral das turmas
                    val cores = listOf(0xFF0056D2, 0xFF6200EA, 0xFFD84315, 0xFF009688)

                    // Para cada turma real, vamos contar os alunos
                    turmasDoBanco.forEachIndexed { index, turma ->
                        val totalAlunos = repository.contarAlunosDaTurma(turma.id_turma)
                        totalAlunosGeral += totalAlunos

                        turmasResumo.add(
                            TurmaResumo(
                                id = turma.id_turma,
                                nome_turma = turma.nome_turma ?: "Turma sem nome",
                                codigo = turma.codigo_acesso,
                                alunosAtivos = totalAlunos,
                                moduloAtual = "Módulo 1", // Fixo até criarmos os quizzes
                                corDestaque = cores[index % cores.size] // Alterna as cores
                            )
                        )
                    }

                    // Monta os dados finais para a tela
                    val stats = EstatisticasDashboard(
                        totalAlunos = totalAlunosGeral,
                        crescimentoSemana = 0, // Fixo por enquanto
                        atividadesPendentes = 0, // Fixo por enquanto
                        engajamentoMedio = 0.0f // Fixo por enquanto
                    )

                    _uiState.value = DashboardState.Success(stats, turmasResumo)
                },
                onFailure = { erro ->
                    _uiState.value = DashboardState.Error(erro.message ?: "Erro desconhecido")
                }
            )
        }
    }
}