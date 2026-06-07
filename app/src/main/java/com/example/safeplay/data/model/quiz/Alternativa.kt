package com.example.safeplay.data.model.quiz

import kotlinx.serialization.Serializable

@Serializable
data class Alternativa(
    val id: String, // Ajuste para Int se o ID no seu banco for numérico
    val id_desafio: String,
    val texto_opcao: String,
    val is_correta: Boolean,
    val justificativa: String? = null // Mapeia a sua "Justificativa Pedagógica" (opcional)
)