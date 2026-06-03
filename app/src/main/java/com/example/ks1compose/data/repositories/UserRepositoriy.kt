import com.example.ks1compose.data.datasource.remote.RetrofitInstance

import com.example.ks1compose.domain.models.UserInformationResponse

class UserRepository {
    private val apiService = RetrofitInstance.apiService

    suspend fun getUserInfo(login: String): UserInformationResponse {
        val response = apiService.getUserByLogin(login)
        if (!response.isSuccessful) {
            throw Exception("Ошибка: ${response.code()}")
        }
        return response.body() ?: throw Exception("Пустой ответ")
    }
}
