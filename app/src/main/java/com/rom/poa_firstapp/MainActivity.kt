package com.rom.poa_firstapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.rom.poa_firstapp.data.model.Notification
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.data.repository.NotificationRepositoryImpl
import com.rom.poa_firstapp.data.repository.ProfileRepositoryImpl
import com.rom.poa_firstapp.ui.common.LoadingState
import com.rom.poa_firstapp.ui.navigation.AppNavigation
import com.rom.poa_firstapp.ui.navigation.ROUTES
import com.rom.poa_firstapp.ui.theme.Poa_firstappTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var currentIntent by mutableStateOf<android.content.Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()

        currentIntent = intent

        var startDestination by mutableStateOf<String?>(null)

        setContent {
            Poa_firstappTheme {
                val navController = rememberNavController()
                val supabaseClient = SupabaseModule.client

                LaunchedEffect(currentIntent, startDestination) {
                    val uri = currentIntent?.data
                    if (startDestination != null && uri?.scheme == "poaride" && uri?.host == "reset-password") {
                        // If startDestination is already ResetPassword, let NavHost handle it (cold start case)
                        if (startDestination == ROUTES.ResetPassword.name && navController.currentDestination == null) {
                            return@LaunchedEffect
                        }

                        // For warm starts or cases where we need to switch screens, wait for graph to be ready
                        repeat(10) {
                            val hasGraph = try {
                                navController.graph
                                true
                            } catch (e: Exception) {
                                false
                            }

                            if (hasGraph) {
                                if (navController.currentBackStackEntry?.destination?.route != ROUTES.ResetPassword.name) {
                                    try {
                                        navController.navigate(ROUTES.ResetPassword.name)
                                    } catch (e: Exception) {
                                        android.util.Log.e("MainActivity", "Navigation failed", e)
                                    }
                                }
                                return@LaunchedEffect
                            }
                            kotlinx.coroutines.delay(100)
                        }
                    }
                }

                val context = LocalContext.current
                
                val profileRepository = remember { ProfileRepositoryImpl(supabaseClient) }
                val notificationRepository = remember { NotificationRepositoryImpl(supabaseClient) }

                // Permission Launcher for Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(context, "Notifications disabled. You might miss booking updates.", Toast.LENGTH_SHORT).show()
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Real-time Notification Listener
                LaunchedEffect(startDestination) {
                    val userId = supabaseClient.auth.currentSessionOrNull()?.user?.id
                    if (userId != null) {
                        notificationRepository.getNotificationsFlow(userId).collectLatest { action ->
                            if (action is PostgresAction.Insert) {
                                val newNotification = action.decodeRecord<Notification>()
                                showLocalNotification(context, newNotification)
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    // Check for deep link on cold start
                    val uri = intent?.data
                    if (uri?.scheme == "poaride" && uri?.host == "reset-password") {
                        startDestination = ROUTES.ResetPassword.name
                        return@LaunchedEffect
                    }

                    try {
                        val session = supabaseClient.auth.currentSessionOrNull()
                        if (session == null) {
                            startDestination = ROUTES.Onboarding.name
                        } else {
                            val userId = session.user?.id
                            if (userId != null) {
                                val profile = withContext(Dispatchers.IO) {
                                    profileRepository.getProfile(userId)
                                }
                                startDestination = if (profile != null) ROUTES.Home.name else ROUTES.ProfileSetup.name
                            } else {
                                startDestination = ROUTES.Onboarding.name
                            }
                        }
                    } catch (e: Exception) {
                        startDestination = ROUTES.Onboarding.name
                    }
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ride Notifications"
            val descriptionText = "Notifications for bookings and price negotiations"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("ride_notifications", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showLocalNotification(context: Context, notification: Notification) {
        val builder = NotificationCompat.Builder(context, "ride_notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
    }
}
