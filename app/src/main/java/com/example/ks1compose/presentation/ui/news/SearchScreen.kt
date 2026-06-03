package com.example.ks1compose.presentation.ui.news

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ks1compose.data.DTOs.NewsDTO
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    newsViewModel: NewsViewModel,
    onNavigateBack: () -> Unit,
    currentUserId: String
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NewsDTO>>(emptyList()) }
    var searchHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var showHistory by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val sharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    val allNews by newsViewModel.allNews.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()

    // Загрузка истории поиска - функция определена внутри
    fun loadSearchHistory() {
        val history = sharedPreferences.getString("search_history", "")
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        searchHistory = history.take(10)
    }

    // Сохранение запроса в историю
    fun saveSearchQuery(query: String) {
        val updatedHistory = listOf(query) + searchHistory.filter { it != query }
        val historyToSave = updatedHistory.take(10).joinToString(",")
        sharedPreferences.edit().putString("search_history", historyToSave).apply()
        searchHistory = updatedHistory.take(10)
    }

    // Выполнение поиска
    fun performSearch(query: String) {
        if (query.isNotBlank()) {
            isSearching = true
            keyboardController?.hide()
            saveSearchQuery(query)

            // Фильтрация новостей по запросу
            val results = allNews.filter { news ->
                news.title.contains(query, ignoreCase = true) ||
                        news.description.contains(query, ignoreCase = true)
            }
            searchResults = results
            isSearching = false
            showHistory = false
        }
    }

    // Очистка поиска
    fun clearSearch() {
        searchQuery = ""
        searchResults = emptyList()
        showHistory = true
        keyboardController?.hide()
    }

    // Загружаем историю при старте
    LaunchedEffect(Unit) {
        loadSearchHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Поиск новостей",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Поле поиска
            PersonalTextField(
                text = searchQuery,
                label = "Введите запрос...",
                padding = 0,
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (searchQuery.isNotEmpty()) Icons.Default.Clear else null,
                onTrailingIconClick = { clearSearch() },
                onValueChange = {
                    searchQuery = it
                    if (it.isEmpty()) {
                        searchResults = emptyList()
                        showHistory = true
                    }
                },
                onKeyboardDone = {
                    performSearch(searchQuery)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка поиска
            PersonalButton(
                text = "Найти",
                onClick = { performSearch(searchQuery) },
                widthFactor = 1f,
                isLoading = isSearching
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Контент
            if (isLoading && searchResults.isEmpty()) {
                PersonalLoadingIndicator()
            } else {
                when {
                    showHistory && searchHistory.isNotEmpty() -> {
                        HistorySection(
                            searchHistory = searchHistory,
                            onHistoryItemClick = { query ->
                                searchQuery = query
                                performSearch(query)
                            },
                            onClearHistory = {
                                sharedPreferences.edit().remove("search_history").apply()
                                searchHistory = emptyList()
                            }
                        )
                    }
                    searchResults.isNotEmpty() -> {
                        SearchResultsSection(
                            results = searchResults,
                            currentUserId = currentUserId
                        )
                    }
                    searchQuery.isNotEmpty() && !isSearching && !showHistory -> {
                        EmptyResultsSection()
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySection(
    searchHistory: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "История поиска",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(onClick = onClearHistory) {
                    Text(
                        text = "Очистить",
                        color = Color.Red.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            searchHistory.forEachIndexed { index, query ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHistoryItemClick(query) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = query,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
                if (index < searchHistory.size - 1) {
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SearchResultsSection(
    results: List<NewsDTO>,
    currentUserId: String
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Результаты поиска",
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
                    text = "Найдено: ${results.size}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { news ->
                NewsSearchCard(
                    news = news,
                    currentUserId = currentUserId
                )
            }
        }
    }
}

@Composable
fun EmptyResultsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ничего не найдено",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = "Попробуйте изменить запрос",
            fontSize = 14.sp,
            color = Color.Gray.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun NewsSearchCard(
    news: NewsDTO,
    currentUserId: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Открыть детали новости */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = news.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = news.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!news.url.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = news.url,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}