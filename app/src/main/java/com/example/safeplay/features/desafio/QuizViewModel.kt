package com.example.safeplay.features.desafio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeplay.data.model.quiz.Alternativa
import com.example.safeplay.data.model.quiz.Desafio
import com.example.safeplay.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado completo da tela de Quiz
sealed class QuizState {
    object Loading : QuizState()
    data class Error(val message: String) : QuizState()

    // O estado ativo do jogo
    data class Playing(
        val desafioAtual: Desafio,
        val alternativas: List<Alternativa> = emptyList(), // Só preenchido se for múltipla escolha
        val indiceAtual: Int,
        val totalDesafios: Int,
        val pontuacaoAcumulada: Int,
        val mostrarDicaErro: Boolean = false // Controla se a dica pedagógica deve aparecer
    ) : QuizState()

    // Estado final quando acaba o módulo
    data class Finished(val pontuacaoFinal: Int) : QuizState()
}

class QuizViewModel(
    private val repository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizState>(QuizState.Loading)
    val uiState: StateFlow<QuizState> = _uiState.asStateFlow()

    // Variáveis internas para controlar o fluxo
    private var listaDesafios: List<Desafio> = emptyList()
    private var indiceAtual = 0
    private var pontuacao = 0

    /**
     * Inicia o jogo carregando todos os desafios daquele módulo.
     */
    fun iniciarQuiz(idModulo: String) {
        viewModelScope.launch {
            _uiState.value = QuizState.Loading

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

                    carregarDesafioAtual()
                },
                onFailure = { erro ->
                    _uiState.value = QuizState.Error(erro.message ?: "Falha ao carregar desafios.")
                }
            )
        }
    }

    /**
     * Prepara a tela para o desafio atual. Se for múltipla escolha, busca as alternativas.
     */
    private suspend fun carregarDesafioAtual() {
        if (indiceAtual >= listaDesafios.size) {
            // Se já passámos de todos os desafios, o módulo terminou!
            _uiState.value = QuizState.Finished(pontuacao)
            return
        }

        val desafio = listaDesafios[indiceAtual]

        if (desafio.tipo_dinamica == "quiz_multipla_escolha") {
            // Busca as alternativas específicas deste desafio
            val resultadoAlternativas = repository.buscarAlternativasPorDesafio(desafio.id)

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
            // Se for Drag & Drop, os dados já vieram no JSONB (regras_validacao), não precisa de nova query
            _uiState.value = QuizState.Playing(
                desafioAtual = desafio,
                indiceAtual = indiceAtual + 1,
                totalDesafios = listaDesafios.size,
                pontuacaoAcumulada = pontuacao
            )
        }
    }

    /**
     * Recebe o mapa do aluno (id_item -> nome_da_zona) e cruza com o JSONB do Supabase.
     */
    fun validarRespostaDragAndDrop(alocacaoDoAluno: Map<String, String?>) {
        val currentState = _uiState.value
        if (currentState !is QuizState.Playing) return

        val regras = currentState.desafioAtual.regras_validacao
        if (regras == null) {
            _uiState.value = QuizState.Error("Erro: Desafio não possui regras de validação.")
            return
        }

        var todasCorretas = true

        // Percorre todos os itens interativos do desafio atual
        for (item in regras.itens_interativos) {
            val zonaEscolhidaPeloAluno = alocacaoDoAluno[item.id]

            if (item.alvo_correto != null) {
                // É um item válido: o aluno TINHA que colocá-lo na zona certa
                if (zonaEscolhidaPeloAluno != item.alvo_correto) {
                    todasCorretas = false
                    break // Se errou um, já perde a jogada
                }
            } else {
                // É um distrator (ex: "App Pirata"): não deve ser colocado em zona nenhuma!
                if (zonaEscolhidaPeloAluno != null) {
                    todasCorretas = false
                    break
                }
            }
        }

        // Chama a função existente que soma os pontos ou exibe o pop-up de erro
        processarResposta(isCorreta = todasCorretas)
    }
    /**
     * Chamado pela UI quando o aluno valida uma resposta.
     */
    fun processarResposta(isCorreta: Boolean) {
        val currentState = _uiState.value
        if (currentState !is QuizState.Playing) return

        if (isCorreta) {
            // Acertou! Soma os pontos e avança para o próximo.
            pontuacao += currentState.desafioAtual.pontos_valendo
            indiceAtual++

            viewModelScope.launch {
                carregarDesafioAtual()
            }
        } else {
            // Errou! Exibe a dica pedagógica.
            _uiState.value = currentState.copy(mostrarDicaErro = true)

            // Aqui você pode decidir se quer descontar pontos ou vidas no futuro
        }
    }

    /**
     * Chamado pela UI para esconder a dica de erro e tentar novamente.
     */
    fun tentarNovamente() {
        val currentState = _uiState.value
        if (currentState is QuizState.Playing) {
            _uiState.value = currentState.copy(mostrarDicaErro = false)
        }
    }
}