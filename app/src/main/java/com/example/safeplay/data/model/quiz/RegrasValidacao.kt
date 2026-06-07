package com.example.safeplay.data.model.quiz

import kotlinx.serialization.Serializable

// Esta classe representa o objeto JSON inteiro
@Serializable
data class RegrasValidacao(
    val zonas_alvo: List<String> = emptyList(),
    val itens_interativos: List<ItemInterativo> = emptyList()
)

// Esta classe representa cada cardzinho dentro do array "itens_interativos"
@Serializable
data class ItemInterativo(
    val id: String,
    val texto: String,
    val alvo_correto: String? = null // Pode ser nulo para os "distratores" (opções incorretas)
)