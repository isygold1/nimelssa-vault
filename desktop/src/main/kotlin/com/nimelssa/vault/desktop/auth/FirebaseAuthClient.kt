package com.nimelssa.vault.desktop.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

data class AuthUser(val uid: String, val email: String?)

/**
 * Talks to Firebase Authentication through the Identity Toolkit REST API so the
 * desktop build can share the same project as the Android app without the
 * Android-only Firebase SDK.
 */
object FirebaseAuthClient {
    private const val API_KEY = "AIzaSyALIsMT5RzeMjFdetX1XpPJjEatRgJDLwU"
    private const val BASE = "https://identitytoolkit.googleapis.com/v1/accounts:"

    suspend fun signUp(email: String, password: String): Result<AuthUser> =
        authenticate("signUp?key=$API_KEY", email, password)

    suspend fun signIn(email: String, password: String): Result<AuthUser> =
        authenticate("signInWithPassword?key=$API_KEY", email, password)

    private suspend fun authenticate(
        path: String,
        email: String,
        password: String
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
            }

            val payload = buildJsonObject {
                put("email", email)
                put("password", password)
                put("returnSecureToken", true)
            }.toString()

            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (status !in 200..299) {
                throw IllegalStateException(parseError(body))
            }

            val json = Json.parseToJsonElement(body).jsonObject
            AuthUser(
                uid = json["localId"]?.jsonPrimitive?.content.orEmpty(),
                email = json["email"]?.jsonPrimitive?.content
            )
        }
    }

    private fun parseError(raw: String): String {
        val message = runCatching {
            Json.parseToJsonElement(raw).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
        }.getOrNull() ?: return "Authentication failed."

        return when {
            message == "EMAIL_EXISTS" -> "An account already exists for that email."
            message == "EMAIL_NOT_FOUND" -> "No account found for that email."
            message == "INVALID_PASSWORD" -> "Incorrect password."
            message == "INVALID_EMAIL" -> "Enter a valid email address."
            message.startsWith("WEAK_PASSWORD") -> "Password must be at least 6 characters."
            message == "OPERATION_NOT_ALLOWED" -> "Email/password sign-in is not enabled for this project."
            message == "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Too many attempts. Try again later."
            else -> message.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
