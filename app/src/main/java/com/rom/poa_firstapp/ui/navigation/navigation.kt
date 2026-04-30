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

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier){
    NavHost(
        navController = navController,
        startDestination = ROUTES.Onboarding.name
    ) {
        composable(ROUTES.Onboarding.name){OnboardingScreen(navController, modifier)}
        composable(ROUTES.Login.name){LoginScreen(navController, modifier)}
        composable(ROUTES.Signup.name){SignupScreen(navController, modifier)}
        composable(ROUTES.ForgotPassword.name){ForgotPasswordScreen(navController, modifier)}
        composable(ROUTES.Home.name){HomeScreen(navController, modifier)}
    }
}
