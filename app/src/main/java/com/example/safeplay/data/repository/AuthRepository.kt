package com.example.safeplay.data.repository

import com.example.safeplay.data.model.Modulos
import com.example.safeplay.data.model.Perfil
import com.example.safeplay.data.model.ProgressoAluno
import com.example.safeplay.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository {

    // Retornamos um Result<> nativo do Kotlin para indicar Sucesso ou Falha de forma limpa
    suspend fun cadastrarUsuario(nome: String, email: String, senha: String, papel: String): Result<String> {
        return try {
            // 1. Cria a conta no Supabase Auth
            SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = senha
            }

            // 2. Recupera o ID gerado
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                ?: throw Exception("Erro: ID de utilizador não encontrado após registo.")

            // 3. Monta o Perfil e insere no banco PostgreSQL
            val novoPerfil = Perfil(
                id_perfil = userId, // Atenção: Usando id_perfil conforme corrigimos antes
                nome = nome,
                role = papel
            )
            SupabaseClient.client.postgrest["perfis"].insert(novoPerfil)

            // 4. NOVA LÓGICA: Se for aluno, inicializa a trilha dinamicamente
            if (papel == "aluno") {
                // Busca todos os módulos (fases) existentes ordenados pela trilha
                val modulos = SupabaseClient.client.postgrest["modulos"]
                    .select()
                    .decodeList<Modulos>()

                // Cria a lista de progressos em lote
                if (modulos.isNotEmpty()) {
                    val progressosIniciais = modulos.map { modulo ->
                        ProgressoAluno(
                            id_aluno = userId,
                            id_modulo = modulo.id_modulo,
                            status = if (modulo.ordem_trilha == 1) "Em Andamento" else "Bloqueado"
                        )
                    }

                    // Insere todos de uma vez na tabela progresso_aluno
                    SupabaseClient.client.postgrest["progresso_aluno"].insert(progressosIniciais)
                }
            }

            // Retorna sucesso com o papel escolhido
            Result.success(papel)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fazerLogin(email: String, senha: String, papelSelecionado: String): Result<String> {
        return try {
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = senha
            }

            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                ?: throw Exception("Sessão não iniciada corretamente.")

            // 3. Vai à tabela 'perfis' buscar a identidade real usando o ID
            val perfilReal = SupabaseClient.client.postgrest["perfis"]
                .select {
                    filter {
                        // Atenção: use o nome exato da sua coluna aqui (id_perfil)
                        eq("id_perfil", userId)
                    }
                }.decodeSingle<Perfil>() // Converte a linha do banco para a nossa Data Class

            // 4. A Grande Validação
            if (perfilReal.role != papelSelecionado) {
                // Se a pessoa clicou no botão errado, fazemos LogOut imediatamente por segurança
                SupabaseClient.client.auth.signOut()

                // Dispara o erro que vai aparecer em vermelho no ecrã
                val nomePapel = if (papelSelecionado == "aluno") "Aluno" else "Educador"
                throw Exception("Esta conta não pertence a um $nomePapel.")
            }

            // 5. Se chegou aqui, a senha está certa e o botão escolhido também!
            Result.success(perfilReal.role)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ocorreu um erro ao fazer login."))
        }
    }
}