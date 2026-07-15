package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: MainViewModel) {
    // Animation States
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(key1 = true) {
        // Run animations in parallel
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(1000, easing = EaseOutQuad)
        )
        // Hold for 2.5 seconds total
        delay(1500)
        viewModel.currentScreenRoute = "onboarding"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HalabaDarkGreen,
                        Color(0xFF0D241E),
                        ObsidianDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background Decorative traditional geometric sun
        Canvas(modifier = Modifier.size(250.dp).alpha(0.12f)) {
            val path = Path()
            val points = 16
            val outerRadius = size.width / 2f
            val innerRadius = outerRadius * 0.7f
            val center = Offset(size.width / 2f, size.height / 2f)
            
            for (i in 0 until points * 2) {
                val angle = (i * Math.PI / points).toFloat()
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val x = center.x + r * kotlin.math.cos(angle)
                val y = center.y + r * kotlin.math.sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = HalabaGold)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value).alpha(alpha.value)
        ) {
            // Cultural Badge / Icon Placeholder
            Text(
                text = "🌶️",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "HALABA MARKET",
                fontSize = 28.sp,
                color = HalabaGold,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp
            )

            Text(
                text = "Your Trusted Local Marketplace",
                fontSize = 14.sp,
                color = Color(0xFFC0CDC0),
                modifier = Modifier.padding(top = 8.dp),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(100.dp))
            
            // Subtitle indicating connection
            Text(
                text = "Buyers • Sellers • Local Market",
                fontSize = 12.sp,
                color = Color.LightGray.copy(alpha = 0.6f),
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
        }
    }
}
