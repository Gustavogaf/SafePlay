package com.example.safeplay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MembroTurma(
    // Não enviamos o id_membro porque o banco de dados (Supabase) gera-o automaticamente
    val id_turma: String, // Ajuste para Int se a chave primária da turma for numérica
    val id_aluno: String
)