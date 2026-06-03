package com.example.ks1compose.presentation.common.PersonalUsefulElements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PersonalLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    size: Int = 40
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            modifier = Modifier.size(size.dp),
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun PersonalLoadingOverlay(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(50.dp),
                    strokeWidth = 3.dp
                )
            }
        }
    }
}