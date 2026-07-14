package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.HalabaPatternDivider
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    var currentPage by remember { mutableStateOf(0) }
    
    val pages = listOf(
        OnboardingPageData(
            emoji = "🌶️",
            title = "Welcome to Halaba",
            description = "The first digital portal built specifically for Halaba City, Ethiopia. Connect with local crop growers, property owners, and transits securely."
        ),
        OnboardingPageData(
            emoji = "🛺",
            title = "Verified Listings",
            description = "Browse high-quality land plots, traditional houses, TVS Bajaj vehicles, livestock, and spices. All listings are mapped to specific local Kebeles."
        ),
        OnboardingPageData(
            emoji = "🤝",
            title = "Assigned Local Brokers",
            description = "Let our top verified city brokers handle site visits, municipal paperwork clearance, and price negotiations to protect you from fraud."
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Pattern Accents
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                HalabaPatternDivider(height = 16.dp, alpha = 0.5f)
            }

            // Central Animated Page Content
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    slideInHorizontally(animationSpec = tween(400)) { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(400)) { width -> -width } + fadeOut()
                },
                label = "page_transition",
                modifier = Modifier.weight(1f)
            ) { page ->
                val data = pages[page]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = data.emoji,
                        fontSize = 80.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Text(
                        text = data.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = data.description,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Bottom Buttons & Indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (index == currentPage) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPage) HalabaSoftGreen else MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.2f
                                    )
                                )
                                .clickable { currentPage = index }
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.currentScreenRoute = "login" }
                    ) {
                        Text(
                            text = "Skip",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            if (currentPage < pages.size - 1) {
                                currentPage++
                            } else {
                                viewModel.currentScreenRoute = "login"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HalabaDarkGreen,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentPage == pages.size - 1) "Get Started" else "Next",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private data class OnboardingPageData(
    val emoji: String,
    val title: String,
    val description: String
)
