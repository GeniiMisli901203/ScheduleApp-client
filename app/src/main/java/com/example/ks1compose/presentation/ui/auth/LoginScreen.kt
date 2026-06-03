package com.example.ks1compose.presentation.ui.auth

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButtonOutlined
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingOverlay
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.R
import com.example.ks1compose.domain.models.FcmTokenRequest
import com.example.ks1compose.domain.models.RetrofitInstanceWithAuth
import com.example.ks1compose.presentation.common.ButtonBGColor
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToDashboard: (String, String, String, String, String, String) -> Unit,
    onNavigateToRegistration: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // ✅ Создаем coroutine scope для композабла

    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loginError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    val loginResult by authViewModel.loginResult.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    // Обработка результата логина
    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is AuthViewModel.AuthResult.Success -> {
                // ✅ Отправляем FCM токен в фоновом режиме
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val fcmToken = sharedPrefs.getString("fcm_token", null)

                if (fcmToken != null) {
                    // Используем scope из композабла
                    scope.launch {
                        try {
                            val api = RetrofitInstanceWithAuth.apiService
                            api.registerFcmToken(
                                "Bearer ${result.data.token}",
                                FcmTokenRequest(fcmToken)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Навигация на главный экран
                onNavigateToDashboard(
                    result.data.token,
                    result.data.userId,
                    result.data.role,
                    login,
                    result.data.userName,
                    result.data.userSName
                )
                authViewModel.resetResults()
            }
            else -> {}
        }
    }

    PersonalLoadingOverlay(isLoading = isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Логотип
            Image(
                painter = painterResource(R.drawable.logo4),
                contentDescription = "logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок
            Text(
                text = "Расписание школы",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.drawWithCache {
                    val brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF9E82F0),
                            Color(0xFF42A5F5)
                        )
                    )
                    onDrawBehind {
                        drawRoundRect(
                            brush,
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Поле логина
            PersonalTextField(
                text = login,
                label = "Логин",
                padding = 0,
                isError = loginError != null,
                errorMessage = loginError,
                leadingIcon = Icons.Default.Person,
                onValueChange = {
                    login = it
                    loginError = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле пароля
            PersonalTextField(
                text = password,
                label = "Пароль",
                padding = 0,
                isError = passwordError != null,
                errorMessage = passwordError,
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                onValueChange = {
                    password = it
                    passwordError = null
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка входа
            PersonalButton(
                text = "Войти",
                onClick = {
                    var isValid = true

                    if (login.isBlank()) {
                        loginError = "Введите логин"
                        isValid = false
                    }

                    if (password.isBlank()) {
                        passwordError = "Введите пароль"
                        isValid = false
                    }

                    if (isValid) {
                        authViewModel.login(login, password)
                    }
                },
                widthFactor = 1f
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка регистрации
            PersonalButtonOutlined(
                text = "Создать аккаунт",
                onClick = onNavigateToRegistration,
                borderColor = ButtonBGColor,
                textColor = ButtonBGColor
            )

            // Сообщение об ошибке
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}