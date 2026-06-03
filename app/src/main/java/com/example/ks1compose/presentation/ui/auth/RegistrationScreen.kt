package com.example.ks1compose.presentation.ui.auth

import android.util.Patterns
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButtonOutlined
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalDropdown
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingOverlay
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.presentation.common.ButtonBGColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel,
    onNavigateToDashboard: (String, String, String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userSName by remember { mutableStateOf("") }
    var userClass by remember { mutableStateOf("") }
    var userSchool by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") }

    // Состояния ошибок
    var loginError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var userNameError by remember { mutableStateOf<String?>(null) }
    var userSNameError by remember { mutableStateOf<String?>(null) }
    var userClassError by remember { mutableStateOf<String?>(null) }
    var userSchoolError by remember { mutableStateOf<String?>(null) }

    val registerResult by authViewModel.registerResult.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()

    val roleOptions = listOf(
        "student" to "Ученик",
        "teacher" to "Учитель",
        "admin" to "Администратор"
    )

    // Обработка результата регистрации
    LaunchedEffect(registerResult) {
        when (val result = registerResult) {
            is AuthViewModel.AuthResult.Success -> {
                val token = result.data.token
                val userId = result.data.userId
                val userRole = result.data.role
                val userNameResponse = result.data.userName
                val userSNameResponse = result.data.userSName

                // Проверяем, что все необходимые данные не null
                if (token.isNotEmpty() && userId.isNotEmpty() && userRole.isNotEmpty()) {
                    onNavigateToDashboard(
                        token,
                        userId,
                        userRole,
                        login,
                        userNameResponse ?: userName,
                        userSNameResponse ?: userSName
                    )
                }
                authViewModel.resetResults()
            }
            else -> {}
        }
    }

    PersonalLoadingOverlay(isLoading = isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок
            Text(
                text = "Регистрация",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ButtonBGColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (role == "student") "Создание аккаунта ученика" else "Создание аккаунта учителя",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Выбор роли
            PersonalDropdown(
                selectedValue = role,
                label = "Выберите роль",
                options = roleOptions,
                onValueChange = {
                    role = it
                    if (it == "teacher") userClass = ""
                },
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Логин
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

            Spacer(modifier = Modifier.height(12.dp))

            // Имя
            PersonalTextField(
                text = userName,
                label = "Имя",
                padding = 0,
                isError = userNameError != null,
                errorMessage = userNameError,
                leadingIcon = Icons.Default.Person,
                onValueChange = {
                    userName = it
                    userNameError = null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Фамилия
            PersonalTextField(
                text = userSName,
                label = "Фамилия",
                padding = 0,
                isError = userSNameError != null,
                errorMessage = userSNameError,
                leadingIcon = Icons.Default.Person,
                onValueChange = {
                    userSName = it
                    userSNameError = null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Класс (только для ученика)
            if (role == "student") {
                PersonalTextField(
                    text = userClass,
                    label = "Класс (например, 10А)",
                    padding = 0,
                    isError = userClassError != null,
                    errorMessage = userClassError,
                    leadingIcon = Icons.Default.School,
                    onValueChange = {
                        userClass = it
                        userClassError = null
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Email
            PersonalTextField(
                text = email,
                label = "Email",
                padding = 0,
                isError = emailError != null,
                errorMessage = emailError,
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                onValueChange = {
                    email = it
                    emailError = null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Пароль
            PersonalTextField(
                text = password,
                label = "Пароль (минимум 8 символов)",
                padding = 0,
                isError = passwordError != null,
                errorMessage = passwordError,
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                onValueChange = {
                    password = it
                    if (it.length >= 8) passwordError = null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Подтверждение пароля
            PersonalTextField(
                text = confirmPassword,
                label = "Подтвердите пароль",
                padding = 0,
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Школа
            PersonalTextField(
                text = userSchool,
                label = "Школа (полное название)",
                padding = 0,
                isError = userSchoolError != null,
                errorMessage = userSchoolError,
                leadingIcon = Icons.Default.School,
                onValueChange = {
                    userSchool = it
                    userSchoolError = null
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка регистрации
            PersonalButton(
                text = "Зарегистрироваться",
                onClick = {
                    var isValid = true

                    // Валидация
                    if (login.isBlank()) {
                        loginError = "Введите логин"
                        isValid = false
                    }

                    if (userName.isBlank()) {
                        userNameError = "Введите имя"
                        isValid = false
                    }

                    if (userSName.isBlank()) {
                        userSNameError = "Введите фамилию"
                        isValid = false
                    }

                    if (role == "student" && userClass.isBlank()) {
                        userClassError = "Введите класс"
                        isValid = false
                    }

                    if (email.isBlank()) {
                        emailError = "Введите email"
                        isValid = false
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Введите корректный email"
                        isValid = false
                    }

                    if (password.isBlank()) {
                        passwordError = "Введите пароль"
                        isValid = false
                    } else if (password.length < 8) {
                        passwordError = "Пароль должен быть минимум 8 символов"
                        isValid = false
                    }

                    if (confirmPassword.isBlank()) {
                        confirmPasswordError = "Подтвердите пароль"
                        isValid = false
                    } else if (password != confirmPassword) {
                        confirmPasswordError = "Пароли не совпадают"
                        isValid = false
                    }

                    if (userSchool.isBlank()) {
                        userSchoolError = "Введите название школы"
                        isValid = false
                    }

                    if (isValid) {
                        authViewModel.register(
                            login = login,
                            email = email,
                            password = password,
                            userName = userName,
                            userSName = userSName,
                            userClass = if (role == "student") userClass else "",
                            userSchool = userSchool,
                            role = role
                        )
                    }
                },
                widthFactor = 1f,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка входа
            PersonalButtonOutlined(
                text = "Уже есть аккаунт? Войти",
                onClick = onNavigateToLogin,
                borderColor = ButtonBGColor,
                textColor = ButtonBGColor
            )

            // Сообщение об ошибке
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}