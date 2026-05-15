package com.example.madhusiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: com.example.madhusiri.viewmodel.AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSelection: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        if (authViewModel.isUserLoggedIn()) {
            if (authViewModel.isProfileComplete()) {
                onNavigateToHome()
            } else {
                onNavigateToSelection()
            }
        } else {
            onNavigateToLogin()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Madhu-Siri",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
