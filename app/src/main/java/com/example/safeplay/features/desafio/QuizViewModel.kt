package com.example.safeplay.features.desafio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeplay.data.model.quiz.Alternativa
import com.example.safeplay.data.model.quiz.Desafio
import com.example.safeplay.data.repository.QuizRepository
import com.example.safeplay.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuizState {
    object Loading : QuizState()
    data class Error(val message: String) : QuizState()
    data class Playing(
        val desafioAtual: Desafio,
        val alternativas: List<Alternativa> = emptyList(),
        val indiceAtual: Int,
        val totalDesafios: Int,
        val pontuacaoAcumulada: Int,
        val mostrarDicaErro: Boolean = false
    ) : QuizState()
    data class Finished(
        val pontuacaoFinal: Int,
        val tituloModulo: String,
        val iconeMedalhaUrl: String?
    ) : QuizState()
}

class QuizViewModel(
    private val repository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizState>(QuizState.Loading)
    val uiState: StateFlow<QuizState> = _uiState.asStateFlow()

    private var listaDesafios: List<Desafio> = emptyList()
    private var idModuloAtual: String = ""
    private var indiceAtual = 0
    private var pontuacao = 0
    private var errosDesafioAtual = 0
    private var totalTentativasModulo = 0 // Controla o acumulado de submissões do módulo

    fun iniciarQuiz(idModulo: String) {
        viewModelScope.launch {
            _uiState.value = QuizState.Loading
            idModuloAtual = idModulo

            val resultado = repository.buscarDesafiosPorModulo(idModulo)

            resultado.fold(
                onSuccess = { desafios ->
                    if (desafios.isEmpty()) {
                        _uiState.value = QuizState.Error("Nenhum desafio encontrado para este módulo.")
                        return@launch
                    }

                    listaDesafios = desafios
                    indiceAtual = 0
                    pontuacao = 0
                    errosDesafioAtual = 0
                    totalTentativasModulo = 0

                    carregarDesafioAtual()
                },
                onFailure = { erro ->
                    _uiState.value = QuizState.Error(erro.message ?: "Falha ao carregar desafios.")
                }
            )
        }
    }

    private suspend fun carregarDesafioAtual() {
        if (indiceAtual >= listaDesafios.size) {
            // O módulo acabou! Gravamos os dados consolidados no Supabase antes de ir para a tela final
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Utilizador não autenticado.")

                val resultadoPersistencia = repository.finalizarModuloEDesbloquearProximo(
                    idModuloAtual = idModuloAtual,
                    idAluno = userId,
                    pontuacaoFinal = pontuacao,
                    totalTentativas = totalTentativasModulo
                )

                repository.finalizarModuloEDesbloquearProximo(
                    idModuloAtual = idModuloAtual,
                    idAluno = userId,
                    pontuacaoFinal = pontuacao,
                    totalTentativas = totalTentativasModulo
                ).fold(
                    onSuccess = { dadosModulo ->
                        // Passa os dados obtidos do banco diretamente para o estado final da View
                        _uiState.value = QuizState.Finished(
                            pontuacaoFinal = pontuacao,
                            tituloModulo = dadosModulo.first,
                            iconeMedalhaUrl = dadosModulo.second
                        )
                    },
                    onFailure = { erro ->
                        _uiState.value = QuizState.Error("Erro ao salvar progresso no banco: ${erro.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = QuizState.Error(e.message ?: "Erro ao processar finalização do módulo.")
            }
            return
        }

        val desafio = listaDesafios[indiceAtual]

        if (desafio.tipo_dinamica == "quiz_multipla_escolha") {
            val resultadoAlternativas = repository.buscarAlternativasPorDesafio(desafio.id_desafio)

            resultadoAlternativas.fold(
                onSuccess = { alternativas ->
                    _uiState.value = QuizState.Playing(
                        desafioAtual = desafio,
                        alternativas = alternativas,
                        indiceAtual = indiceAtual + 1,
                        totalDesafios = listaDesafios.size,
                        pontuacaoAcumulada = pontuacao
                    )
                },
                onFailure = { erro ->
                    _uiState.value = QuizState.Error("Erro ao carregar alternativas: ${erro.message}")
                }
            )
        } else {
            _uiState.value = QuizState.Playing(
                desafioAtual = desafio,
                indiceAtual = indiceAtual + 1,
                totalDesafios = listaDesafios.size,
                pontuacaoAcumulada = pontuacao
            )
        }
    }

    fun validarRespostaDragAndDrop(alocacaoDoAluno: Map<String, String?>) {
        val currentState = _uiState.value
        if (currentState !is QuizState.Playing) return

        val regras = currentState.desafioAtual.regras_validacao
        if (regras == null) {
            _uiState.value = QuizState.Error("Erro: Desafio não possui regras de validação.")
            return
        }

        var todasCorretas = true
        for (item in regras.itens_interativos) {
            val zonaEscolhidaPeloAluno = alocacaoDoAluno[item.id]
            if (item.alvo_correto != null) {
                if (zonaEscolhidaPeloAluno != item.alvo_correto) {
                    todasCorretas = false
                    break
                }
            } else {
                if (zonaEscolhidaPeloAluno != null) {
                    todasCorretas = false
                    break
                }
            }
        }

        processarResposta(isCorreta = todasCorretas)
    }

    fun validarRespostaMultiplaEscolha(idAlternativa: String) {
        val currentState = _uiState.value
        if (currentState !is QuizState.Playing) return

        val alternativaSelecionada = currentState.alternativas.find { it.id_alternativa == idAlternativa }

        if (alternativaSelecionada != null) {
            processarResposta(isCorreta = alternativaSelecionada.is_correta)
        } else {
            _uiState.value = QuizState.Error("Erro: Alternativa não encontrada.")
        }
    }

    fun processarResposta(isCorreta: Boolean) {
        val currentState = _uiState.value
        if (currentState !is QuizState.Playing) return

        totalTentativasModulo++ // Conta a tentativa global do módulo

        if (isCorreta) {
            val limiteErros = minOf(errosDesafioAtual, 2)
            val percentualDesconto = limiteErros * 0.05

            val pontosBase = currentState.desafioAtual.pontos_valendo
            val pontosDescontados = (pontosBase * percentualDesconto).toInt()
            val pontosFinais = pontosBase - pontosDescontados

            pontuacao += pontosFinais
            indiceAtual++
            errosDesafioAtual = 0

            viewModelScope.launch {
                carregarDesafioAtual()
            }
        } else {
            errosDesafioAtual++
            _uiState.value = currentState.copy(mostrarDicaErro = true)
        }
    }

    fun tentarNovamente() {
        val currentState = _uiState.value
        if (currentState is QuizState.Playing) {
            _uiState.value = currentState.copy(mostrarDicaErro = false)
        }
    }
}