package com.example.safeplay.data.model.quiz

import kotlinx.serialization.Serializable

@Serializable
data class Desafio(
    val id: String, // Ajuste para Int se o ID no seu banco for numérico
    val id_modulo: String,
    val contexto: String,
    val pergunta: String,
    val dica_erro: String,
    val pontos_valendo: Int,
    val tipo_dinamica: String, // Receberá "drag_and_drop" ou "quiz_multipla_escolha"

    // O pulo do gato: mapeamos a coluna JSONB diretamente para a nossa classe!
    // Se no banco o campo estiver vazio (em múltiplas escolhas), o Kotlin assume null com segurança.
    val regras_validacao: RegrasValidacao? = null
)