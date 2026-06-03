package com.example.ks1compose

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.presentation.ui.grades.AdminEditGradesScreen
import com.example.ks1compose.presentation.ui.admin.AdminScheduleScreen
import com.example.ks1compose.presentation.ui.admin.AdminScreen
import com.example.ks1compose.presentation.ui.auth.AuthViewModel
import com.example.ks1compose.presentation.ui.auth.LoginScreen
import com.example.ks1compose.presentation.ui.auth.RegistrationScreen
import com.example.ks1compose.presentation.ui.dashboard.DashboardScreen
import com.example.ks1compose.presentation.ui.grades.AddGradeScreen
import com.example.ks1compose.presentation.ui.grades.GradeDetailScreen
import com.example.ks1compose.presentation.ui.grades.GradeScreen
import com.example.ks1compose.presentation.ui.grades.GradeViewModel
import com.example.ks1compose.presentation.ui.news.AddIdeaScreen
import com.example.ks1compose.presentation.ui.news.IdeaScreen
import com.example.ks1compose.presentation.ui.news.NewsViewModel
import com.example.ks1compose.presentation.ui.news.SearchScreen
import com.example.ks1compose.presentation.ui.profile.AccountScreen
import com.example.ks1compose.presentation.ui.profile.EditProfileScreen
import com.example.ks1compose.presentation.ui.profile.UserViewModel
import com.example.ks1compose.presentation.ui.schedule.AddScheduleScreen
import com.example.ks1compose.presentation.ui.schedule.ClassStudentsScreen
import com.example.ks1compose.presentation.ui.schedule.LessonViewModel
import com.example.ks1compose.presentation.ui.schedule.ScheduleScreen
import com.example.ks1compose.presentation.ui.schedule.ScheduleViewModel
import com.example.ks1compose.presentation.ui.schedule.TeacherScheduleScreen
import com.example.ks1compose.presentation.common.DarkGrey
import com.example.ks1compose.presentation.common.KS1ComposeTheme
import com.example.ks1compose.presentation.common.LightGrey
import com.example.ks1compose.presentation.ui.factories.GradeViewModelFactory
import com.example.ks1compose.presentation.ui.factories.LessonViewModelFactory
import com.example.ks1compose.presentation.ui.factories.NewsViewModelFactory
import com.example.ks1compose.presentation.ui.factories.ScheduleViewModelFactory
import com.example.ks1compose.presentation.ui.factories.UserViewModelFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        try {
            FirebaseApp.initializeApp(this)

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
                        .putString("fcm_token", token)
                        .apply()
                    Log.d("Firebase", "FCM Token: $token")
                }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Failed to initialize Firebase", e)
        }

        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(false) }
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

            // Загружаем сохраненные данные
            val savedToken = sharedPreferences.getString("token", null)
            val savedUserId = sharedPreferences.getString("userId", null)
            val savedUserLogin = sharedPreferences.getString("userLogin", null)
            val savedUserRole = sharedPreferences.getString("userRole", null)
            val savedUserName = sharedPreferences.getString("userName", null)
            val savedUserSName = sharedPreferences.getString("userSName", null) // ✅ Правильное имя

            // Восстанавливаем TokenManager
            LaunchedEffect(savedToken, savedUserId, savedUserRole) {
                if (savedToken != null) {
                    TokenManager.authToken = savedToken
                    TokenManager.userId = savedUserId
                    TokenManager.userRole = savedUserRole
                    TokenManager.userName = savedUserName
                    TokenManager.userSName = savedUserSName // ✅ Используем правильную переменную
                }
            }

            KS1ComposeTheme(darkTheme = darkTheme) {
                AppContent(
                    darkTheme = darkTheme,
                    onThemeChange = { isDarkTheme -> darkTheme = isDarkTheme },
                    sharedPreferences = sharedPreferences,
                    application = this@MainActivity.application
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    navController: NavController,
    userRole: String?,
    userName: String?
) {
    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(initial = null)

    TopAppBar(
        modifier = Modifier
            .clip(RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp))
            .background(DarkGrey),
        title = {
            Column {
                Text(
                    text = getScreenTitle(currentRoute?.destination?.route, userRole),
                    color = Color.White,
                    fontSize = 18.sp
                )
                if (currentRoute?.destination?.route == "dashboard" && userName != null) {
                    Text(
                        text = "Привет, $userName!",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        },
        actions = {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

// com.example.ks1compose.MainActivity.kt
// Основные изменения в начале AppContent

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    sharedPreferences: android.content.SharedPreferences,
    application: Application
) {
    val navController = rememberNavController()

    // Инициализация ViewModel с правильными фабриками
    // com.example.ks1compose.MainActivity.kt
// В AppContent:

    val authViewModel: AuthViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(application)  // Используем фабрику
    )
    val lessonViewModel: LessonViewModel = viewModel(
        factory = LessonViewModelFactory(application)
    )
    val gradeViewModel: GradeViewModel = viewModel(
        factory = GradeViewModelFactory(application)
    )
    val scheduleViewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModelFactory(application)
    )
    val newsViewModel: NewsViewModel = viewModel(
        factory = NewsViewModelFactory(application)
    )

    // Состояния навигации - загружаем из SharedPreferences при старте
    var token by rememberSaveable { mutableStateOf(sharedPreferences.getString("token", null)) }
    var userLogin by rememberSaveable { mutableStateOf(sharedPreferences.getString("userLogin", null)) }
    var userId by rememberSaveable { mutableStateOf(sharedPreferences.getString("userId", null)) }
    var userRole by rememberSaveable { mutableStateOf(sharedPreferences.getString("userRole", null)) }
    var userName by rememberSaveable { mutableStateOf(sharedPreferences.getString("userName", null)) }
    var userSName by rememberSaveable { mutableStateOf(sharedPreferences.getString("userSName", null)) }

    // Флаг для отслеживания, была ли уже загружена информация о пользователе
    var isUserInfoLoaded by rememberSaveable { mutableStateOf(false) }

    val currentRoute = navController.currentDestination?.route

    // При наличии токена, восстанавливаем TokenManager и загружаем информацию о пользователе
    LaunchedEffect(token) {
        if (token != null && !isUserInfoLoaded) {
            // Восстанавливаем TokenManager
            TokenManager.authToken = token
            TokenManager.userId = userId
            TokenManager.userRole = userRole
            TokenManager.userName = userName
            TokenManager.userSName = userSName

            // Загружаем актуальную информацию о пользователе
            userViewModel.loadUserInfo()
            isUserInfoLoaded = true
        }
    }

    // Следим за изменениями информации о пользователе
    val userInfo by userViewModel.userInfo.collectAsState()
    LaunchedEffect(userInfo) {
        userInfo?.let {
            userId = it.userId
            userRole = it.role
            userName = it.name
            userSName = it.sName
            TokenManager.userId = it.userId
            TokenManager.userRole = it.role
            TokenManager.userName = it.name
            TokenManager.userSName = it.sName

            // Сохраняем в SharedPreferences
            sharedPreferences.edit()
                .putString("userId", it.userId)
                .putString("userRole", it.role)
                .putString("userName", it.name)
                .putString("userSName", it.sName)
                .apply()
        }
    }

    // Определяем стартовый экран
    val startDestination = if (token != null) "dashboard" else "login"

    Scaffold(
        topBar = {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != "login" && currentRoute != "registration") {
                AppBar(
                    navController = navController,
                    userRole = userRole,
                    userName = userName
                )
            }
        },
        bottomBar = {
            if (token != null && currentRoute != "login" && currentRoute != "registration") {
                AppBottomBar(
                    navController = navController,
                    userRole = userRole,
                    userLogin = userLogin,
                    userId = userId
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            // ================ Аутентификация ================
            composable("login") {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToDashboard = { newToken, newUserId, newRole, newLogin, newName, newSName ->
                        // Сохраняем все данные
                        token = newToken
                        userId = newUserId
                        userRole = newRole
                        userLogin = newLogin
                        userName = newName
                        userSName = newSName

                        // Обновляем TokenManager
                        TokenManager.authToken = newToken
                        TokenManager.userId = newUserId
                        TokenManager.userRole = newRole
                        TokenManager.userName = newName
                        TokenManager.userSName = newSName

                        // Сохраняем в SharedPreferences
                        sharedPreferences.edit()
                            .putString("token", newToken)
                            .putString("userId", newUserId)
                            .putString("userLogin", newLogin)
                            .putString("userRole", newRole)
                            .putString("userName", newName)
                            .putString("userSName", newSName)
                            .apply()

                        // Загружаем информацию о пользователе
                        userViewModel.loadUserInfo()
                        isUserInfoLoaded = true

                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegistration = { navController.navigate("registration") }
                )
            }

            composable("registration") {
                RegistrationScreen(
                    authViewModel = authViewModel,
                    onNavigateToDashboard = { newToken, newUserId, newRole, newLogin, newName, newSName ->
                        // Проверяем, что все параметры не null и не пустые
                        if (newToken.isNotEmpty() && newUserId.isNotEmpty() && newRole.isNotEmpty()) {
                            token = newToken
                            userId = newUserId
                            userRole = newRole
                            userLogin = newLogin
                            userName = newName
                            userSName = newSName

                            // Обновляем TokenManager
                            TokenManager.authToken = newToken
                            TokenManager.userId = newUserId
                            TokenManager.userRole = newRole
                            TokenManager.userName = newName
                            TokenManager.userSName = newSName

                            // Сохраняем в SharedPreferences
                            sharedPreferences.edit()
                                .putString("token", newToken)
                                .putString("userId", newUserId)
                                .putString("userLogin", newLogin)
                                .putString("userRole", newRole)
                                .putString("userName", newName)
                                .putString("userSName", newSName)
                                .apply()

                            navController.navigate("dashboard") {
                                popUpTo("registration") { inclusive = true }
                            }
                        } else {
                            Log.e(
                                "MainActivity",
                                "Invalid registration response: token=$newToken, userId=$newUserId, role=$newRole"
                            )
                        }
                    },
                    onNavigateToLogin = { navController.navigate("login") }
                )
            }


            // ================ Главный экран ================
            composable("dashboard") {
                DashboardScreen(
                    userViewModel = userViewModel,
                    gradeViewModel = gradeViewModel,
                    lessonViewModel = lessonViewModel,
                    newsViewModel = newsViewModel,
                    onNavigateToSchedule = { navController.navigate("schedule") },
                    onNavigateToGrades = { navController.navigate("grades") },
                    onNavigateToNews = { navController.navigate("ideas") },
                    onNavigateToAdmin = { navController.navigate("admin") }
                )
            }

            // ================ Новости ================
            composable("ideas") {
                IdeaScreen(
                    newsViewModel = newsViewModel,
                    userViewModel = userViewModel,
                    onNavigateToSearch = { navController.navigate("search") },
                    onNavigateToAddNews = { navController.navigate("addIdea") }
                )
            }

            composable("search") {
                SearchScreen(
                    newsViewModel = newsViewModel,
                    onNavigateBack = { navController.navigateUp() },
                    currentUserId = userId ?: ""
                )
            }

            composable("addIdea") {
                AddIdeaScreen(
                    newsViewModel = newsViewModel,
                    onIdeaAdded = { navController.navigateUp() },
                    token = token ?: ""
                )
            }

            composable("teacher_schedule") {
                TeacherScheduleScreen(
                    lessonViewModel = lessonViewModel,
                    userViewModel = userViewModel
                )
            }

            // ================ Расписание ================
            composable("schedule") {
                ScheduleScreen(
                    scheduleViewModel = scheduleViewModel,
                    userViewModel = userViewModel,
                    userLogin = userLogin ?: "",
                    navController = navController  // Передаем navController
                )
            }

            composable("addSchedule") {
                AddScheduleScreen(
                    scheduleViewModel = scheduleViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            composable("adminSchedule") {
                AdminScheduleScreen(
                    scheduleViewModel = scheduleViewModel
                )
            }

            // ================ Оценки ================
            composable("grades") {
                GradeScreen(
                    gradeViewModel = gradeViewModel,
                    userViewModel = userViewModel,
                    userRole = userRole ?: "student"
                )
            }

            composable(
                "grade_detail/{gradeId}",
                arguments = listOf(navArgument("gradeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val gradeId = backStackEntry.arguments?.getString("gradeId") ?: ""
                GradeDetailScreen(
                    gradeViewModel = gradeViewModel,
                    gradeId = gradeId,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            composable("add_grade") {
                AddGradeScreen(
                    gradeViewModel = gradeViewModel,
                    userViewModel = userViewModel,
                    onGradeAdded = { navController.navigateUp() }
                )
            }

            composable(
                "edit_grades/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                AdminEditGradesScreen(
                    userId = userId,
                    userViewModel = userViewModel,
                    gradeViewModel = gradeViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // ================ Аккаунт ================
            // com.example.ks1compose.MainActivity.kt
// Найдите composable для account и исправьте:

            // com.example.ks1compose.MainActivity.kt
// Найдите composable для account и исправьте:

            composable(
                "account/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val accountUserId = backStackEntry.arguments?.getString("userId") ?: userId ?: ""
                AccountScreen(
                    userViewModel = userViewModel,
                    authViewModel = authViewModel,
                    onEditProfile = { profileUserId ->
                        navController.navigate("editProfile/$profileUserId")
                    },
                    onAddSchedule = { navController.navigate("addSchedule") },
                    onViewAllSchedules = { navController.navigate("adminSchedule") },
                    onLogout = {
                        token = null
                        // Очищаем все переменные состояния
                        userId = null
                        userLogin = null
                        userRole = null
                        userName = null
                        userSName = null

                        TokenManager.clear()

                        sharedPreferences.edit()
                            .remove("token")
                            .remove("userId")
                            .remove("userLogin")
                            .remove("userRole")
                            .remove("userName")
                            .remove("userSName")
                            .apply()

                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    userId = accountUserId,
                    darkTheme = darkTheme,
                    onThemeChange = onThemeChange
                )
            }

            composable(
                "editProfile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                EditProfileScreen(
                    userViewModel = userViewModel,
                    onProfileUpdated = {
                        // После закрытия экрана редактирования, обновляем списки в админке
                        userViewModel.loadAllStudents()
                        userViewModel.loadAllTeachers()
                        navController.navigateUp()
                    },
                    userId = userId
                )
            }

            // ================ Ученики класса (для учителя) ================
            composable(
                "class_students/{className}",
                arguments = listOf(navArgument("className") { type = NavType.StringType })
            ) { backStackEntry ->
                val className = backStackEntry.arguments?.getString("className") ?: ""
                ClassStudentsScreen(
                    userViewModel = userViewModel,
                    className = className,
                    onNavigateBack = { navController.navigateUp() },
                    onSelectStudent = { studentId ->
                        navController.navigate("add_grade?studentId=$studentId&className=$className")
                    }
                )
            }
            // В MainActivity.kt при вызове AdminScreen:
            composable("admin") {
                AdminScreen(
                    userViewModel = userViewModel,
                    gradeViewModel = gradeViewModel,
                    onNavigateBack = { navController.navigateUp() },
                    onEditProfile = { userId ->
                        println("📤 Navigating to edit profile with userId: $userId")  // Проверьте, что здесь UUID
                        navController.navigate("editProfile/$userId")
                    },
                    onEditGrades = { userId ->
                        println("📤 Navigating to edit grades with userId: $userId")
                        navController.navigate("edit_grades/$userId")
                    }
                )
            }
        }
    }
}

// com.example.ks1compose.MainActivity.kt - только измененная часть AppBottomBar

@Composable
private fun AppBottomBar(
    navController: NavController,
    userRole: String?,
    userLogin: String?,
    userId: String?
) {
    val currentRoute = navController.currentDestination?.route

    val items = listOf(
        BottomNavItem(
            route = "dashboard",
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            label = "Главная"
        ),
        BottomNavItem(
            route = "schedule",
            icon = Icons.Outlined.CalendarToday,
            selectedIcon = Icons.Filled.CalendarToday,
            label = "Расписание"
        ),
        BottomNavItem(
            route = "grades",
            icon = Icons.Outlined.Star,
            selectedIcon = Icons.Filled.Star,
            label = "Оценки"
        ),
        BottomNavItem(
            route = "ideas",
            icon = Icons.Outlined.Article,
            selectedIcon = Icons.Filled.Article,
            label = "Новости"
        ),
        BottomNavItem(
            route = if (userId != null) "account/$userId" else "account/",
            icon = Icons.Outlined.AccountCircle,
            selectedIcon = Icons.Filled.AccountCircle,
            label = "Аккаунт"
        )
    )

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clip(RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp))
            .background(LightGrey),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = when {
                    item.route.contains("account") -> currentRoute?.contains("account") == true
                    else -> currentRoute == item.route
                }

                BottomNavItem(
                    icon = if (isSelected) item.selectedIcon else item.icon,
                    label = item.label,
                    isSelected = isSelected,
                    onClick = {
                        when {
                            item.route.contains("account") && userId != null -> {
                                navController.navigate("account/$userId") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                            else -> {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
)

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(4.dp)
            .size(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1
        )
    }
}

@Composable
fun getScreenTitle(route: String?, userRole: String?): String {
    return when {
        route == "dashboard" -> "Главная"
        route == "ideas" -> "Новости"
        route?.contains("account") == true -> "Аккаунт"
        route == "schedule" -> "Расписание"
        route == "grades" -> if (userRole == "teacher") "Оценки классов" else "Мои оценки"
        route == "addSchedule" -> "Добавить расписание"
        route == "addIdea" -> "Добавить новость"
        route == "adminSchedule" -> "Все расписания"
        route == "add_grade" -> "Добавить оценку"
        route?.contains("grade_detail") == true -> "Детали оценки"
        route?.contains("editProfile") == true -> "Редактировать профиль"
        route?.contains("class_students") == true -> "Ученики класса"
        route == "search" -> "Поиск"
        route == "registration" -> "Регистрация"
        route == "login" -> "Вход"
        else -> "Расписание школы"
    }
}