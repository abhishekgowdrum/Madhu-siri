package com.example.madhusiri.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madhusiri.model.UserRole
import com.example.madhusiri.viewmodel.AuthState
import com.example.madhusiri.viewmodel.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.BEEKEEPER) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Madhu-Siri",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Bee-Farmer Harmony",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (isRegistering) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Your Role:", fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedRole == UserRole.BEEKEEPER,
                        onClick = { selectedRole = UserRole.BEEKEEPER }
                    )
                    Text("Beekeeper")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedRole == UserRole.FARMER,
                        onClick = { selectedRole = UserRole.FARMER }
                    )
                    Text("Farmer")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (isRegistering) {
                            viewModel.register(email, password, selectedRole)
                        } else {
                            viewModel.login(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (isRegistering) "Register" else "Login")
                }

                TextButton(onClick = { isRegistering = !isRegistering }) {
                    Text(if (isRegistering) "Already have an account? Login" else "New here? Register")
                }
            }

            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
