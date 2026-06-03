package com.example.ks1compose.presentation.ui.grades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalDropdown
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.presentation.ui.profile.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGradeScreen(
    gradeViewModel: GradeViewModel,
    userViewModel: UserViewModel,
    onGradeAdded: () -> Unit
) {
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle()
    val isLoading by gradeViewModel.isLoading.collectAsStateWithLifecycle()

    var selectedClass by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("") }
    var selectedGradeType by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var lessonDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }

    var classError by remember { mutableStateOf<String?>(null) }
    var studentError by remember { mutableStateOf<String?>(null) }
    var subjectError by remember { mutableStateOf<String?>(null) }
    var gradeError by remember { mutableStateOf<String?>(null) }
    var gradeTypeError by remember { mutableStateOf<String?>(null) }

    val classList = listOf("5А", "5Б", "6А", "6Б", "7А", "7Б", "8А", "8Б", "9А", "9Б", "10А", "10Б", "11А", "11Б")
    val gradeValues = listOf("2", "3", "4", "5")
    val gradeTypes = listOf(
        "homework" to "Домашняя работа",
        "classwork" to "Классная работа",
        "test" to "Контрольная работа",
        "exam" to "Экзамен"
    )
    val subjects = listOf("Математика", "Физика", "Химия", "Биология", "История", "Литература", "Английский", "Информатика")

    // Заглушка для списка учеников
    val students = listOf(
        "1" to "Иванов Иван",
        "2" to "Петров Петр",
        "3" to "Сидоров Сидор",
        "4" to "Смирнова Анна"
    )

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
                PersonalCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Информация об оценке",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор ученика
                    PersonalDropdown(
                        selectedValue = selectedStudent,
                        label = "Выберите ученика",
                        options = students,
                        onValueChange = {
                            selectedStudent = it
                            studentError = null
                        },
                        isError = studentError != null,
                        errorMessage = studentError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор предмета
                    PersonalDropdown(
                        selectedValue = selectedSubject,
                        label = "Выберите предмет",
                        options = subjects.map { it to it },
                        onValueChange = {
                            selectedSubject = it
                            subjectError = null
                        },
                        isError = subjectError != null,
                        errorMessage = subjectError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор оценки
                    PersonalDropdown(
                        selectedValue = selectedGrade,
                        label = "Выберите оценку",
                        options = gradeValues.map { it to it },
                        onValueChange = {
                            selectedGrade = it
                            gradeError = null
                        },
                        isError = gradeError != null,
                        errorMessage = gradeError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор типа оценки
                    PersonalDropdown(
                        selectedValue = selectedGradeType,
                        label = "Тип оценки",
                        options = gradeTypes,
                        onValueChange = {
                            selectedGradeType = it
                            gradeTypeError = null
                        },
                        isError = gradeTypeError != null,
                        errorMessage = gradeTypeError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Дата урока
                    PersonalTextField(
                        text = lessonDate,
                        label = "Дата урока (ГГГГ-ММ-ДД)",
                        padding = 0,
                        leadingIcon = Icons.Default.CalendarToday,
                        onValueChange = { lessonDate = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Комментарий
                    PersonalTextField(
                        text = comment,
                        label = "Комментарий (необязательно)",
                        padding = 0,
                        leadingIcon = Icons.Default.Comment,
                        maxLines = 3,
                        singleLine = false,
                        onValueChange = { comment = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PersonalButton(
                    text = "Добавить оценку",
                    onClick = {
                        var isValid = true

                        if (selectedClass.isBlank()) {
                            classError = "Выберите класс"
                            isValid = false
                        }

                        if (selectedStudent.isBlank()) {
                            studentError = "Выберите ученика"
                            isValid = false
                        }

                        if (selectedSubject.isBlank()) {
                            subjectError = "Выберите предмет"
                            isValid = false
                        }

                        if (selectedGrade.isBlank()) {
                            gradeError = "Выберите оценку"
                            isValid = false
                        }

                        if (selectedGradeType.isBlank()) {
                            gradeTypeError = "Выберите тип оценки"
                            isValid = false
                        }

                        if (isValid) {
                            // Добавление оценки
                            onGradeAdded()
                        }
                    },
                    widthFactor = 1f,
                    isLoading = isLoading
                )
            }

            if (isLoading) {
                PersonalLoadingIndicator()
            }
        }
    }
}