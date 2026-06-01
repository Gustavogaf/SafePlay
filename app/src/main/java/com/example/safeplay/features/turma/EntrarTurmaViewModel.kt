package com.example.safeplay.features.turma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeplay.data.repository.TurmaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EntrarTurmaState {
    object Idle : EntrarTurmaState()
    object Loading : EntrarTurmaState()
    object Success : EntrarTurmaState()
    data class Error(val message: String) : EntrarTurmaState()
}

class EntrarTurmaViewModel(
    private val repository: TurmaRepository = TurmaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EntrarTurmaState>(EntrarTurmaState.Idle)
    val uiState: StateFlow<EntrarTurmaState> = _uiState.asStateFlow()

    fun validarCodigoEEntrar(codigo: String) {
        if (codigo.length != 6) {
            _uiState.value = EntrarTurmaState.Error("O código deve ter 6 caracteres.")
            return
        }

        viewModelScope.launch {
            _uiState.value = EntrarTurmaState.Loading

            val resultado = repository.entrarNaTurma(codigo)

            resultado.fold(
                onSuccess = {
                    _uiState.value = EntrarTurmaState.Success
                },
                onFailure = { erro ->
                    _uiState.value = EntrarTurmaState.Error(erro.message ?: "Erro desconhecido.")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = EntrarTurmaState.Idle
    }
}