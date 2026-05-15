package com.example.madhusiri.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.madhusiri.viewmodel.AuthViewModel

@Composable
fun ProfileSetupScreen(viewModel: AuthViewModel, onComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    val detectedVillage by viewModel.detectedVillage.collectAsState()
    val isDetailsSaved by viewModel.isDetailsSaved.collectAsState()
    val context = LocalContext.current

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc || next }
        if (areGranted) {
            viewModel.detectVillage(context)
        }
    }

    LaunchedEffect(detectedVillage) {
        if (detectedVillage != null) {
            village = detectedVillage!!
        }
    }

    LaunchedEffect(isDetailsSaved) {
        if (isDetailsSaved) {
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Complete Your Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = village,
            onValueChange = { village = it },
            label = { Text("Locality / Village") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
                        viewModel.detectVillage(context)
                    } else {
                        launcher.launch(permissions)
                    }
                }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Detect Location")
                }
            }
        )
        Text(
            text = "Recommended: Use the location icon to auto-detect",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
        )

        val authState by viewModel.authState.collectAsState()
        if (authState is com.example.madhusiri.viewmodel.AuthState.Error) {
            Text(
                text = (authState as com.example.madhusiri.viewmodel.AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && village.isNotBlank()) {
                    viewModel.saveUserDetails(name, village)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Save & Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
