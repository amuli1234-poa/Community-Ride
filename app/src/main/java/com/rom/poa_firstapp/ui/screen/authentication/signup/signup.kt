package com.rom.poa_firstapp.ui.screen.authentication.signup

import android.graphics.Paint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rom.poa_firstapp.R
import com.rom.poa_firstapp.ui.theme.primaryColor
import com.rom.poa_firstapp.ui.theme.secondaryColor
import com.rom.poa_firstapp.ui.theme.tertiaryColor

@Composable

fun SignupScreen(){
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Lottie Animation
        LottieSignup()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Sign Up To Continue",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
    Spacer(modifier = Modifier.height(20.dp))

    FirstName()

    Spacer(modifier = Modifier.height(20.dp))

    MiddleName()

    Spacer(modifier = Modifier.height(20.dp))

    LastName()

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Create A Strong Password",
        style = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    )

    Spacer(modifier = Modifier.height(20.dp))

    CreatePassword()

    Spacer(modifier = Modifier.height(20.dp))

    ConfirmPassword()

}


// the lottie animation Section
@Composable

fun LottieSignup() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.signup))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        Modifier.height(400.dp)
    )
}


// The First name Section
@Composable
fun FirstName() {
    var textInput by remember { mutableStateOf(TextFieldValue("")) }
    OutlinedTextField(
        value = textInput,
        onValueChange = { textInput = it },
        maxLines = 1,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = tertiaryColor,
            unfocusedBorderColor = primaryColor
        ),

        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        label = { Text(text = "Enter Your First Name") },
        placeholder = { Text(text = "eg Peter") },
        modifier = Modifier.fillMaxWidth()

    )
}


// The Middle name section
@Composable
fun MiddleName() {
    var textInput by remember { mutableStateOf(TextFieldValue("")) }
    OutlinedTextField(
        value = textInput,
        onValueChange = { textInput = it },
        maxLines = 1,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = tertiaryColor,
            unfocusedBorderColor = primaryColor
        ),

        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        label = { Text(text = "Enter Your Middle Name") },
        placeholder = { Text(text = "eg Amuli") },
        modifier = Modifier.fillMaxWidth()

    )
}

// The Last name Section
@Composable
fun LastName() {
    var textInput by remember { mutableStateOf(TextFieldValue("")) }
    OutlinedTextField(
        value = textInput,
        onValueChange = { textInput = it },
        maxLines = 1,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = tertiaryColor,
            unfocusedBorderColor = primaryColor
        ),

        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        label = { Text(text = "Enter Your Last Name") },
        placeholder = { Text(text = "eg Gift") },
        modifier = Modifier.fillMaxWidth()

    )
}

// the Create password Section


@Composable
fun CreatePassword() {
    var textInput by remember { mutableStateOf(TextFieldValue("")) }
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
                onClick = {}
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_visibility_off_24),
                    contentDescription = "view password",
                    tint = secondaryColor
                )
            }

        },

        shape = RoundedCornerShape(24.dp),
        label = { Text(text = " Password") },
        placeholder = { Text(text = "****************") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()


    )
}
// the confirm password Section


// the password section
@Composable
fun ConfirmPassword() {
    var textInput by remember { mutableStateOf(TextFieldValue("")) }
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
                onClick = {}
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_visibility_off_24),
                    contentDescription = "view password",
                    tint = secondaryColor
                )
            }

        },

        shape = RoundedCornerShape(24.dp),
        label = { Text(text = " Confirm Your Password") },
        placeholder = { Text(text = "****************") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()


    )
}