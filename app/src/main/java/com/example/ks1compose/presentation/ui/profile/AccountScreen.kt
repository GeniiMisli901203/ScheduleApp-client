package com.example.ks1compose.presentation.ui.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButtonDanger
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.presentation.ui.auth.AuthViewModel

// com.example.ks1compose.presentation.ui.profile.AccountScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onEditProfile: (String) -> Unit,
    onAddSchedule: () -> Unit,
    onViewAllSchedules: () -> Unit,
    onLogout: () -> Unit,
    userId: String,  // Изменено с userLogin на userId
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()
    val error by userViewModel.error.collectAsStateWithLifecycle()

    val isTeacherOrAdmin = userInfo?.role == "teacher" || userInfo?.role == "admin"
    val isStudent = userInfo?.role == "student"

    // Загружаем информацию о пользователе
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo()
    }

    // Для отладки
    LaunchedEffect(userInfo) {
        println("📤 AccountScreen userInfo loaded:")
        println("📤 - userId: ${userInfo?.userId}")
        println("📤 - login: ${userInfo?.email}")
        println("📤 - name: ${userInfo?.name}")
        println("📤 - role: ${userInfo?.role}")
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                PersonalLoadingIndicator()
            } else if (error != null) {
                // ... код ошибки ...
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Аватар и имя пользователя
                    ProfileHeader(
                        userName = "${userInfo?.name ?: ""} ${userInfo?.sName ?: ""}",
                        userRole = userInfo?.role ?: "",
                        userClass = if (isStudent) userInfo?.uClass else null,
                        userSchool = userInfo?.school ?: "",
                        userId = userInfo?.userId ?: userId,  // Используем userId из параметра как запасной
                        onEditProfile = onEditProfile
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Информация о пользователе
                    UserInfoCard(
                        email = userInfo?.email ?: "",
                        login = userInfo?.email ?: "",  // Используем email как логин
                        userId = userInfo?.userId ?: userId
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Настройки
                    SettingsCard(
                        darkTheme = darkTheme,
                        onThemeChange = onThemeChange
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Действия для учителей/админов
                    if (isTeacherOrAdmin) {
                        TeacherActionsCard(
                            onAddSchedule = onAddSchedule,
                            onViewAllSchedules = onViewAllSchedules
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Кнопка выхода
                    PersonalButtonDanger(
                        text = "Выйти из аккаунта",
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Версия приложения
                    Text(
                        text = "Версия 1.0.0",
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userRole: String,
    userClass: String?,
    userSchool: String,
    userId: String,  // Добавляем параметр
    onEditProfile: (String) -> Unit
) {
    val roleText = when (userRole) {
        "student" -> "Ученик"
        "teacher" -> "Учитель"
        "admin" -> "Администратор"
        else -> userRole
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Аватар с кнопкой редактирования
            Box(
                modifier = Modifier
                    .size(100.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                    }
                }

                // Кнопка редактирования
                IconButton(
                    onClick = {
                        println("📤 Editing profile for user with ID: $userId")
                        onEditProfile(userId)  // Передаем userId
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать профиль",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Имя
            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Роль
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = roleText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            if (userClass != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userClass,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userSchool,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
@Composable
fun UserInfoCard(
    email: String,
    login: String,
    userId: String
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Информация",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = email
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray.copy(alpha = 0.2f)
            )

            InfoRow(
                icon = Icons.Default.Person,
                label = "Логин",
                value = login
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray.copy(alpha = 0.2f)
            )

            InfoRow(
                icon = Icons.Default.Info,
                label = "ID",
                value = userId
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SettingsCard(
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Настройки",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Темная тема",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = darkTheme,
                    onCheckedChange = onThemeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
fun TeacherActionsCard(
    onAddSchedule: () -> Unit,
    onViewAllSchedules: () -> Unit
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Действия учителя",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка добавления расписания
            ActionButton(
                icon = Icons.Default.Add,
                text = "Добавить расписание",
                onClick = onAddSchedule
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка просмотра всех расписаний
            ActionButton(
                icon = Icons.Default.DateRange,
                text = "Все расписания",
                onClick = onViewAllSchedules
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}