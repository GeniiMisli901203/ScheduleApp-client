package com.example.ks1compose.data.repositories

import com.example.ks1compose.domain.models.LoginRequest
import com.example.ks1compose.domain.models.RegistrationRequest
import com.example.ks1compose.data.datasource.remote.RetrofitInstance
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.domain.models.TokenResponse
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {
    private val api = RetrofitInstance.apiService

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    suspend fun register(request: RegistrationRequest): Result<TokenResponse> {
        return try {
            val response = api.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                // Сохраняем данные в TokenManager
                TokenManager.authToken = tokenResponse.token
                TokenManager.userId = tokenResponse.userId
                TokenManager.userRole = tokenResponse.role
                TokenManager.userName = tokenResponse.userName
                TokenManager.userSName = tokenResponse.userSName
                Result.Success(tokenResponse)
            } else {
                Result.Error(response.message() ?: "Ошибка регистрации", response.code())
            }
        } catch (e: HttpException) {
            Result.Error("Ошибка сети: ${e.message()}", e.code())
        } catch (e: IOException) {
            Result.Error("Проверьте подключение к интернету")
        } catch (e: Exception) {
            Result.Error("Неизвестная ошибка: ${e.message}")
        }
    }

    suspend fun login(login: String, password: String): Result<TokenResponse> {
        return try {
            val response = api.loginUser(LoginRequest(login, password))
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                TokenManager.authToken = tokenResponse.token
                TokenManager.userId = tokenResponse.userId
                TokenManager.userRole = tokenResponse.role
                TokenManager.userName = tokenResponse.userName
                TokenManager.userSName = tokenResponse.userSName
                Result.Success(tokenResponse)
            } else {
                Result.Error("Неверный логин или пароль", response.code())
            }
        } catch (e: HttpException) {
            Result.Error("Ошибка сети: ${e.message()}", e.code())
        } catch (e: IOException) {
            Result.Error("Проверьте подключение к интернету")
        } catch (e: Exception) {
            Result.Error("Неизвестная ошибка: ${e.message}")
        }
    }

    fun logout() {
        TokenManager.clear()
    }
}