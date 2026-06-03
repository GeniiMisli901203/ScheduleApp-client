package com.example.ks1compose.presentation.ui.grades

import android.os.Build
import androidx.annotation.RequiresExtension
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
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.domain.models.GradeUIModel
import com.example.ks1compose.data.repositories.GradeRepository
import com.example.ks1compose.presentation.ui.profile.UserViewModel

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun GradeScreen(
    gradeViewModel: GradeViewModel,
    userViewModel: UserViewModel,
    userRole: String,
    onClassClick: (String) -> Unit = {},
    onStudentClick: (String, String) -> Unit = { _, _ -> }
) {
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle()
    val myGrades by gradeViewModel.myGrades.collectAsStateWithLifecycle()
    val classGrades by gradeViewModel.classGrades.collectAsStateWithLifecycle()
    val averageGrade by gradeViewModel.averageGrade.collectAsStateWithLifecycle()
    val isLoading by gradeViewModel.isLoading.collectAsStateWithLifecycle()
    val error by gradeViewModel.error.collectAsStateWithLifecycle()
    val addGradeResult by gradeViewModel.addGradeResult.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val classList = remember {
        listOf("5А", "5Б", "6А", "6Б", "7А", "7Б", "8А", "8Б", "9А", "9Б", "10А", "10Б", "11А", "11Б")
    }

    var selectedClass by remember { mutableStateOf<String?>(null) }
    var selectedStudent by remember { mutableStateOf<String?>(null) }
    var showAddGradeDialog by remember { mutableStateOf(false) }

    // Показываем сообщение об успешном добавлении оценки
    LaunchedEffect(addGradeResult) {
        if (addGradeResult is GradeRepository.Result.Success) {
            snackbarHostState.showSnackbar(
                message = "Оценка успешно добавлена",
                duration = SnackbarDuration.Short
            )
            gradeViewModel.clearResults()
            // Обновляем список оценок если выбран класс
            if (selectedClass != null) {
                gradeViewModel.loadClassGrades(selectedClass!!)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (userRole) {
                "student" -> {
                    // Добавляем LaunchedEffect для загрузки оценок
                    LaunchedEffect(Unit) {
                        gradeViewModel.loadMyGrades()  // Загружаем оценки при входе
                    }

                    StudentGradesContent(
                        grades = myGrades,
                        averageGrade = averageGrade,
                        className = userInfo?.uClass ?: "",
                        isLoading = isLoading,
                        error = error,
                        onRefresh = {
                            gradeViewModel.loadMyGrades()  // Обновляем по кнопке
                        }
                    )
                }
                "teacher", "admin" -> {
                    // Если класс не выбран, показываем список классов
                    if (selectedClass == null) {
                        ClassListContent(
                            classList = classList,
                            isLoading = isLoading,
                            error = error,
                            onClassSelected = { className ->
                                selectedClass = className
                                gradeViewModel.loadClassGrades(className)
                                onClassClick(className)
                            },
                            onRefresh = { /* Ничего не делаем при обновлении списка классов */ }
                        )
                    } else {
                        // Показываем оценки выбранного класса
                        TeacherClassGradesContent(
                            className = selectedClass!!,
                            onBack = {
                                selectedClass = null
                                selectedStudent = null
                            },
                            onAddGradeClick = {
                                showAddGradeDialog = true
                            },
                            classGrades = classGrades,
                            isLoading = isLoading,
                            error = error,
                            onRefresh = {
                                gradeViewModel.loadClassGrades(selectedClass!!)
                            },
                            onStudentClick = onStudentClick
                        )
                    }
                }
            }
        }
    }

    // Диалог добавления оценки
    if (showAddGradeDialog && selectedClass != null) {
        AddGradeDialog(
            className = selectedClass!!,
            userViewModel = userViewModel,  // <-- Добавьте эту строку
            onDismiss = {
                showAddGradeDialog = false
                userViewModel.clearStudents() // Очищаем список учеников при закрытии
            },
            onConfirm = { studentId, subject, gradeValue, gradeType, comment, lessonDate ->
                gradeViewModel.addGrade(
                    studentId = studentId,
                    subjectName = subject,
                    className = selectedClass!!,
                    gradeValue = gradeValue,
                    gradeType = gradeType,
                    comment = comment,
                    lessonDate = lessonDate
                )
                showAddGradeDialog = false
                userViewModel.clearStudents()
            }
        )
    }
}

@Composable
fun StudentGradesContent(
    grades: List<GradeUIModel>,
    averageGrade: Double?,
    className: String,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    if (isLoading && grades.isEmpty()) {
        PersonalLoadingIndicator()
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Информационная карточка
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Средний балл",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format("%.2f", averageGrade ?: 0.0),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$className класс",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${grades.size} оценок",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }

        if (error != null) {
            ErrorMessage(
                message = error,
                onRetry = onRefresh
            )
        } else if (grades.isEmpty()) {
            EmptyGradesMessage()
        } else {
            // Группировка оценок по предметам
            val gradesBySubject = grades.groupBy { it.subjectName }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                gradesBySubject.forEach { (subject, subjectGrades) ->
                    item {
                        SubjectGradesCard(
                            subject = subject,
                            grades = subjectGrades,
                            averageGrade = subjectGrades.map { it.gradeValue }.average()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectGradesCard(
    subject: String,
    grades: List<GradeUIModel>,
    averageGrade: Double
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Заголовок предмета
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = String.format("%.2f", averageGrade),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Горизонтальный список оценок
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grades.size) { index ->
                    val grade = grades[index]
                    GradeChip(grade = grade)
                }
            }
        }
    }
}

@Composable
fun GradeChip(
    grade: GradeUIModel
) {
    Surface(
        modifier = Modifier
            .width(60.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { /* Открыть детали */ },
        color = grade.color.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = grade.gradeValue.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = grade.color
            )
            Text(
                text = grade.date.takeLast(2), // Показываем только день
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TeacherGradesContent(
    classList: List<String>,
    selectedClass: String?,
    onClassSelected: (String) -> Unit,
    selectedStudent: String?,
    onStudentSelected: (String) -> Unit,
    classGrades: List<GradeUIModel>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Список классов
        if (selectedClass == null) {
            Text(
                text = "Выберите класс",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classList) { className ->
                    ClassCard(
                        className = className,
                        onClick = { onClassSelected(className) }
                    )
                }
            }
        } else {
            // Заголовок с выбранным классом
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onClassSelected("") }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                    Text(
                        text = "$selectedClass класс",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Обновить"
                    )
                }
            }

            if (isLoading && classGrades.isEmpty()) {
                PersonalLoadingIndicator()
            } else if (error != null) {
                ErrorMessage(
                    message = error,
                    onRetry = onRefresh
                )
            } else if (classGrades.isEmpty()) {
                EmptyGradesMessage()
            } else {
                // Группировка оценок по ученикам
                val gradesByStudent = classGrades.groupBy { it.id to it.subjectName }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gradesByStudent.forEach { (studentInfo, studentGrades) ->
                        val (studentId, studentName) = studentInfo
                        item {
                            StudentGradesCard(
                                studentId = studentId,
                                studentName = studentName ?: "Ученик",
                                grades = studentGrades,
                                onClick = { onStudentSelected(studentId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassCard(
    className: String,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = className,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "$className класс",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Посмотреть оценки",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun StudentGradesCard(
    studentId: String,
    studentName: String,
    grades: List<GradeUIModel>,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = studentName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Средний балл ученика
                val avg = grades.map { it.gradeValue }.average()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = String.format("%.2f", avg),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Группировка оценок по предметам
            val gradesBySubject = grades.groupBy { it.subjectName }

            gradesBySubject.forEach { (subject, subjectGrades) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        subjectGrades.take(3).forEach { grade ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = grade.color.copy(alpha = 0.2f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = grade.gradeValue.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = grade.color
                                    )
                                }
                            }
                        }
                        if (subjectGrades.size > 3) {
                            Text(
                                text = "+${subjectGrades.size - 3}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        PersonalButton(
            text = "Повторить",
            onClick = onRetry,
            widthFactor = 0.5f
        )
    }
}

@Composable
fun EmptyGradesMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Оценок пока нет",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun TeacherClassGradesContent(
    className: String,
    onBack: () -> Unit,
    onAddGradeClick: () -> Unit,
    classGrades: List<GradeUIModel>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onStudentClick: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок с выбранным классом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
                Text(
                    text = "$className класс",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                IconButton(onClick = onAddGradeClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить оценку",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Обновить"
                    )
                }
            }
        }

        if (isLoading && classGrades.isEmpty()) {
            PersonalLoadingIndicator()
        } else if (error != null) {
            ErrorMessage(
                message = error,
                onRetry = onRefresh
            )
        } else if (classGrades.isEmpty()) {
            EmptyGradesMessage()
        } else {
            // Правильная группировка оценок по ученикам
            val gradesByStudent = classGrades.groupBy { it.studentId }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gradesByStudent.forEach { (studentId, studentGrades) ->
                    val studentName = studentGrades.firstOrNull()?.studentName ?: "Ученик"
                    item {
                        StudentGradesCard(
                            studentId = studentId,
                            studentName = studentName,
                            grades = studentGrades,
                            onClick = { onStudentClick(studentId, className) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ClassListContent(
    classList: List<String>,
    isLoading: Boolean,
    error: String?,
    onClassSelected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Выберите класс",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (error != null) {
            ErrorMessage(
                message = error,
                onRetry = onRefresh
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classList) { className ->
                    ClassCard(
                        className = className,
                        onClick = { onClassSelected(className) }
                    )
                }
            }
        }
    }
}
