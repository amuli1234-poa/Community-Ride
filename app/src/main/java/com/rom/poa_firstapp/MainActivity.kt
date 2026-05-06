package com.rom.poa_firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.navigation.AppNavigation
import com.rom.poa_firstapp.ui.navigation.ROUTES
import com.rom.poa_firstapp.ui.theme.Poa_firstappTheme
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var startDestination by mutableStateOf<String?>(null)
        
        // Keep the splash screen on-screen until the start destination is determined
        splashScreen.setKeepOnScreenCondition {
            startDestination == null
        }

        setContent {
            Poa_firstappTheme {
                val navController = rememberNavController()
                val supabaseClient = SupabaseModule.client
                
                val profileRepository = remember { ProfileRepositoryImpl(supabaseClient) }

                LaunchedEffect(Unit) {
                    println("DEBUG: MainActivity LaunchedEffect started")
                    try {
                        val session = try {
                            supabaseClient.auth.currentSessionOrNull()
                        } catch (e: Exception) {
                            println("DEBUG: Error getting session: ${e.message}")
                            null
                        }
                        
                        println("DEBUG: Current session: $session")
                        if (session == null) {
                            println("DEBUG: No session, navigating to Onboarding")
                            startDestination = ROUTES.Onboarding.name
                        } else {
                            val userId = session.user?.id
                            println("DEBUG: Session found, user ID: $userId")
                            if (userId != null) {
                                val profile = withContext(Dispatchers.IO) {
                                    try {
                                        println("DEBUG: Fetching profile for $userId")
                                        profileRepository.getProfile(userId)
                                    } catch (e: Exception) {
                                        println("DEBUG: Error fetching profile: ${e.message}")
                                        null
                                    }
                                }
                                println("DEBUG: Profile result: $profile")
                                startDestination = if (profile != null) {
                                    ROUTES.Home.name
                                } else {
                                    ROUTES.ProfileSetup.name
                                }
                            } else {
                                startDestination = ROUTES.Onboarding.name
                            }
                        }
                    } catch (e: Exception) {
                        println("DEBUG: Critical Error in session check: ${e.message}")
                        startDestination = ROUTES.Onboarding.name
                    }
                    println("DEBUG: Final startDestination: $startDestination")
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (startDestination == null) {
                        LoadingState()
                    } else {
                        AppNavigation(
                            navController = navController,
                            modifier = Modifier.fillMaxSize(),
                            startDestination = startDestination!!
                        )
                    }
                }
            }
        }
    }
}
