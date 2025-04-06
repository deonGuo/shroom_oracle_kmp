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
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "http://192.168.1.22:8000/oracle", // 👈 change to your backend URL
            formData = formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"mushroom.jpg\"")
                })
            }
        )
        return response.body()
    }
}
