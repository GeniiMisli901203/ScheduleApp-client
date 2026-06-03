package com.example.ks1compose.presentation.ui.grades

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalDropdown
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.presentation.ui.profile.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddGradeDialog(
    className: String,
    userViewModel: UserViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, String?, String) -> Unit
) {
    val students by userViewModel.students.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()
    val error by userViewModel.error.collectAsStateWithLifecycle()

    var selectedStudentId by remember { mutableStateOf("") }
    var selectedStudentName by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("") }
    var selectedGradeType by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var lessonDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }

    // Загружаем учеников при открытии диалога
    LaunchedEffect(className) {
        userViewModel.loadStudentsByClass(className)
    }

    // Формируем список учеников для выпадающего списка
    val studentOptions = students.map { student ->
        student.id to student.name
    }

    val subjects = listOf("Математика", "Физика", "Химия", "Биология", "История", "Литература", "Английский", "Информатика")
    val gradeValues = listOf("2" to "2", "3" to "3", "4" to "4", "5" to "5")
    val gradeTypes = listOf(
        "homework" to "Домашняя работа",
        "classwork" to "Классная работа",
        "test" to "Контрольная работа",
        "exam" to "Экзамен"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Добавить оценку",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Класс: $className",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    PersonalLoadingIndicator()
                } else if (error != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error!!,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { userViewModel.loadStudentsByClass(className) },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Повторить")
                        }
                    }
                } else {
                    // Выбор ученика
                    PersonalDropdown(
                        selectedValue = selectedStudentId,
                        label = "Выберите ученика",
                        options = studentOptions,
                        onValueChange = { studentId ->
                            selectedStudentId = studentId
                            // Сохраняем имя выбранного ученика
                            selectedStudentName = students.find { it.id == studentId }?.name ?: ""
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор предмета
                    PersonalDropdown(
                        selectedValue = selectedSubject,
                        label = "Выберите предмет",
                        options = subjects.map { it to it },
                        onValueChange = { selectedSubject = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор оценки
                    PersonalDropdown(
                        selectedValue = selectedGrade,
                        label = "Выберите оценку",
                        options = gradeValues,
                        onValueChange = { selectedGrade = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор типа оценки
                    PersonalDropdown(
                        selectedValue = selectedGradeType,
                        label = "Тип оценки",
                        options = gradeTypes,
                        onValueChange = { selectedGradeType = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Дата урока
                    PersonalTextField(
                        text = lessonDate,
                        label = "Дата урока (ГГГГ-ММ-ДД)",
                        padding = 0,
                        onValueChange = { lessonDate = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Комментарий
                    PersonalTextField(
                        text = comment,
                        label = "Комментарий",
                        padding = 0,
                        maxLines = 3,
                        onValueChange = { comment = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                userViewModel.clearStudents()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Отмена")
                        }

                        Button(
                            onClick = {
                                if (selectedStudentId.isNotBlank() &&
                                    selectedSubject.isNotBlank() &&
                                    selectedGrade.isNotBlank() &&
                                    selectedGradeType.isNotBlank()) {
                                    onConfirm(
                                        selectedStudentId,  // Передаем правильный ID ученика
                                        selectedSubject,
                                        selectedGrade.toInt(),
                                        selectedGradeType,
                                        comment.takeIf { it.isNotBlank() },
                                        lessonDate
                                    )
                                    userViewModel.clearStudents()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            enabled = selectedStudentId.isNotBlank() &&
                                    selectedSubject.isNotBlank() &&
                                    selectedGrade.isNotBlank() &&
                                    selectedGradeType.isNotBlank()
                        ) {
                            Text("Добавить")
                        }
                    }
                }
            }
        }
    }
}