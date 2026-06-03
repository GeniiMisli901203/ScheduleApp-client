package com.example.ks1compose.presentation.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.data.repositories.UserRepository
import com.example.ks1compose.domain.models.StudentUIModel
import com.example.ks1compose.domain.models.UserDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(context: Context) : ViewModel() {  // Конструктор с Context
    private val repository = UserRepository(context)

    private val _userInfo = MutableStateFlow<UserDTO?>(null)
    val userInfo: StateFlow<UserDTO?> = _userInfo.asStateFlow()

    private val _students = MutableStateFlow<List<StudentUIModel>>(emptyList())
    val students: StateFlow<List<StudentUIModel>> = _students.asStateFlow()

    private val _teachers = MutableStateFlow<List<UserDTO>>(emptyList())
    val teachers: StateFlow<List<UserDTO>> = _teachers.asStateFlow()

    private val _editingUser = MutableStateFlow<UserDTO?>(null)
    val editingUser: StateFlow<UserDTO?> = _editingUser.asStateFlow()

    sealed class UpdateResult {
        object Success : UpdateResult()
        data class Error(val message: String) : UpdateResult()
        object Loading : UpdateResult()
    }

    private val _updateResult = MutableStateFlow<UpdateResult?>(null)
    val updateResult: StateFlow<UpdateResult?> = _updateResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (TokenManager.authToken != null) {
            loadUserInfo()
        }
    }


    fun loadUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getUserInfo()) {
                is UserRepository.Result.Success -> {
                    _userInfo.value = result.data
                    TokenManager.userId = result.data.userId
                    TokenManager.userRole = result.data.role
                    TokenManager.userName = result.data.name
                    TokenManager.userSName = result.data.sName
                }
                is UserRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadUserById(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getUserById(userId)) {
                is UserRepository.Result.Success -> {
                    _editingUser.value = result.data
                }
                is UserRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateUserInfo(
        userId: String,
        name: String,
        sName: String,
        uClass: String,
        school: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateResult.value = UpdateResult.Loading
            _error.value = null

            when (val result = repository.updateUserInfo(userId, name, sName, uClass, school)) {
                is UserRepository.Result.Success -> {
                    _updateResult.value = UpdateResult.Success
                    _editingUser.value = _editingUser.value?.copy(
                        name = name,
                        sName = sName,
                        uClass = uClass,
                        school = school
                    )
                    // Обновляем списки после изменения
                    loadAllStudents(true)
                    loadAllTeachers(true)
                }
                is UserRepository.Result.Error -> {
                    _updateResult.value = UpdateResult.Error(result.message)
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadStudentsByClass(className: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getStudentsByClass(className)) {
                is UserRepository.Result.Success -> {
                    _students.value = result.data
                }
                is UserRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadAllTeachers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getAllTeachers(forceRefresh)) {
                is UserRepository.Result.Success -> {
                    _teachers.value = result.data
                }
                is UserRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadAllStudents(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getAllStudents(forceRefresh)) {
                is UserRepository.Result.Success -> {
                    _students.value = result.data.map { user ->
                        StudentUIModel(
                            id = user.userId,
                            name = "${user.name} ${user.sName}",
                            className = user.uClass,
                            averageGrade = null
                        )
                    }
                }
                is UserRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }

    suspend fun getUserById(userId: String): UserRepository.Result<UserDTO> {
        return repository.getUserById(userId)
    }
    fun clearError() {
        _error.value = null
    }

    fun clearStudents() {
        _students.value = emptyList()
    }

    val isTeacher: Boolean
        get() = _userInfo.value?.role == "teacher"

    val isStudent: Boolean
        get() = _userInfo.value?.role == "student"

    val isAdmin: Boolean
        get() = _userInfo.value?.role == "admin"

    val fullName: String
        get() = _userInfo.value?.let { "${it.name} ${it.sName}" } ?: ""
}