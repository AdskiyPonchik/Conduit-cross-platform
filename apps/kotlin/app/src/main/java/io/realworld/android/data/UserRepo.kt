package io.realworld.android.data

import io.realworld.api.ConduitClient
import io.realworld.api.models.entities.LoginData
import io.realworld.api.models.entities.SignupData
import io.realworld.api.models.entities.User
import io.realworld.api.models.entities.UserUpdateData
import io.realworld.api.models.requests.LoginRequest
import io.realworld.api.models.requests.SignupRequest
import io.realworld.api.models.requests.UserUpdateRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

object UserRepo {
    val api = ConduitClient.publicApi
    val authAPI = ConduitClient.authApi

    suspend fun login(
        email: String,
        password: String,
    ): User? =
        try {
            val response = api.loginUser(LoginRequest(LoginData(email, password)))
            ConduitClient.authToken = response.body()?.user?.token
            response.body()?.user
        } catch (e: Exception) {
            null
        }

    suspend fun signup(
        username: String,
        email: String,
        password: String,
    ): User? =
        try {
            val response = api.signupUser(SignupRequest(SignupData(email, password, username)))
            ConduitClient.authToken = response.body()?.user?.token
            response.body()?.user
        } catch (e: Exception) {
            null
        }

    suspend fun getCurrentUser(token: String): User? =
        try {
            ConduitClient.authToken = token
            authAPI.getCurrentUser().body()?.user
        } catch (e: Exception) {
            null
        }

    suspend fun updateUser(
        bio: String?,
        username: String?,
        image: String?,
        email: String?,
        password: String?,
    ): User? =
        try {
            val response =
                authAPI.updateCurrentUser(
                    UserUpdateRequest(
                        UserUpdateData(
                            bio,
                            email,
                            image,
                            username,
                            password,
                        ),
                    ),
                )
            response.body()?.user
        } catch (e: Exception) {
            null
        }

    suspend fun getUserProfile() =
        try {
            authAPI.getCurrentUser().body()?.user
        } catch (e: Exception) {
            null
        }
    suspend fun uploadProfileImage(imageFile: File): String? {
        return try {
            // MIME Type erkennen
            val mimeType = when (imageFile.extension.lowercase()) {
                "png" -> "image/png"
                else -> "image/jpeg"
            }

            val imagePart = MultipartBody.Part.createFormData(
                "file",
                imageFile.name,
                imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
            )

            val response = authAPI.uploadImage(imagePart)
            if (!response.isSuccessful) return null

            val rawBody = response.body()?.string()?.trim().orEmpty()
            if (rawBody.isBlank()) return null

            when {
                rawBody.startsWith("{") -> {
                    val json = JSONObject(rawBody)
                    json.optString("image").takeIf { it.isNotBlank() }
                        ?: json.optString("url").takeIf { it.isNotBlank() }
                        ?: json.optString("imageUrl").takeIf { it.isNotBlank() }
                        ?: json.optString("path").takeIf { it.isNotBlank() }
                }
                rawBody.startsWith("\"") && rawBody.endsWith("\"") -> rawBody.removeSurrounding("\"")
                rawBody.startsWith("http://") || rawBody.startsWith("https://") -> rawBody
                else -> "${ConduitClient.baseUrl.trimEnd('/')}/api/images/$rawBody"
            }
        } catch (e: Exception) {
            null
        }
    }

}
