package com.rom.poa_firstapp.ui.screen.authentication.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.graphics.Color
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
import androidx.core.app.NotificationCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rom.poa_firstapp.R
import com.rom.poa_firstapp.ui.theme.loginColor
import com.rom.poa_firstapp.ui.theme.primaryColor
import com.rom.poa_firstapp.ui.theme.secondaryColor
import com.rom.poa_firstapp.ui.theme.tertiaryColor
import kotlin.math.max


@Composable

fun LoginScreen() {
    var passwordInput by remember { mutableStateOf(TextFieldValue("")) }
    var isVisible by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
//    modifier = Modifier.fillMaxSize()
    )
    {
//    lottie animation
        LottieAnimationWidget()

        Spacer(modifier = Modifier.height(10.dp))
//    simple message
        Text(
            text = "welcome to the future!!!",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        EmailPlace()
// password input
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(150.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "password ",
            )

            Text(
                text = "Forgot password?",
                color = secondaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    // TODO: Navigate to SignUp Screen
                }
            )

        }
        Spacer(modifier = Modifier.height(5.dp))
                Password()

        Spacer(modifier = Modifier.height(30.dp))
//    button
        LoginButton()

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = loginColor
            )

            Text(
                text = "Signup",
                color = secondaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    // TODO: Navigate to SignUp Screen
                }
            )
            Spacer(modifier = Modifier.height(12.dp),)
        }

    }
}

@Composable

fun LottieAnimationWidget() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.login))
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

// The Email Section
@Composable
fun EmailPlace() {
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
                imageVector = Icons.Filled.Email,
                "Email",
                tint = secondaryColor
            )
        },
        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        label = { Text(text = "Enter Your Email") },
        placeholder = { Text(text = "eg piuskamau@gmail.com") },
        modifier = Modifier.fillMaxWidth()

    )
}
@Composable
fun TogglePassword(trailingIcon:String){
    if(trailingIcon =="OFF" ){
        println("Show password")
    }else{
        println("Hide password")
    }
}

// the password section
@Composable
fun Password() {
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
                onClick = { isVisible = !isVisible}
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_visibility_off_24),
                    contentDescription = "view password",
                    tint = secondaryColor
                )
            }

        },


        shape = RoundedCornerShape(24.dp),
        label = { Text(text = " Enter Your Password") },
        visualTransformation = if (!isVisible){ PasswordVisualTransformation() }else {
            VisualTransformation.None},
        modifier = Modifier.fillMaxWidth()


    )
}

@Composable

fun LoginButton(){

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