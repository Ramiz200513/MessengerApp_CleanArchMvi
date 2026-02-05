package com.example.messengerapp.presentation.auth.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock // Иконка замка
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation // Для звездочек в пароле
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state.isSuccess) {
            navController.navigate("chat_list") // Раскомментируешь, когда будет экран чатов
        }

        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Вход",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = viewModel.emailInput,
                    onValueChange = { viewModel.handleIntent(LoginIntent.OnEmailChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.passwordInput,
                    onValueChange = { viewModel.handleIntent(LoginIntent.OnPaswwordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Пароль") }, // Исправил label
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), // Скрывает пароль точками
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock, // Иконка замка
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))


                Button(
                    onClick = { viewModel.handleIntent(LoginIntent.OnLoginClick) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Войти")
                }

                Spacer(modifier = Modifier.height(16.dp))


                TextButton(onClick = { navController.navigate("signup") }) {
                    Text("Нет аккаунта? Регистрация")
                }
            }
        }
    }
}