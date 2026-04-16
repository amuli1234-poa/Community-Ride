package com.rom.poa_firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rom.poa_firstapp.ui.screen.authentication.forgotpassword.ForgotPasswordScreen
import com.rom.poa_firstapp.ui.screen.authentication.login.LoginScreen
import com.rom.poa_firstapp.ui.screen.authentication.signup.SignupScreen
import com.rom.poa_firstapp.ui.theme.Poa_firstappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Poa_firstappTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding->
             LoginScreen()
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Poa_firstappTheme {


    }
}
