package com.example.subpro.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


data class AuthStartResponse(val nonce: String, val url: String)
data class TelegramVerifyRequest(val nonce: String, val telegramData: Map<String, String>)
data class VerifyResponse(val token: String?, val error: String?)

interface ApiService {

    @POST("/api/auth/start")
    suspend fun startAuth(): AuthStartResponse

    @POST("/api/auth/verify")
    suspend fun verify(@Body request: TelegramVerifyRequest): VerifyResponse
}

object ApiClient {
    private const val BASE_URL = "https://ТВОЙ_ДОМЕН.ngrok-free.dev"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
