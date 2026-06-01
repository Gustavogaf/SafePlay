package com.example.safeplay.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeplay.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Injetamos o Repositório no construtor do ViewModel
class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun fazerCadastro(nome: String, email: String, senha: String, isAluno: Boolean) {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            _authState.value = AuthState.Error("Por favor, preencha todos os campos.")
            return
        }
        if (senha.length < 6) {
            _authState.value = AuthState.Error("A senha deve ter pelo menos 6 caracteres.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val papel = if (isAluno) "aluno" else "educador"

            // O ViewModel apenas pede ao Repositório para executar a ação
            val resultado = repository.cadastrarUsuario(nome, email, senha, papel)

            // O comando "fold" avalia o resultado e divide entre sucesso e falha
            resultado.fold(
                onSuccess = { roleRetornada ->
                    _authState.value = AuthState.Success(roleRetornada)
                },
                onFailure = { erro ->
                    _authState.value = AuthState.Error(erro.message ?: "Ocorreu um erro ao criar a conta.")
                }
            )
        }
    }

    fun fazerLogin(email: String, senha: String, isAluno: Boolean) {
        if (email.isBlank() || senha.isBlank()) {
            _authState.value = AuthState.Error("Por favor, preencha todos os campos.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val papel = if (isAluno) "aluno" else "educador"
            val resultado = repository.fazerLogin(email, senha, papel)

            resultado.fold(
                onSuccess = { roleRetornada ->
                    _authState.value = AuthState.Success(roleRetornada)
                },
                onFailure = { erro ->
                    _authState.value = AuthState.Error(erro.message ?: "Ocorreu um erro ao fazer login")
                }
            )
        }
    }

    fun resetError() {
        _authState.value = AuthState.Idle
    }
}