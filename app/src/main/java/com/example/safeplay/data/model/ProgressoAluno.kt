package com.example.safeplay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProgressoAluno(
    // Não precisamos enviar o id_progresso se ele for gerado automaticamente pelo banco (Ex: UUID ou Serial)
    val id_aluno: String,
    val id_modulo: String, // Ajuste para Int se o ID da sua fase for numérico no banco
    val status: String,
    val pontuacao_obtida: Int = 0,
    val tentativas: Int = 0
    // Opcional: data_ultima_tentativa omitida para que o banco preencha com o valor padrão (now())
)