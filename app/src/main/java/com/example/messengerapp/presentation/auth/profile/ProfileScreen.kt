package com.example.messengerapp.presentation.auth.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen (
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
){
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut){
            navController.navigate("login"){
                popUpTo(0)
            }
        }
    }
    var newUsrename by remember{ mutableStateOf("")}
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ){ paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            if (state.isLoading){
                CircularProgressIndicator()
            }else{
                Box(
                    modifier = Modifier.
                        size(120.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape) ,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.user?.username?.take(1)?.uppercase() ?: "",
                        color = Color.White,
                        style = typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    label = { Text(text = "Имя пользователя") },
                    value = state.user?.username ?: "",
                    onValueChange = { newText ->
                        viewModel.handleIntent(ProfileIntent.OnNameChanged(newText))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.handleIntent(ProfileIntent.OnSaveProfile)},
                    modifier = Modifier.fillMaxWidth(0.8f),
                    enabled = (state.user?.username != newUsrename)
                ) {
                    Text("Сохранить изменения")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 6. КНОПКА ВЫЙТИ
                OutlinedButton(
                    onClick = {viewModel.handleIntent(ProfileIntent.OnSignOutClicked) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Выйти из аккаунта")
                }
            }

            if (state.error != null) {
                Text(text = state.error!!, color = Color.Red)
            }
            }
        }
    }
