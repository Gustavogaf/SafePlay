package com.example.safeplay.data.repository

import com.example.safeplay.data.model.MembroTurma
import com.example.safeplay.data.model.NovaTurma
import com.example.safeplay.data.model.Turma
import com.example.safeplay.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest

class TurmaRepository {

    suspend fun entrarNaTurma(codigoAcesso: String): Result<Unit> {
        return try {
            // 1. Pega o ID do aluno que está a tentar entrar
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                ?: throw Exception("Sessão expirada. Faça login novamente.")

            // 2. Busca a turma que tem este código exato
            val turmas = SupabaseClient.client.postgrest["turma"]
                .select {
                    filter { eq("codigo_acesso", codigoAcesso) }
                }.decodeList<Turma>()

            if (turmas.isEmpty()) {
                throw Exception("Código inválido. Nenhuma turma encontrada.")
            }

            val turmaEncontrada = turmas.first()

            // 3. Monta o objeto para inserir na tabela membro_turma
            val novoMembro = MembroTurma(
                id_turma = turmaEncontrada.id_turma,
                id_aluno = userId
            )

            // 4. Salva o vínculo no banco de dados
            SupabaseClient.client.postgrest["membros_turma"].insert(novoMembro)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ocorreu um erro ao entrar na turma."))
        }
    }

    suspend fun criarTurma(nomeTurma: String): Result<String> {
        return try {
            // 1. Pega o ID do educador logado
            val educadorId = SupabaseClient.client.auth.currentUserOrNull()?.id
                ?: throw Exception("Sessão expirada. Faça login novamente.")

            // 2. Gera um código alfanumérico aleatório de 6 caracteres
            val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val codigoGerado = (1..6)
                .map { caracteres.random() }
                .joinToString("")

            // 3. Monta o objeto com o novo campo nome_turma
            val novaTurma = NovaTurma(
                nome_turma = nomeTurma,
                codigo_acesso = codigoGerado,
                id_educador = educadorId
            )

            // 4. Salva no banco de dados
            SupabaseClient.client.postgrest["turma"].insert(novaTurma)

            // Retorna o código gerado para a interface mostrar ao professor
            Result.success(codigoGerado)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao criar a turma."))
        }
    }

    suspend fun listarTurmasDoEducador(): Result<List<Turma>> {
        return try {
            val educadorId = SupabaseClient.client.auth.currentUserOrNull()?.id
                ?: throw Exception("Sessão expirada. Faça login novamente.")

            // Busca apenas as turmas onde o id_educador é igual ao do usuário logado
            val turmas = SupabaseClient.client.postgrest["turma"]
                .select { filter { eq("id_educador", educadorId) } }
                .decodeList<Turma>()

            Result.success(turmas)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao buscar turmas."))
        }
    }

    suspend fun contarAlunosDaTurma(idTurma: String): Int {
        return try {
            // Conta quantas vezes este id_turma aparece na tabela de vínculos
            val membros = SupabaseClient.client.postgrest["membros_turma"]
                .select { filter { eq("id_turma", idTurma) } }
                .decodeList<MembroTurma>()

            membros.size
        } catch (e: Exception) {
            println("SAFEPLAY_ERRO: Falha ao contar alunos -> ${e.message}")
            0
        }
    }
}