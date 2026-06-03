package com.example.ks1compose.presentation.common.PersonalUsefulElements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ks1compose.presentation.common.TextFieldBGColor

@Composable
fun PersonalTextField(
    maxLines: Int = 1,
    singleLine: Boolean = true,
    text: String,
    label: String,
    padding: Int = 16,
    isError: Boolean = false,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    readOnly: Boolean = false,
    onKeyboardDone: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Visual transformation - только для полей с паролем
    val visualTransformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    // Keyboard type - специальный тип только для пароля
    val effectiveKeyboardType = when {
        isPassword -> KeyboardType.Password
        else -> keyboardType
    }


    val passwordTrailingIcon = when {
        isPassword -> {
            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
        }
        else -> null
    }


    val passwordOnClick = when {
        isPassword -> {
            { passwordVisible = !passwordVisible }
        }
        else -> null
    }


    val finalTrailingIcon = passwordTrailingIcon ?: trailingIcon
    val finalOnClick = passwordOnClick ?: onTrailingIconClick

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = text,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 14.sp, color = if (isError) Color.Red else Color.White) },
            shape = RoundedCornerShape(25.dp),
            colors = TextFieldDefaults.colors(

                unfocusedContainerColor = TextFieldBGColor,
                focusedContainerColor = TextFieldBGColor.copy(alpha = 0.9f),


                unfocusedTextColor = Color.White,
                focusedTextColor = Color.Black,


                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,


                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,


                errorContainerColor = TextFieldBGColor.copy(alpha = 0.1f),
                errorLabelColor = Color.Red,
                errorTextColor = Color.Red,
                errorIndicatorColor = Color.Red,


                cursorColor = MaterialTheme.colorScheme.primary,


                disabledContainerColor = TextFieldBGColor.copy(alpha = 0.5f),
                disabledTextColor = Color.Gray,
                disabledLabelColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padding.dp)
                .border(
                    width = if (isError) 2.dp else 1.dp,
                    color = if (isError) Color.Red else TextFieldBGColor,
                    shape = RoundedCornerShape(25.dp)
                ),
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = effectiveKeyboardType,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onKeyboardDone?.invoke()
                    keyboardController?.hide()
                }
            ),
            isError = isError,
            readOnly = readOnly,
            enabled = !readOnly,
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = finalTrailingIcon?.let {
                {
                    IconButton(
                        onClick = finalOnClick ?: {},
                        enabled = !readOnly
                    ) {
                        Icon(
                            imageVector = it,
                            contentDescription = when {
                                isPassword -> if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                                else -> null
                            },
                            tint = if (isError) Color.Red else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = when {
                    readOnly -> Color.Gray
                    isError -> Color.Red
                    else -> Color.White
                }
            ),
            placeholder = {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp)
            )
        }
    }
}


@Composable
fun PersonalTextFieldSmall(
    text: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = text,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 12.sp) },
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = TextFieldBGColor.copy(alpha = 0.1f),
                focusedContainerColor = Color.White,
                unfocusedTextColor = TextFieldBGColor,
                focusedTextColor = TextFieldBGColor,
                unfocusedLabelColor = TextFieldBGColor.copy(alpha = 0.7f),
                focusedLabelColor = TextFieldBGColor,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                errorContainerColor = TextFieldBGColor.copy(alpha = 0.05f),
                errorLabelColor = Color.Red,
                errorTextColor = Color.Red
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isError) 2.dp else 1.dp,
                    color = if (isError) Color.Red else TextFieldBGColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(15.dp)
                ),
            singleLine = true,
            isError = isError,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = if (text.isNotEmpty()) TextFieldBGColor else Color.Gray
            )
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
            )
        }
    }
}