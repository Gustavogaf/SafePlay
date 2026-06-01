package com.example.safeplay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Turma(
    val id_turma: String, // Ajuste para Int se o seu banco usar numeração sequencial em vez de UUID
    val nome_turma: String? = null,
    val codigo_acesso: String,
    val id_educador: String? = null
)
@Serializable
data class NovaTurma(
    val nome_turma: String,
    val codigo_acesso: String,
    val id_educador: String
)