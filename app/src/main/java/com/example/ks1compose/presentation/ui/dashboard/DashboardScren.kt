package com.example.ks1compose.presentation.ui.dashboard


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.presentation.ui.grades.GradeViewModel
import com.example.ks1compose.presentation.ui.news.NewsViewModel
import com.example.ks1compose.presentation.ui.profile.UserViewModel
import com.example.ks1compose.presentation.ui.schedule.LessonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userViewModel: UserViewModel,
    gradeViewModel: GradeViewModel,
    lessonViewModel: LessonViewModel,
    newsViewModel: NewsViewModel,
    onNavigateToSchedule: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()

    // Загружаем данные при первом входе
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo()
    }

    Scaffold(
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && userInfo == null) {
                PersonalLoadingIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Приветствие
                    item {
                        GreetingCard(
                            userName = userInfo?.let { "${it.name} ${it.sName}" } ?: "Пользователь",
                            userRole = userInfo?.role ?: "student"
                        )
                    }

                    // Быстрые действия
                    item {
                        QuickActionsCard(
                            onNavigateToSchedule = onNavigateToSchedule,
                            onNavigateToGrades = onNavigateToGrades,
                            onNavigateToNews = onNavigateToNews
                        )
                    }

                    // Информация о пользователе
                    item {
                        UserInfoDashboardCard(
                            userClass = if (userInfo?.role == "student") userInfo?.uClass else null,
                            userSchool = userInfo?.school ?: "",
                            userEmail = userInfo?.email ?: ""
                        )
                    }

                    // Статистика
                    item {
                        StatisticsCard()
                    }

                    // Для администратора добавим дополнительную карточку
                    if (userInfo?.role == "admin") {
                        item {
                            QuickActionsCard(
                                onNavigateToSchedule = onNavigateToSchedule,
                                onNavigateToGrades = onNavigateToGrades,
                                onNavigateToNews = onNavigateToNews,
                                onNavigateToAdmin = onNavigateToAdmin,
                                isAdmin = userInfo?.role == "admin"
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// com.example.ks1compose.presentation.ui.dashboard.DashboardScreen.kt
// Исправленная функция QuickActionsCard

@Composable
fun QuickActionsCard(
    onNavigateToSchedule: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToAdmin: (() -> Unit)? = null,  // Добавляем опциональный параметр
    isAdmin: Boolean = false  // Флаг администратора
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Text(
            text = "Быстрые действия",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Определяем количество кнопок для равномерного распределения
        val buttonCount = if (isAdmin && onNavigateToAdmin != null) 4 else 3

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (buttonCount == 4)
                Arrangement.SpaceEvenly
            else
                Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.DateRange,
                label = "Расписание",
                onClick = onNavigateToSchedule,
                color = MaterialTheme.colorScheme.primary
            )

            QuickActionButton(
                icon = Icons.Default.Grade,
                label = "Оценки",
                onClick = onNavigateToGrades,
                color = MaterialTheme.colorScheme.primary
            )

            QuickActionButton(
                icon = Icons.Default.Menu,
                label = "Новости",
                onClick = onNavigateToNews,
                color = MaterialTheme.colorScheme.primary
            )

            // Кнопка администратора только для админа
            if (isAdmin && onNavigateToAdmin != null) {
                QuickActionButton(
                    icon = Icons.Default.Settings,
                    label = "Админ",
                    onClick = onNavigateToAdmin,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
@Composable
fun GreetingCard(
    userName: String,
    userRole: String
) {
    val roleText = when (userRole) {
        "student" -> "Ученик"
        "teacher" -> "Учитель"
        "admin" -> "Администратор"
        else -> userRole
    }

    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        borderColor = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Привет, $userName!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = roleText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onNavigateToSchedule: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToNews: () -> Unit
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Text(
            text = "Быстрые действия",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.DateRange,
                label = "Расписание",
                onClick = onNavigateToSchedule,
                color = MaterialTheme.colorScheme.primary
            )

            QuickActionButton(
                icon = Icons.Default.Grade,
                label = "Оценки",
                onClick = onNavigateToGrades,
                color = MaterialTheme.colorScheme.primary
            )

            QuickActionButton(
                icon = Icons.Default.Menu,
                label = "Новости",
                onClick = onNavigateToNews,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() },
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun UserInfoDashboardCard(
    userClass: String?,
    userSchool: String,
    userEmail: String
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Text(
            text = "Информация",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (userClass != null) {
            InfoRowDashboard(
                icon = Icons.Default.School,
                label = "Класс",
                value = userClass
            )
        }

        InfoRowDashboard(
            icon = Icons.Default.LocationOn,
            label = "Школа",
            value = userSchool
        )

        InfoRowDashboard(
            icon = Icons.Default.Email,
            label = "Email",
            value = userEmail
        )
    }
}

@Composable
fun InfoRowDashboard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
fun StatisticsCard() {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Text(
            text = "Статистика",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "0",
                label = "Оценок"
            )
            StatItem(
                value = "0",
                label = "Уроков"
            )
            StatItem(
                value = "0",
                label = "Новостей"
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}