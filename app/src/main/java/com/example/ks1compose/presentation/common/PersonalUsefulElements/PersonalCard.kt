package com.example.ks1compose.presentation.common.PersonalUsefulElements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PersonalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(25.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .border(2.dp, borderColor, RoundedCornerShape(25.dp))
                .padding(16.dp)
        ) {
            content()
        }
    }
}