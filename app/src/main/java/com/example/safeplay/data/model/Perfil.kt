package com.example.safeplay.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

// A anotação @Serializable é o que permite ao Ktor/Supabase
// transformar este objeto Kotlin em JSON para o banco de dados.

@Serializable
data class Perfil(
    val id_perfil: String,   // O ID único gerado pelo Supabase Auth
    val nome: String, // O nome preenchido no formulário
    val role: String  // 'aluno' ou 'educador'
)