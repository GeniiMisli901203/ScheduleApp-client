// com.example.ks1compose.presentation.ui.profile.EditProfileScreen.kt
package com.example.ks1compose.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalTextField

// com.example.ks1compose.presentation.ui.profile.EditProfileScreen.kt
// com.example.ks1compose.presentation.ui.profile.EditProfileScreen.kt
// Проверьте, что вы используете правильные переменные

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userViewModel: UserViewModel,
    onProfileUpdated: () -> Unit,
    userId: String
) {
    val editingUser by userViewModel.editingUser.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()
    val updateResult by userViewModel.updateResult.collectAsStateWithLifecycle()
    val error by userViewModel.error.collectAsStateWithLifecycle()

    var userName by remember { mutableStateOf("") }
    var userSName by remember { mutableStateOf("") }
    var userClass by remember { mutableStateOf("") }
    var userSchool by remember { mutableStateOf("") }

    // Состояния ошибок
    var nameError by remember { mutableStateOf<String?>(null) }
    var sNameError by remember { mutableStateOf<String?>(null) }
    var classError by remember { mutableStateOf<String?>(null) }
    var schoolError by remember { mutableStateOf<String?>(null) }

    // Загружаем данные пользователя по ID
    LaunchedEffect(userId) {
        println("📤 Loading user data for ID: $userId")
        userViewModel.loadUserById(userId)
    }

    // Обновляем поля когда данные загружены
    LaunchedEffect(editingUser) {
        editingUser?.let {
            println("📤 User data loaded: name=${it.name}, sName=${it.sName}, class=${it.uClass}, school=${it.school}")
            userName = it.name ?: ""
            userSName = it.sName ?: ""
            userClass = it.uClass ?: ""
            userSchool = it.school ?: ""
        }
    }

    // Обработка результата обновления
    LaunchedEffect(updateResult) {
        when (updateResult) {
            is UserViewModel.UpdateResult.Success -> {
                println("✅ Update successful, closing screen")
                onProfileUpdated()
                userViewModel.clearUpdateResult()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать профиль", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onProfileUpdated) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && editingUser == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(50.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Аватар
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Форма редактирования
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Основная информация",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Имя
                            PersonalTextField(
                                text = userName,
                                label = "Имя",
                                padding = 0,
                                isError = nameError != null,
                                errorMessage = nameError,
                                leadingIcon = Icons.Default.Person,
                                onValueChange = {
                                    userName = it
                                    nameError = null
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Фамилия
                            PersonalTextField(
                                text = userSName,
                                label = "Фамилия",
                                padding = 0,
                                isError = sNameError != null,
                                errorMessage = sNameError,
                                leadingIcon = Icons.Default.Person,
                                onValueChange = {
                                    userSName = it
                                    sNameError = null
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Класс (только для учеников)
                            if (editingUser?.role == "student") {
                                PersonalTextField(
                                    text = userClass,
                                    label = "Класс",
                                    padding = 0,
                                    isError = classError != null,
                                    errorMessage = classError,
                                    leadingIcon = Icons.Default.School,
                                    onValueChange = {
                                        userClass = it
                                        classError = null
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Школа
                            PersonalTextField(
                                text = userSchool,
                                label = "Школа",
                                padding = 0,
                                isError = schoolError != null,
                                errorMessage = schoolError,
                                leadingIcon = Icons.Default.School,
                                onValueChange = {
                                    userSchool = it
                                    schoolError = null
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопка сохранения
                    PersonalButton(
                        text = "Сохранить изменения",
                        onClick = {
                            var isValid = true

                            if (userName.isBlank()) {
                                nameError = "Введите имя"
                                isValid = false
                            } else {
                                println("📤 UserName value: '$userName'")
                            }

                            if (userSName.isBlank()) {
                                sNameError = "Введите фамилию"
                                isValid = false
                            }

                            if (editingUser?.role == "student" && userClass.isBlank()) {
                                classError = "Введите класс"
                                isValid = false
                            }

                            if (userSchool.isBlank()) {
                                schoolError = "Введите школу"
                                isValid = false
                            }

                            if (isValid) {
                                println("📤 Calling updateUserInfo with userId: $userId")
                                userViewModel.updateUserInfo(
                                    userId = userId,  // Для админа это ID другого пользователя
                                    name = userName,
                                    sName = userSName,
                                    uClass = userClass,
                                    school = userSchool
                                )
                            }
                        },
                        widthFactor = 1f,
                        isLoading = isLoading && updateResult is UserViewModel.UpdateResult.Loading
                    )

                    // Сообщение об ошибке
                    if (error != null && updateResult is UserViewModel.UpdateResult.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Сообщение об успехе
                    if (updateResult is UserViewModel.UpdateResult.Success) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Профиль успешно обновлен!",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}