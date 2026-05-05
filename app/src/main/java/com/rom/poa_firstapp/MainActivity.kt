package com.rom.poa_firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.ui.navigation.AppNavigation
import com.rom.poa_firstapp.ui.navigation.ROUTES
import com.rom.poa_firstapp.ui.theme.Poa_firstappTheme
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check login status synchronously from cache to determine the starting screen
        val isUserLoggedIn = SupabaseModule.client.auth.currentSessionOrNull() != null

        setContent {
            Poa_firstappTheme {
               val navController = rememberNavController()
               val startDestination = remember {
                   if (isUserLoggedIn) ROUTES.Home.name else ROUTES.Onboarding.name
               }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        AppNavigation(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                            startDestination = startDestination
                        )
                    },
                )
            }
        }
    }
}
