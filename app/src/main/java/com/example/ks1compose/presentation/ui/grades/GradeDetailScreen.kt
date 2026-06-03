package com.example.ks1compose.presentation.ui.grades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailScreen(
    gradeViewModel: GradeViewModel,
    gradeId: String,
    onNavigateBack: () -> Unit
) {
    // Здесь будет загрузка детальной информации об оценке
    val isLoading = remember { mutableStateOf(false) }

    Scaffold(
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading.value) {
                PersonalLoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Оценка
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "5",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Предмет
                    Text(
                        text = "Математика",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Тип оценки
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Домашняя работа",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Детальная информация
                    PersonalCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DetailRow(
                                icon = Icons.Default.Person,
                                label = "Учитель",
                                value = "Иванова М.И."
                            )

                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color.Gray.copy(alpha = 0.2f)
                            )

                            DetailRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Дата",
                                value = "15.01.2024"
                            )

                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color.Gray.copy(alpha = 0.2f)
                            )

                            DetailRow(
                                icon = Icons.Default.Class,
                                label = "Класс",
                                value = "10А"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Комментарий
                    PersonalCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Комментарий",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Отличная работа! Молодец!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопки для учителя
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { /* Редактировать */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Изменить")
                        }

                        Button(
                            onClick = { /* Удалить */ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.8f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
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