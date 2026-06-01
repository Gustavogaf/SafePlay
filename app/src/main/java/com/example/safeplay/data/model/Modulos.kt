package com.example.safeplay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Modulos(
    val id_modulo: String,
    val titulo: String,
    val ordem_trilha: Int
    // No futuro, podemos adicionar aqui: val icone: String, val cor: String
)

// Uma classe simples para juntar o módulo com o status do aluno (não precisa de @Serializable)
data class ModuloComProgresso(
    val id_modulo: String,
    val titulo: String,
    val ordem_trilha: Int,
    val status: String // "Em Andamento", "Concluído" ou "Bloqueado"
)