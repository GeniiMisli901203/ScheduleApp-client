package com.example.ks1compose.data.datasource.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

import android.content.Context
import com.example.ks1compose.data.datasource.local.SecureDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}



object TokenManager {
    var authToken: String? = null
    var userId: String? = null
    var userRole: String? = null
    var userName: String? = null
    var userSName: String? = null

    private lateinit var secureDataStore: SecureDataStore
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var _isInitialized = false

    fun init(context: Context) {
        if (_isInitialized) return

        secureDataStore = SecureDataStore(context)
        // Загружаем данные из DataStore при инициализации
        runBlocking {
            try {
                val userData = secureDataStore.userDataFlow.first()
                userData?.let {
                    authToken = it.token
                    userId = it.userId
                    userRole = it.role
                    userName = it.name
                    userSName = it.sName
                }
                _isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveUserData(
        token: String,
        userId: String,
        role: String,
        name: String,
        sName: String,
        login: String,
        email: String,
        userClass: String,
        school: String
    ) {
        checkInitialized()
        this.authToken = token
        this.userId = userId
        this.userRole = role
        this.userName = name
        this.userSName = sName

        secureDataStore.saveUserData(
            userId = userId,
            token = token,
            login = login,
            role = role,
            name = name,
            sName = sName,
            email = email,
            userClass = userClass,
            school = school
        )
    }

    fun clear() {
        ioScope.launch {
            authToken = null
            userId = null
            userRole = null
            userName = null
            userSName = null
            if (::secureDataStore.isInitialized) {
                secureDataStore.clearUserData()
            }
            _isInitialized = false
        }
    }

    suspend fun clearSuspend() {
        authToken = null
        userId = null
        userRole = null
        userName = null
        userSName = null
        if (::secureDataStore.isInitialized) {
            secureDataStore.clearUserData()
        }
        _isInitialized = false
    }

    private fun checkInitialized() {
        if (!::secureDataStore.isInitialized) {
            throw IllegalStateException("TokenManager must be initialized with init(context) first")
        }
    }

    val fullName: String?
        get() = if (userName != null && userSName != null) {
            "$userName $userSName"
        } else {
            null
        }

    // Публичный геттер для проверки инициализации
    fun isInitialized(): Boolean = _isInitialized
}