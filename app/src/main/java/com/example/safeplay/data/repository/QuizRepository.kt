package com.example.safeplay.data.repository

import com.example.safeplay.data.model.quiz.Alternativa
import com.example.safeplay.data.model.quiz.Desafio
import com.example.safeplay.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class QuizRepository {

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
}