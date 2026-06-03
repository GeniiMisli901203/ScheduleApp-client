package com.example.ks1compose.presentation.common.PersonalUsefulElements


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ks1compose.presentation.common.ButtonBGColor
import com.example.ks1compose.presentation.common.DarkPink

// com.example.ks1compose.PersonalUsefulElements.PersonalButton.kt
@Composable
fun PersonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    widthFactor: Float = 0.7f,
    backgroundColor: Color = ButtonBGColor,
    textColor: Color = Color.White,
    fontSize: Int = 15
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(widthFactor),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = textColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = textColor,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PersonalButtonFullWidth(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    backgroundColor: Color = ButtonBGColor,
    textColor: Color = Color.White
) {
    PersonalButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        widthFactor = 1f,
        backgroundColor = backgroundColor,
        textColor = textColor
    )
}

@Composable
fun PersonalButtonOutlined(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = ButtonBGColor,
    textColor: Color = ButtonBGColor
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(0.7f),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PersonalButtonDanger(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PersonalButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        backgroundColor = DarkPink,
        widthFactor = 1f
    )
}