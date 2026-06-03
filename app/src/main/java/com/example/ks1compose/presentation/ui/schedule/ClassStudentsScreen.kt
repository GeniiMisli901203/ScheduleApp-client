package com.example.ks1compose.presentation.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.presentation.ui.profile.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassStudentsScreen(
    userViewModel: UserViewModel,
    className: String,
    onNavigateBack: () -> Unit,
    onSelectStudent: (String) -> Unit
) {
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()

    // Заглушка для списка учеников
    val students = remember {
        listOf(
            StudentInfo("1", "Иванов Иван", "10А"),
            StudentInfo("2", "Петров Петр", "10А"),
            StudentInfo("3", "Сидоров Сидор", "10А"),
            StudentInfo("4", "Смирнова Анна", "10А"),
            StudentInfo("5", "Кузнецов Дмитрий", "10А"),
            StudentInfo("6", "Попова Елена", "10А"),
            StudentInfo("7", "Васильев Алексей", "10А"),
            StudentInfo("8", "Михайлова Ольга", "10А")
        )
    }

    Scaffold(
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                PersonalLoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Статистика
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatisticItem(
                                value = students.size.toString(),
                                label = "Учеников"
                            )
                            StatisticItem(
                                value = "4.3",
                                label = "Ср. балл"
                            )
                            StatisticItem(
                                value = "0",
                                label = "Должников"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Список учеников
                    Text(
                        text = "Список учеников",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(students) { student ->
                            StudentItemCard(
                                student = student,
                                onClick = { onSelectStudent(student.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class StudentInfo(
    val id: String,
    val name: String,
    val className: String
)

@Composable
fun StatisticItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
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

@Composable
fun StudentItemCard(
    student: StudentInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = student.name.take(1),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = student.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = student.className,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Row {
                IconButton(onClick = { /* Просмотр оценок */ }) {
                    Icon(
                        Icons.Default.Grade,
                        contentDescription = "Оценки",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить оценку",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}