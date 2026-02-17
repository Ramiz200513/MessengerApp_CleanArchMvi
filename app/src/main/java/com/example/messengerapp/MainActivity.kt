package com.example.messengerapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.messengerapp.navigation.AppNavGraph
import com.example.messengerapp.ui.theme.MessengerAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.jar.Manifest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessengerAppTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ){ isGranted->
                    viewModel.fetchAndSaveFcmToken()
                }
                LaunchedEffect(Unit ) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }else{
                        viewModel.fetchAndSaveFcmToken()
                    }
                }
                val startRoute by viewModel.startDestination.collectAsState()
                if (startRoute != null) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        startDestination = startRoute!!
                    )
                }
            }
        }
    }
}
