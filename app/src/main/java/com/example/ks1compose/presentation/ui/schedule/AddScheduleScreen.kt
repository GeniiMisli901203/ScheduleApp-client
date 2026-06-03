package com.example.ks1compose.presentation.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.data.DTOs.ScheduleDTO
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalDropdown
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.data.datasource.remote.TokenManager
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    scheduleViewModel: ScheduleViewModel,
    onNavigateBack: () -> Unit
) {
    val daysList = listOf(
        "пн" to "Понедельник",
        "вт" to "Вторник",
        "ср" to "Среда",
        "чт" to "Четверг",
        "пт" to "Пятница",
        "сб" to "Суббота"
    )

    val classList = listOf(
        "5А", "5Б", "6А", "6Б", "7А", "7Б", "8А", "8Б",
        "9А", "9Б", "10А", "10Б", "11А", "11Б"
    )

    var selectedClass by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("") }
    var lessons by remember { mutableStateOf(List(9) { "" }) }
    var offices by remember { mutableStateOf(List(9) { "" }) }

    var classError by remember { mutableStateOf<String?>(null) }
    var dayError by remember { mutableStateOf<String?>(null) }

    val isLoading by scheduleViewModel.isLoading.collectAsStateWithLifecycle()
    val operationResult by scheduleViewModel.operationResult.collectAsStateWithLifecycle()
    val errorMessage by scheduleViewModel.errorMessage.collectAsStateWithLifecycle()

    // Обработка результата добавления
    LaunchedEffect(operationResult) {
        if (operationResult != null) {
            onNavigateBack()
            scheduleViewModel.clearMessages()
        }
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Информационная карточка
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Заполните расписание для класса. Можно указать до 9 уроков.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Выбор класса и дня
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

                        // Выбор класса
                        PersonalDropdown(
                            selectedValue = selectedClass,
                            label = "Выберите класс",
                            options = classList.map { it to it },
                            onValueChange = {
                                selectedClass = it
                                classError = null
                            },
                            isError = classError != null,
                            errorMessage = classError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Выбор дня
                        PersonalDropdown(
                            selectedValue = selectedDay,
                            label = "Выберите день недели",
                            options = daysList,
                            onValueChange = {
                                selectedDay = it
                                dayError = null
                            },
                            isError = dayError != null,
                            errorMessage = dayError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Список уроков
                Text(
                    text = "Уроки",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                for (i in 0 until 9) {
                    LessonInputCard(
                        lessonNumber = i + 1,
                        lesson = lessons[i],
                        office = offices[i],
                        onLessonChange = { newValue ->
                            lessons = lessons.toMutableList().apply { this[i] = newValue }
                        },
                        onOfficeChange = { newValue ->
                            offices = offices.toMutableList().apply { this[i] = newValue }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопка добавления
                PersonalButton(
                    text = "Добавить расписание",
                    onClick = {
                        var isValid = true

                        if (selectedClass.isBlank()) {
                            classError = "Выберите класс"
                            isValid = false
                        }

                        if (selectedDay.isBlank()) {
                            dayError = "Выберите день недели"
                            isValid = false
                        }

                        if (isValid) {
                            val token = TokenManager.authToken
                            if (token == null) {
                                scheduleViewModel.setErrorMessage("Не авторизован")
                                return@PersonalButton
                            }

                            val schedule = ScheduleDTO(
                                scheduleId = UUID.randomUUID().toString(),
                                className = selectedClass,
                                day = selectedDay,
                                lessons = lessons.map { if (it.isBlank()) "—" else it },
                                office = offices.map { if (it.isBlank()) "—" else it }
                            )

                            scheduleViewModel.addSchedule("Bearer $token", schedule)
                        }
                    },
                    widthFactor = 1f,
                    isLoading = isLoading
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

            if (isLoading) {
                PersonalLoadingIndicator()
            }
        }
    }
}

@Composable
fun LessonInputCard(
    lessonNumber: Int,
    lesson: String,
    office: String,
    onLessonChange: (String) -> Unit,
    onOfficeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = lessonNumber.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Урок $lessonNumber",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Название урока
            PersonalTextField(
                text = lesson,
                label = "Название урока",
                padding = 0,
                leadingIcon = Icons.Default.MenuBook,
                onValueChange = onLessonChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Кабинет
            PersonalTextField(
                text = office,
                label = "Кабинет",
                padding = 0,
                leadingIcon = Icons.Default.Room,
                onValueChange = onOfficeChange
            )
        }
    }
}