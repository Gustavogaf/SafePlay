package com.example.safeplay.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safeplay.data.model.MembroTurma
import com.example.safeplay.data.network.SupabaseClient
import com.example.safeplay.data.repository.AuthRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    // O Success agora envia a rota exata de destino para a UI
    data class Success(val destino: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

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
            val resultado = repository.cadastrarUsuario(nome, email, senha, papel)

            resultado.fold(
                onSuccess = { roleRetornada ->
                    // Ao cadastrar, o aluno obrigatoriamente ainda não tem turma
                    val destino = if (roleRetornada == "aluno") "entrar_turma" else "dashboard_educador"
                    _authState.value = AuthState.Success(destino)
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
                    if (roleRetornada == "aluno") {
                        try {
                            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                            if (userId != null) {
                                // Verifica na base de dados se o aluno já se vinculou a alguma turma
                                val turmasVinculadas = SupabaseClient.client.postgrest["membros_turma"]
                                    .select { filter { eq("id_aluno", userId) } }
                                    .decodeList<MembroTurma>()

                                if (turmasVinculadas.isNotEmpty()) {
                                    // Se já estiver numa turma, pula direto para o mapa
                                    _authState.value = AuthState.Success("trilha")
                                } else {
                                    // Se não estiver, pede o código da turma
                                    _authState.value = AuthState.Success("entrar_turma")
                                }
                            } else {
                                _authState.value = AuthState.Error("Erro ao recuperar a sessão do aluno.")
                            }
                        } catch (e: Exception) {
                            _authState.value = AuthState.Error("Erro ao verificar o vínculo com a turma.")
                        }
                    } else {
                        // Se for educador, o fluxo continua o mesmo
                        _authState.value = AuthState.Success("dashboard_educador")
                    }
                },
                onFailure = { erro ->
                    // Tratamento amigável de erros do Supabase
                    val mensagemErro = erro.message ?: ""
                    val mensagemAmigavel = when {
                        mensagemErro.contains("Invalid login credentials") -> "Email ou senha incorretos. Tente novamente."
                        mensagemErro.contains("Email not confirmed") -> "Por favor, confirme o seu email antes de entrar."
                        mensagemErro.contains("Unable to resolve host") || mensagemErro.contains("Network") -> "Sem conexão à internet. Verifique o seu Wi-Fi."
                        else -> "Ocorreu um erro ao aceder. Tente novamente mais tarde."
                    }
                    _authState.value = AuthState.Error(mensagemAmigavel)
                }
            )
        }
    }

    fun resetError() {
        _authState.value = AuthState.Idle
    }
}