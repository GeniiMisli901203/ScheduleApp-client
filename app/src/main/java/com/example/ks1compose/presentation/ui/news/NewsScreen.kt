package com.example.ks1compose.presentation.ui.news

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.data.DTOs.NewsDTO
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.presentation.common.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.presentation.ui.profile.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class LinkPreview(
    val title: String?,
    val description: String?,
    val imageUrl: String?
)

@Composable
fun IdeaScreen(
    newsViewModel: NewsViewModel,
    userViewModel: UserViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToAddNews: () -> Unit
) {
    val allNews by newsViewModel.allNews.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()
    val error by newsViewModel.error.collectAsStateWithLifecycle()
    val userInfo by userViewModel.userInfo.collectAsState()

    val isTeacherOrAdmin = userInfo?.role == "teacher" || userInfo?.role == "admin"
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        newsViewModel.loadAllNews()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Верхняя панель с кнопками
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Новости",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    // Кнопка поиска
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Кнопка обновления
                    IconButton(
                        onClick = { newsViewModel.loadAllNews() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Кнопка добавления (только для учителей/админов)
                    if (isTeacherOrAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onNavigateToAddNews,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Добавить",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            if (isLoading && allNews.isEmpty()) {
                PersonalLoadingIndicator()
            } else if (error != null && allNews.isEmpty()) {
                // Ошибка и нет данных
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ошибка загрузки",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PersonalButton(
                        text = "Повторить",
                        onClick = { newsViewModel.loadAllNews() },
                        widthFactor = 0.5f
                    )
                }
            } else if (allNews.isEmpty()) {
                // Нет новостей
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Новостей пока нет",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    if (isTeacherOrAdmin) {
                        Spacer(modifier = Modifier.height(16.dp))
                        PersonalButton(
                            text = "Добавить первую новость",
                            onClick = onNavigateToAddNews,
                            widthFactor = 0.7f
                        )
                    }
                }
            } else {
                // Список новостей
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allNews) { news ->
                        NewsCard(
                            news = news,
                            currentUserId = userInfo?.userId ?: "",
                            isTeacherOrAdmin = isTeacherOrAdmin,
                            onDelete = { newsId ->
                                coroutineScope.launch {
                                    TokenManager.authToken?.let { token ->
                                        newsViewModel.deleteNews(token, newsId)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Индикатор загрузки при обновлении
        if (isLoading && allNews.isNotEmpty()) {
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

@Composable
fun NewsCard(
    news: NewsDTO,
    currentUserId: String,
    isTeacherOrAdmin: Boolean,
    onDelete: (String) -> Unit
) {
    var linkPreview by remember { mutableStateOf<LinkPreview?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(news.url) {
        if (!news.url.isNullOrEmpty()) {
            linkPreview = fetchLinkPreview(news.url)
        }
    }

    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Заголовок и кнопка удаления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = news.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Кнопка удаления (только для автора или админа/учителя)
                if (isTeacherOrAdmin || news.userId == currentUserId) {
                    IconButton(
                        onClick = { onDelete(news.userId) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Описание
            Text(
                text = news.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            // Превью ссылки
            if (linkPreview != null && !news.url.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openUrl(news.url, context) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = linkPreview?.title ?: "Перейти по ссылке",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!linkPreview?.description.isNullOrEmpty()) {
                            Text(
                                text = linkPreview?.description ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                        Text(
                            text = news.url,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }
            } else if (!news.url.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = news.url,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { openUrl(news.url, context) },
                    maxLines = 1
                )
            }
        }
    }
}

private fun openUrl(url: String?, context: Context) {
    if (!url.isNullOrEmpty()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

suspend fun fetchLinkPreview(url: String): LinkPreview? {
    return withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get()

            val title = doc.title()
            val description = doc.select("meta[name=description]").attr("content")
                .takeIf { it.isNotEmpty() }
                ?: doc.select("meta[property=og:description]").attr("content")

            val imageUrl = doc.select("meta[property=og:image]").attr("content")
                .takeIf { it.isNotEmpty() }

            LinkPreview(
                title = title.takeIf { it.isNotEmpty() },
                description = description.takeIf { it.isNotEmpty() },
                imageUrl = imageUrl
            )
        } catch (e: Exception) {
            null
        }
    }
}