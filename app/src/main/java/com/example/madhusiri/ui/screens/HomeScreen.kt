package com.example.madhusiri.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.madhusiri.model.Hive
import com.example.madhusiri.model.SprayAlert
import com.example.madhusiri.model.UserRole
import com.example.madhusiri.viewmodel.AuthViewModel
import com.example.madhusiri.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(authViewModel: AuthViewModel, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val user by authViewModel.currentUser.collectAsState()
    val mapViewModel: MapViewModel = viewModel()
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
            // Permissions granted
        }
    }

    LaunchedEffect(Unit) {
        if (permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            launcher.launch(permissions)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Tips") },
                    label = { Text("Tips") }
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Madhu-Siri", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DashboardSection(user, mapViewModel, context)
                1 -> MapSection(user, mapViewModel)
                2 -> TipsSection()
            }
        }
    }
}

@Composable
fun DashboardSection(user: com.example.madhusiri.model.User?, viewModel: MapViewModel, context: Context) {
    val hives by viewModel.hives.collectAsState()
    val alerts by viewModel.sprayAlerts.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(
                "Welcome, ${user?.name ?: "User"}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Role: ${user?.role ?: "Unknown"}",
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (user?.role == UserRole.BEEKEEPER) {
            item {
                Text("Your Hives", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(hives.filter { it.ownerUid == user.uid }) { hive ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Hive at ${hive.latitude}, ${hive.longitude}", fontWeight = FontWeight.Medium)
                            Text("Status: ${hive.healthStatus}", fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            item {
                Text("Farmer Actions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Go to Map tab to report spraying location.")
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Spray Alerts", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(alerts) { alert ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (user?.role == UserRole.BEEKEEPER) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Farmer: ${alert.farmerName}", fontWeight = FontWeight.Bold)
                    Text(alert.message)
                    Text("Posted at: ${java.text.SimpleDateFormat("HH:mm").format(java.util.Date(alert.timestamp))}", fontSize = 12.sp)
                    
                    if (user?.role == UserRole.BEEKEEPER) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${alert.latitude},${alert.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Navigate to Spot")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapSection(user: com.example.madhusiri.model.User?, viewModel: MapViewModel) {
    val hives by viewModel.hives.collectAsState()
    val alerts by viewModel.sprayAlerts.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val defaultLocation = LatLng(12.9716, 77.5946) 
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedLatLng = latLng
                showAddDialog = true
            },
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(myLocationButtonEnabled = false)
        ) {
            hives.forEach { hive ->
                Marker(
                    state = MarkerState(position = LatLng(hive.latitude, hive.longitude)),
                    title = "Hive: ${hive.ownerName}",
                    snippet = "Status: ${hive.healthStatus}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                )
            }

            alerts.forEach { alert ->
                Marker(
                    state = MarkerState(position = LatLng(alert.latitude, alert.longitude)),
                    title = "Spraying: ${alert.farmerName}",
                    snippet = "Click to navigate",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    onInfoWindowClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${alert.latitude},${alert.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }
                )
                Circle(
                    center = LatLng(alert.latitude, alert.longitude),
                    radius = 2000.0, 
                    fillColor = Color(0x22FF0000),
                    strokeColor = Color.Red,
                    strokeWidth = 2f
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = if (user?.role == UserRole.BEEKEEPER) "Tap map to Pin Hive" else "Tap map to Report Spraying",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (user?.role == UserRole.FARMER) {
                Button(
                    onClick = {
                        viewModel.updateCurrentLocation(context)
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report at My Current Location")
                }
            }
        }
        
        FloatingActionButton(
            onClick = {
                viewModel.updateCurrentLocation(context)
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).padding(bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Center on my location")
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            val userLatLng = LatLng(it.latitude, it.longitude)
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            if (user?.role == UserRole.FARMER && selectedLatLng == null) {
                selectedLatLng = userLatLng
                showAddDialog = true
            }
        }
    }

    if (showAddDialog && selectedLatLng != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; selectedLatLng = null },
            title = { Text(if (user?.role == UserRole.BEEKEEPER) "Pin Hive Here?" else "Report Spraying Here?") },
            text = { Text("Location: ${String.format("%.4f", selectedLatLng!!.latitude)}, ${String.format("%.4f", selectedLatLng!!.longitude)}") },
            confirmButton = {
                Button(onClick = {
                    if (user?.role == UserRole.BEEKEEPER) {
                        viewModel.addHive("My Hive", selectedLatLng!!.latitude, selectedLatLng!!.longitude, user.uid, user.name)
                    } else {
                        viewModel.postSprayAlert(selectedLatLng!!.latitude, selectedLatLng!!.longitude, user?.uid ?: "", user?.name ?: "")
                    }
                    showAddDialog = false
                    selectedLatLng = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; selectedLatLng = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TipsSection() {
    val tips = listOf(
        "Use 'Bee-Friendly' pesticides like Neem oil.",
        "Avoid spraying during active foraging hours (10AM - 4PM).",
        "Keep bees inside for 4 hours after a nearby spray alert.",
        "Provide a clean water source for your bees.",
        "Communicate with local farmers to coordinate schedules."
    )

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Bee-Friendly Tips", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(tips) { tip ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(tip)
                }
            }
        }
    }
}
