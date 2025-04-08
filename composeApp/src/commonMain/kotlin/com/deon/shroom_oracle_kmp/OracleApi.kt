package com.deon.shroom_oracle_kmp

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.client.plugins.*
import io.ktor.client.request.forms.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

object OracleApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun sendImageToOracle(imageBytes: ByteArray): OracleResult {
        val maxRetries = 3
        var attempt = 0
        var lastError: Exception? = null

        while(attempt < maxRetries) {
            try {
                return attemptSend(imageBytes)
            } catch (e: Exception) {
                delay(1000) // wait 2s and retry once
                return attemptSend(imageBytes)
            }
        }
        throw Exception("Oracle unreachable after $maxRetries attempts: ${lastError?.message}")
    }

    suspend fun attemptSend(imageBytes: ByteArray): OracleResult {
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "http://192.168.1.22:8000/oracle", // 👈 change to your backend URL
            formData = formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"mushroom.jpg\"")
                })
            }
        )
        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw Exception("Oracle error: $errorBody")
        }
        return response.body()
    }
}
