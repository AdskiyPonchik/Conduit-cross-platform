package io.realworld.android.data

import io.realworld.api.ConduitClient
import io.realworld.api.models.entities.LoginData
import io.realworld.api.models.entities.SignupData
import io.realworld.api.models.entities.User
import io.realworld.api.models.entities.UserUpdateData
import io.realworld.api.models.requests.LoginRequest
import io.realworld.api.models.requests.SignupRequest
import io.realworld.api.models.requests.UserUpdateRequest
import io.realworld.api.models.responses.UserResponse

object UserRepo {
    val api = ConduitClient.publicApi
    val authAPI = ConduitClient.authApi

    suspend fun login(email: String, password: String): User? = try {
        val response = api.loginUser(LoginRequest(LoginData(email, password)))
        ConduitClient.authToken = response.body()?.user?.token
        response.body()?.user
    } catch (e: Exception) { null }

    suspend fun signup(username: String, email: String, password: String): User? = try {
        val response = api.signupUser(SignupRequest(SignupData(email, password, username)))
        ConduitClient.authToken = response.body()?.user?.token
        response.body()?.user
    } catch (e: Exception) { null }

    suspend fun getCurrentUser(token: String): User? = try {
        ConduitClient.authToken = token
        authAPI.getCurrentUser().body()?.user
    } catch (e: Exception) { null }

    suspend fun updateUser(
        bio: String?,
        username: String?,
        image: String?,
        email: String?,
        password: String?
    ): User? = try {
        val response = authAPI.updateCurrentUser(UserUpdateRequest(UserUpdateData(
            bio, email, image, username, password
        )))
        response.body()?.user
    } catch (e: Exception) { null }

    suspend fun getUserProfile() = try {
        authAPI.getCurrentUser().body()?.user
    } catch (e: Exception) { null }

}