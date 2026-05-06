package com.rom.poa_firstapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rom.poa_firstapp.data.remote.SupabaseModule
import com.rom.poa_firstapp.ui.screen.authentication.forgotpassword.ForgotPasswordScreen
import com.rom.poa_firstapp.ui.screen.authentication.login.LoginScreen
import com.rom.poa_firstapp.ui.screen.authentication.signup.SignupScreen
import com.rom.poa_firstapp.ui.screen.onboarding.OnboardingScreen
import com.rom.poa_firstapp.ui.screen.home.HomeScreen
import com.rom.poa_firstapp.ui.screen.postRide.PostRideScreen
import com.rom.poa_firstapp.ui.screen.profile.RiderProfileScreen
import com.rom.poa_firstapp.ui.screen.profile.ProfileSetupScreen
import com.rom.poa_firstapp.ui.screen.messages.MessagesScreen
import io.github.jan.supabase.auth.auth

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = ROUTES.Onboarding.name
){
    println("DEBUG: AppNavigation composing with startDestination: $startDestination")
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(ROUTES.Onboarding.name){OnboardingScreen(navController, modifier)}
        composable(ROUTES.Login.name){LoginScreen(navController, modifier)}
        composable(ROUTES.Signup.name){SignupScreen(navController, modifier)}
        composable(ROUTES.ForgotPassword.name){ForgotPasswordScreen(navController, modifier)}
        composable(ROUTES.Home.name){HomeScreen(navController, modifier)}
        composable(ROUTES.PostRide.name){PostRideScreen(navController)}
        composable(ROUTES.Profile.name) {
            val profileId = navController.currentBackStackEntry?.savedStateHandle?.get<String>("profileId")
            RiderProfileScreen(
                profileId = profileId,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(ROUTES.Login.name) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTES.Messages.name) {
            MessagesScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTES.ProfileSetup.name) {
            val userId = SupabaseModule.client.auth.currentSessionOrNull()?.user?.id
            if (userId != null) {
                ProfileSetupScreen(
                    userId = userId,
                    onProfileCreated = {
                        navController.navigate(ROUTES.Home.name) {
                            popUpTo(ROUTES.ProfileSetup.name) { inclusive = true }
                        }
                    }
                )
            } else {
                // Fallback if session is lost
                navController.navigate(ROUTES.Login.name) {
                    popUpTo(0)
                }
            }
        }
    }
}
