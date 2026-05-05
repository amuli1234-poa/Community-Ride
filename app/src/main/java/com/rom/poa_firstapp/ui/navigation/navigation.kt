package com.rom.poa_firstapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rom.poa_firstapp.ui.screen.authentication.forgotpassword.ForgotPasswordScreen
import com.rom.poa_firstapp.ui.screen.authentication.login.LoginScreen
import com.rom.poa_firstapp.ui.screen.authentication.signup.SignupScreen
import com.rom.poa_firstapp.ui.screen.onboarding.OnboardingScreen
import com.rom.poa_firstapp.ui.screen.home.HomeScreen
import com.rom.poa_firstapp.ui.screen.postRide.PostRideScreen
import com.rom.poa_firstapp.ui.screen.profile.RiderProfileScreen
import com.rom.poa_firstapp.ui.screen.messages.MessagesScreen
import com.rom.poa_firstapp.data.model.RiderProfile

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier,
    startDestination: String = ROUTES.Onboarding.name
){
    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onBack = { navController.popBackStack() }
            )
        }
        composable(ROUTES.Messages.name) {
            MessagesScreen(onBack = { navController.popBackStack() })
        }
    }
}
