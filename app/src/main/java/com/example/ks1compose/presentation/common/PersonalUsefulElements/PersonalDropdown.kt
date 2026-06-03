package com.example.ks1compose.presentation.common.PersonalUsefulElements


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.ks1compose.presentation.common.TextFieldBGColor

@Composable
fun PersonalDropdown(
    selectedValue: String,
    label: String,
    options: List<Pair<String, String>>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val density = LocalDensity.current

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = options.find { it.first == selectedValue }?.second ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label, fontSize = 14.sp) },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Выбрать",
                        modifier = Modifier.clickable { expanded = !expanded },
                        tint = if (isError) Color.Red else Color.White
                    )
                },
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = TextFieldBGColor,
                    focusedContainerColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = TextFieldBGColor,
                    unfocusedLabelColor = Color.White,
                    focusedLabelColor = TextFieldBGColor,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    errorContainerColor = TextFieldBGColor.copy(alpha = 0.1f),
                    errorLabelColor = Color.Red,
                    errorTextColor = Color.Red
                ),
                isError = isError,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 16.dp)
            ) {
                options.forEach { (value, displayName) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onValueChange(value)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}