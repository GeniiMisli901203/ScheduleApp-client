package com.example.ks1compose.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ks1compose.domain.models.UserDTO
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class SecureDataStore(private val context: Context) {

    private val gson = Gson()

    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_TOKEN = stringPreferencesKey("user_token")
        val USER_LOGIN = stringPreferencesKey("user_login")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_SNAME = stringPreferencesKey("user_sname")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_CLASS = stringPreferencesKey("user_class")
        val USER_SCHOOL = stringPreferencesKey("user_school")

        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    suspend fun saveUserData(
        userId: String,
        token: String,
        login: String,
        role: String,
        name: String,
        sName: String,
        email: String,
        userClass: String,
        school: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_TOKEN] = token
            preferences[USER_LOGIN] = login
            preferences[USER_ROLE] = role
            preferences[USER_NAME] = name
            preferences[USER_SNAME] = sName
            preferences[USER_EMAIL] = email
            preferences[USER_CLASS] = userClass
            preferences[USER_SCHOOL] = school
        }
    }

    suspend fun saveUser(user: UserDTO, token: String, login: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = user.userId
            preferences[USER_TOKEN] = token
            preferences[USER_LOGIN] = login
            preferences[USER_ROLE] = user.role
            preferences[USER_NAME] = user.name
            preferences[USER_SNAME] = user.sName
            preferences[USER_EMAIL] = user.email
            preferences[USER_CLASS] = user.uClass
            preferences[USER_SCHOOL] = user.school
        }
    }

    val userDataFlow: Flow<UserData?> = context.dataStore.data.map { preferences ->
        val userId = preferences[USER_ID] ?: return@map null
        UserData(
            userId = userId,
            token = preferences[USER_TOKEN] ?: "",
            login = preferences[USER_LOGIN] ?: "",
            role = preferences[USER_ROLE] ?: "",
            name = preferences[USER_NAME] ?: "",
            sName = preferences[USER_SNAME] ?: "",
            email = preferences[USER_EMAIL] ?: "",
            userClass = preferences[USER_CLASS] ?: "",
            school = preferences[USER_SCHOOL] ?: ""
        )
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[USER_TOKEN]
    }

    suspend fun getUserRole(): String? {
        return context.dataStore.data.first()[USER_ROLE]
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USER_TOKEN)
            preferences.remove(USER_LOGIN)
            preferences.remove(USER_ROLE)
            preferences.remove(USER_NAME)
            preferences.remove(USER_SNAME)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_CLASS)
            preferences.remove(USER_SCHOOL)
        }
    }

    suspend fun saveDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME] = isDark
        }
    }

    val darkThemeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME] ?: false
    }

    suspend fun isFirstLaunch(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[FIRST_LAUNCH] ?: true
    }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = false
        }
    }
}

data class UserData(
    val userId: String,
    val token: String,
    val login: String,
    val role: String,
    val name: String,
    val sName: String,
    val email: String,
    val userClass: String,
    val school: String
)