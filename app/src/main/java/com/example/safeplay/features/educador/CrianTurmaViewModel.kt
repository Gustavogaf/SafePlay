package com.example.safeplay.features.educador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeplay.data.repository.TurmaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PainelEducadorState {
    object Idle : PainelEducadorState()
    object Loading : PainelEducadorState()
    data class Success(val codigoGerado: String) : PainelEducadorState()
    data class Error(val message: String) : PainelEducadorState()
}

class CrianTurmaViewModel(
    private val repository: TurmaRepository = TurmaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PainelEducadorState>(PainelEducadorState.Idle)
    val uiState: StateFlow<PainelEducadorState> = _uiState.asStateFlow()

    fun criarNovaTurma(nomeTurma: String) {
        if (nomeTurma.isBlank()) {
            _uiState.value = PainelEducadorState.Error("O nome da turma não pode estar vazio.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PainelEducadorState.Loading

            val resultado = repository.criarTurma(nomeTurma)

            resultado.fold(
                onSuccess = { codigo ->
                    _uiState.value = PainelEducadorState.Success(codigo)
                },
                onFailure = { erro ->
                    _uiState.value = PainelEducadorState.Error(erro.message ?: "Erro desconhecido.")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = PainelEducadorState.Idle
    }
}