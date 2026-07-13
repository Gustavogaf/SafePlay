package com.example.safeplay.data.repository

import com.example.safeplay.data.model.quiz.Alternativa
import com.example.safeplay.data.model.quiz.Desafio
import com.example.safeplay.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import com.example.safeplay.data.model.Modulos
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

class QuizRepository {
    @Serializable
    data class ProgressoUpdate(
        val status: String? = null,
        val pontuacao_obtida: Int? = null,
        val tentativas: Int? = null
    )
    /**
     * Procura todos os desafios vinculados a um módulo específico.
     * Retorna a lista de desafios (tanto drag_and_drop como quiz_multipla_escolha).
     */
    suspend fun buscarDesafiosPorModulo(idModulo: String): Result<List<Desafio>> {
        return try {
            // Busca os desafios filtrando pelo ID do módulo correspondente
            val desafios = SupabaseClient.client.postgrest["desafios"]
                .select {
                    filter { eq("id_modulo", idModulo) }
                }.decodeList<Desafio>()

            Result.success(desafios)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao carregar os desafios do módulo no Supabase."))
        }
    }

    /**
     * Procura todas as alternativas de múltipla escolha vinculadas a um desafio específico.
     * Será utilizada sempre que o 'tipo_dinamica' do desafio for 'quiz_multipla_escolha'.
     */
    suspend fun buscarAlternativasPorDesafio(idDesafio: String): Result<List<Alternativa>> {
        return try {
            val alternativas = SupabaseClient.client.postgrest["alternativas"]
                .select {
                    filter { eq("id_desafio", idDesafio) }
                }.decodeList<Alternativa>()

            Result.success(alternativas)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao carregar as alternativas do desafio."))
        }
    }

    /**
     * Guarda o resultado de um desafio específico para o aluno.
     */
    suspend fun salvarTentativaDesafio(idDesafio: String, idAluno: String, pontuacaoObtida: Int, tentativas: Int): Result<Unit> {
        return try {
            val tentativa = mapOf(
                "id_desafio" to idDesafio,
                "id_aluno" to idAluno,
                "pontuacao_obtida" to pontuacaoObtida,
                "tentativas" to tentativas
            )

            // Faz o INSERT na tabela de tentativas
            com.example.safeplay.data.network.SupabaseClient.client.postgrest["tentativas_aluno"].insert(tentativa)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Adicione dentro da classe QuizRepository
    suspend fun finalizarModuloEDesbloquearProximo(
        idModuloAtual: String,
        idAluno: String,
        pontuacaoFinal: Int,
        totalTentativas: Int
    ): Result<Pair<String, String?>> { // Retorna o Título e a URL da Medalha
        return try {
            // 1. Atualiza o progresso do módulo atual para "Concluído"
            SupabaseClient.client.postgrest["progresso_aluno"].update(
                ProgressoUpdate(
                    status = "Concluído",
                    pontuacao_obtida = pontuacaoFinal,
                    tentativas = totalTentativas
                )
            ) {
                filter {
                    eq("id_aluno", idAluno)
                    eq("id_modulo", idModuloAtual)
                }
            }

            // 2. Busca as informações de exibição do módulo que acabou de ser feito
            val respostaModulo = SupabaseClient.client.postgrest["modulos"]
                .select(io.github.jan.supabase.postgrest.query.Columns.list("id_modulo", "ordem_trilha", "titulo", "icone_medalha_url")) {
                    filter { eq("id_modulo", idModuloAtual) }
                }.decodeSingle<Map<String, kotlinx.serialization.json.JsonElement>>()

            val ordemTrilha = respostaModulo["ordem_trilha"]?.toString()?.toIntOrNull() ?: 1
            val titulo = respostaModulo["titulo"]?.toString()?.replace("\"", "") ?: "Módulo Concluído"
            val iconeMedalhaUrl = respostaModulo["icone_medalha_url"]?.toString()?.replace("\"", "")

            // 3. Procura se existe um próximo módulo (ordem atual + 1)
            val proximaOrdem = ordemTrilha + 1
            val respostaProximo = SupabaseClient.client.postgrest["modulos"]
                .select(io.github.jan.supabase.postgrest.query.Columns.list("id_modulo", "ordem_trilha")) {
                    filter { eq("ordem_trilha", proximaOrdem) }
                }.decodeSingleOrNull<Map<String, kotlinx.serialization.json.JsonElement>>()

            // 4. Se houver um próximo módulo na fila, altera o status dele para "Em Andamento"
            if (respostaProximo != null) {
                val idProximoModulo = respostaProximo["id_modulo"]?.toString()?.replace("\"", "")
                if (idProximoModulo != null) {
                    SupabaseClient.client.postgrest["progresso_aluno"].update(
                        ProgressoUpdate(status = "Em Andamento")
                    ) {
                        filter {
                            eq("id_aluno", idAluno)
                            eq("id_modulo", idProximoModulo)
                        }
                    }
                }
            }

            Result.success(Pair(titulo, iconeMedalhaUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}