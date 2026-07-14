package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.HalabaDarkGreen
import com.example.ui.theme.HalabaGold
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel)
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: MainViewModel) {
    val currentRoute = viewModel.currentScreenRoute

    // Splash, Onboarding and Login do not show the bottom navigation bar
    val showBottomBar = currentRoute != "splash" && currentRoute != "onboarding" && currentRoute != "login"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { viewModel.currentScreenRoute = "home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HalabaDarkGreen,
                            selectedTextColor = HalabaDarkGreen,
                            indicatorColor = HalabaGold.copy(alpha = 0.35f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "listings",
                        onClick = { viewModel.currentScreenRoute = "listings" },
                        icon = { Icon(Icons.Default.List, contentDescription = "Browse") },
                        label = { Text("Browse", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HalabaDarkGreen,
                            selectedTextColor = HalabaDarkGreen,
                            indicatorColor = HalabaGold.copy(alpha = 0.35f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "schedule",
                        onClick = { viewModel.currentScreenRoute = "schedule" },
                        icon = { Icon(Icons.Default.Event, contentDescription = "Visits") },
                        label = { Text("Visits", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HalabaDarkGreen,
                            selectedTextColor = HalabaDarkGreen,
                            indicatorColor = HalabaGold.copy(alpha = 0.35f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { viewModel.currentScreenRoute = "dashboard" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Portal") },
                        label = { Text("Portal", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HalabaDarkGreen,
                            selectedTextColor = HalabaDarkGreen,
                            indicatorColor = HalabaGold.copy(alpha = 0.35f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "ai_chat",
                        onClick = { viewModel.currentScreenRoute = "ai_chat" },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                        label = { Text("AI Assist", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HalabaDarkGreen,
                            selectedTextColor = HalabaDarkGreen,
                            indicatorColor = HalabaGold.copy(alpha = 0.35f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Transitions
            when (currentRoute) {
                "splash" -> SplashScreen(viewModel)
                "onboarding" -> OnboardingScreen(viewModel)
                "login" -> LoginScreen(viewModel)
                "home" -> HomeScreen(viewModel)
                "listings" -> ListingsScreen(viewModel)
                "detail" -> ListingDetailScreen(viewModel)
                "chat" -> ChatScreen(viewModel)
                "schedule" -> ScheduleScreen(viewModel)
                "dashboard" -> DashboardScreen(viewModel)
                "ai_chat" -> AiChatbotScreen(viewModel)
                else -> HomeScreen(viewModel)
            }
        }
    }
}
