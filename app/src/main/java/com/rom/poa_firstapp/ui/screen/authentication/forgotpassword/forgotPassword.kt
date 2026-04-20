package com.rom.poa_firstapp.ui.screen.authentication.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rom.poa_firstapp.R
import com.rom.poa_firstapp.ui.theme.loginColor
import com.rom.poa_firstapp.ui.theme.primaryColor
import com.rom.poa_firstapp.ui.theme.secondaryColor
import com.rom.poa_firstapp.ui.theme.tertiaryColor


@Composable
fun ForgotPasswordScreen(navController: NavHostController, modifier: Modifier) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Reset Password", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))
//        new password
        var NewPassword by remember { mutableStateOf(TextFieldValue("")) }
        var visible by remember { mutableStateOf(value = false) }
        OutlinedTextField(
            value = NewPassword,
            onValueChange = { NewPassword = it },
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = tertiaryColor,
                unfocusedBorderColor = primaryColor
            ),
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_password_24),
                    contentDescription = "password",
                    tint = secondaryColor
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        visible =! visible
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.outline_visibility_off_24),
                        contentDescription = "view password",
                        tint = secondaryColor
                    )
                }

            },

            shape = RoundedCornerShape(24.dp),
            label = { Text(text = "New Password") },
            placeholder = { Text(text = "****************") },
            visualTransformation = if (!visible){PasswordVisualTransformation()}else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()


        )


        Spacer(modifier = Modifier.height(16.dp))
//    Confirm password
        var textInput by remember { mutableStateOf(TextFieldValue("")) }
        var isVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = tertiaryColor,
                unfocusedBorderColor = primaryColor
            ),
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_password_24),
                    contentDescription = "password",
                    tint = secondaryColor
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {isVisible = !isVisible}
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.outline_visibility_off_24),
                        contentDescription = "view password",
                        tint = secondaryColor
                    )
                }

            },

            shape = RoundedCornerShape(24.dp),
            label = { Text(text = "Confirm Password") },
            placeholder = { Text(text = "****************") },
            visualTransformation = if (!isVisible){ PasswordVisualTransformation()}else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()


        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = {}) {
            Text(
                text = "Login",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = tertiaryColor
                )
            )

        }

    }
}







