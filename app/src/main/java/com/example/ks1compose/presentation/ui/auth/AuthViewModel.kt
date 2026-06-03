package com.example.ks1compose.presentation.ui.auth

import com.example.ks1compose.domain.models.RegistrationRequest


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.domain.models.TokenResponse
import com.example.ks1compose.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    sealed class AuthResult {
        data class Success(val data: TokenResponse) : AuthResult()
        data class Error(val message: String) : AuthResult()
        object Loading : AuthResult()
    }

    private val _loginResult = MutableStateFlow<AuthResult?>(null)
    val loginResult: StateFlow<AuthResult?> = _loginResult

    private val _registerResult = MutableStateFlow<AuthResult?>(null)
    val registerResult: StateFlow<AuthResult?> = _registerResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(login: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginResult.value = AuthResult.Loading

            val result = repository.login(login, password)
            when (result) {
                is AuthRepository.Result.Success -> {
                    _loginResult.value = AuthResult.Success(result.data)
                }
                is AuthRepository.Result.Error -> {
                    _loginResult.value = AuthResult.Error(result.message)
                    _errorMessage.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearUserData() {
        TokenManager.clear()
        _loginResult.value = null
        _registerResult.value = null
        _errorMessage.value = null
    }

    fun register(
        login: String,
        email: String,
        password: String,
        userName: String,
        userSName: String,
        userClass: String,
        userSchool: String,
        role: String = "student"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _registerResult.value = AuthResult.Loading

            val request = RegistrationRequest(
                login = login,
                email = email,
                password = password,
                userName = userName,
                userSName = userSName,
                userClass = userClass,
                userSchool = userSchool,
                role = role
            )

            val result = repository.register(request)
            when (result) {
                is AuthRepository.Result.Success -> {
                    _registerResult.value = AuthResult.Success(result.data)
                }
                is AuthRepository.Result.Error -> {
                    _registerResult.value = AuthResult.Error(result.message)
                    _errorMessage.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun resetResults() {
        _loginResult.value = null
        _registerResult.value = null
        _errorMessage.value = null
    }

    fun logout() {
        repository.logout()
        resetResults()
    }
}