package com.example.safeplay.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

// Usamos um 'object' (Singleton) para garantir que existe apenas uma ligação ao banco em toda a app
object SupabaseClient {

    // TODO: Substituir pelos dados reais do seu projeto Supabase (Configurações > API)
    private const val SUPABASE_URL = "https://fztltnlpmhosnggylsub.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_BIw1EWVNn6OuUJW36EIgSA_W4wTiSaF"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // Instalamos os módulos que vamos usar: Autenticação e Banco de Dados (PostgreSQL)
        install(Auth.Companion)
        install(Postgrest.Companion)

        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
        })
    }
}