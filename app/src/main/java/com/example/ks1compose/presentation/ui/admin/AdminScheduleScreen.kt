package com.example.ks1compose.presentation.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.data.DTOs.ScheduleDTO
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.presentation.ui.schedule.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScheduleScreen(
    scheduleViewModel: ScheduleViewModel
) {
    val daysList = listOf(
        "пн" to "Понедельник",
        "вт" to "Вторник",
        "ср" to "Среда",
        "чт" to "Четверг",
        "пт" to "Пятница",
        "сб" to "Суббота"
    )

    var selectedDay by remember { mutableStateOf(daysList[0].first) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var scheduleToDelete by remember { mutableStateOf<ScheduleDTO?>(null) }

    val allSchedules by scheduleViewModel.allSchedules.collectAsStateWithLifecycle()
    val isLoading by scheduleViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by scheduleViewModel.errorMessage.collectAsStateWithLifecycle()
    val operationResult by scheduleViewModel.operationResult.collectAsStateWithLifecycle()

    // Загружаем все расписания при старте
    LaunchedEffect(Unit) {
        scheduleViewModel.loadAllSchedules()
    }

    // Фильтруем расписания по выбранному дню
    val filteredSchedules = remember(allSchedules, selectedDay) {
        allSchedules.filter { it.day == selectedDay }
            .sortedBy { it.className }
    }

    Scaffold(
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Выбор дня
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Выберите день",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Горизонтальный скролл дней
                        SingleChoiceDaySelector(
                            days = daysList,
                            selectedDay = selectedDay,
                            onDaySelected = { selectedDay = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Статистика
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = daysList.find { it.first == selectedDay }?.second ?: selectedDay,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Классов: ${filteredSchedules.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (isLoading && allSchedules.isEmpty()) {
                    PersonalLoadingIndicator()
                } else if (errorMessage != null && filteredSchedules.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ошибка загрузки",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Неизвестная ошибка",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PersonalButton(
                            text = "Повторить",
                            onClick = { scheduleViewModel.loadAllSchedules() },
                            widthFactor = 0.5f
                        )
                    }
                } else if (filteredSchedules.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет расписания на этот день",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = daysList.find { it.first == selectedDay }?.second ?: selectedDay,
                            fontSize = 14.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredSchedules) { schedule ->
                            AdminScheduleCard(
                                schedule = schedule,
                                onDelete = {
                                    scheduleToDelete = schedule
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            if (isLoading && allSchedules.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog && scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Удалить расписание",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Вы уверены, что хотите удалить расписание?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Класс: ${scheduleToDelete!!.className}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "День: ${scheduleToDelete!!.day}",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val token = TokenManager.authToken
                        if (token != null) {
                            scheduleViewModel.deleteSchedule("Bearer $token", scheduleToDelete!!.scheduleId)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = "Удалить",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun SingleChoiceDaySelector(
    days: List<Pair<String, String>>,
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days.size) { index ->
            val day = days[index]
            val isSelected = day.first == selectedDay

            Surface(
                modifier = Modifier
                    .width(70.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDaySelected(day.first) },
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = if (isSelected)
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.first,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScheduleCard(
    schedule: ScheduleDTO,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с классом и кнопкой удаления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = schedule.className,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${schedule.day}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(12.dp))

            // Список уроков
            for (i in schedule.lessons.indices) {
                if (schedule.lessons[i] != "—" && schedule.lessons[i].isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (i + 1).toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = schedule.lessons[i],
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Каб. ${schedule.office.getOrNull(i) ?: "—"}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            if (schedule.lessons.all { it == "—" || it.isBlank() }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Нет уроков",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Информация о ID (для отладки)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ID: ${schedule.scheduleId.take(8)}...",
                fontSize = 10.sp,
                color = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}