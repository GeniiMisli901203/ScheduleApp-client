package com.example.ks1compose.presentation.ui.grades

import android.os.Build
import androidx.annotation.RequiresExtension
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
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalDropdown
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.domain.models.GradeUIModel
import com.example.ks1compose.data.repositories.UserRepository
import com.example.ks1compose.presentation.ui.profile.UserViewModel

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditGradesScreen(
    userId: String,
    userViewModel: UserViewModel,
    gradeViewModel: GradeViewModel,
    onNavigateBack: () -> Unit
) {
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle() // Это все еще админ
    val myGrades by gradeViewModel.myGrades.collectAsStateWithLifecycle()
    val isLoading by gradeViewModel.isLoading.collectAsStateWithLifecycle()
    val error by gradeViewModel.error.collectAsStateWithLifecycle()

    var selectedGrade by remember { mutableStateOf<GradeUIModel?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var studentName by remember { mutableStateOf("") }

    // Загружаем информацию о выбранном ученике
    LaunchedEffect(userId) {
        // Загружаем информацию о пользователе по ID
        when (val result = userViewModel.getUserById(userId)) {
            is UserRepository.Result.Success -> {
                studentName = "${result.data.name} ${result.data.sName}"
            }
            is UserRepository.Result.Error -> {
                println("Ошибка загрузки пользователя: ${result.message}")
            }
            else -> {}
        }
        // Загружаем оценки ученика
        gradeViewModel.loadUserGrades(userId)
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
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ошибка загрузки: $error",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { gradeViewModel.loadMyGrades() }) {
                        Text("Повторить")
                    }
                }
            } else if (myGrades.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Grade,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "У ученика пока нет оценок",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(myGrades) { grade ->
                        EditableGradeCard(
                            grade = grade,
                            onEdit = { selectedGrade = grade; showEditDialog = true },
                            onDelete = {
                                gradeViewModel.deleteGrade(grade.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Диалог редактирования оценки
    if (showEditDialog && selectedGrade != null) {
        EditGradeDialog(
            grade = selectedGrade!!,
            onDismiss = { showEditDialog = false },
            onSave = { gradeId, newValue, newComment ->
                gradeViewModel.updateGrade(gradeId, newValue, newComment)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditableGradeCard(
    grade: GradeUIModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Оценка
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = grade.color.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = grade.gradeValue.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = grade.color
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Информация
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = grade.subjectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${grade.gradeType} • ${grade.date}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (grade.comment != null) {
                    Text(
                        text = grade.comment,
                        fontSize = 11.sp,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }

            // Кнопка с тремя точками
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Действия",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Редактировать") },
                        onClick = {
                            expanded = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Удалить", color = Color.Red) },
                        onClick = {
                            expanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditGradeDialog(
    grade: GradeUIModel,
    onDismiss: () -> Unit,
    onSave: (String, Int, String?) -> Unit
) {
    var gradeValue by remember { mutableStateOf(grade.gradeValue.toString()) }
    var gradeType by remember { mutableStateOf(grade.gradeType) }
    var comment by remember { mutableStateOf(grade.comment ?: "") }
    var subject by remember { mutableStateOf(grade.subjectName) }

    val gradeValues = listOf("2" to "2", "3" to "3", "4" to "4", "5" to "5")
    val gradeTypes = listOf(
        "homework" to "Домашняя работа",
        "classwork" to "Классная работа",
        "test" to "Контрольная работа",
        "exam" to "Экзамен"
    )
    val subjects = listOf("Математика", "Физика", "Химия", "Биология", "История", "Литература")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать оценку") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Предмет
                PersonalDropdown(
                    selectedValue = subject,
                    label = "Предмет",
                    options = subjects.map { it to it },
                    onValueChange = { subject = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Оценка
                PersonalDropdown(
                    selectedValue = gradeValue,
                    label = "Оценка",
                    options = gradeValues,
                    onValueChange = { gradeValue = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Тип оценки
                PersonalDropdown(
                    selectedValue = gradeType,
                    label = "Тип оценки",
                    options = gradeTypes,
                    onValueChange = { gradeType = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Комментарий
                PersonalTextField(
                    text = comment,
                    label = "Комментарий",
                    padding = 0,
                    maxLines = 3,
                    onValueChange = { comment = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(grade.id, gradeValue.toInt(), comment.takeIf { it.isNotBlank() })
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}