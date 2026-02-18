package com.example.messengerapp.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.messengerapp.presentation.auth.chat.detail.ChatDetailScreen
import com.example.messengerapp.presentation.auth.chat.list.ChatListScreen
import com.example.messengerapp.presentation.auth.login.LoginScreen
import com.example.messengerapp.presentation.auth.profile.ProfileScreen
import com.example.messengerapp.presentation.auth.searchUser.SearchScreen
import com.example.messengerapp.presentation.auth.signup.RegistrationScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
    object Profile : Screen("profile")
    object Search : Screen("search")
}
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
){
    NavHost(navController = navController,
        startDestination = startDestination){
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            RegistrationScreen(navController)
        }
        composable(Screen.ChatList.route) {
            ChatListScreen(navController)
        }
        composable(
            route = Screen.ChatDetail.route, // Используем константу из Screen
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatDetailScreen(navController = navController)
        }
        composable(route = Screen.Profile.route){
            ProfileScreen(navController)
        }
        composable(Screen.Search.route) {

           SearchScreen(navController = navController)
        }
    }
}
