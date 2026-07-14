package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ListingEntity
import com.example.data.UserEntity
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.HalabaPatternDivider
import com.example.ui.theme.*

@Composable
fun ListingDetailScreen(viewModel: MainViewModel) {
    val listing = viewModel.selectedListing ?: return
    val allUsers by viewModel.allUsers.collectAsState()
    
    val broker = allUsers.find { it.id == listing.assignedBrokerId }
    var isFavorite by remember { mutableStateOf(false) }
    
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    // Form states for scheduling
    var meetingTitle by remember { mutableStateOf("Site Inspection: ${listing.title}") }
    var meetingDate by remember { mutableStateOf("2026-07-16") }
    var meetingTime by remember { mutableStateOf("10:00 AM") }
    var meetingNote by remember { mutableStateOf("") }

    // Form states for reviews
    var reviewRating by remember { mutableStateOf(5f) }
    var reviewComment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.currentScreenRoute = "listings" },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }

            Text(
                text = "Listing Details",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                    viewModel.toggleFavoriteListing(listing.id, isFavorite)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) HalabaCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Detail Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Visual Presentation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HalabaDarkGreen,
                                HalabaSoftGreen
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (listing.category) {
                    "Land" -> "⛰️"
                    "Houses" -> "🏠"
                    "Bajaj" -> "🛺"
                    "Spices" -> "🌶️"
                    "Livestock" -> "🐐"
                    else -> "📦"
                }
                Text(text = emoji, fontSize = 90.sp)

                // Bottom Overlay for Views/Favorites
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianDark.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "👁️ ${listing.viewCount} Views",
                        fontSize = 11.sp,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "❤️ ${listing.favoriteCount} Saved",
                        fontSize = 11.sp,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title and badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listing.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Category: ${listing.category} • Subcategory: ${listing.subcategory}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (listing.isVerified) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HalabaGold)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERIFIED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (listing.price > 1000) "${String.format("%,.0f", listing.price)} ETB" else "${listing.price} ETB",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = HalabaSoftGreen
                    )

                    if (listing.isNegotiable) {
                        Text(
                            text = "Negotiable Price",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertOrange,
                            modifier = Modifier
                                .background(AlertOrange.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Description",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = listing.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Location Details & Interactive canvas drawing map
                Text(
                    text = "Location (Kebele Match)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "📍 ${listing.location}, ${listing.kebele}, Halaba City",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HalabaSoftGreen
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Elegant simulated Canvas Map showing coordinates
                SimulatedMapBox(kebele = listing.kebele)

                Spacer(modifier = Modifier.height(20.dp))

                // Halaba Accent Line
                HalabaPatternDivider(height = 10.dp, alpha = 0.35f)

                Spacer(modifier = Modifier.height(20.dp))

                // Assigned Broker Card
                if (broker != null) {
                    Text(
                        text = "Assigned City Broker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(HalabaDarkGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🤝", fontSize = 20.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = broker.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "⭐ ${broker.rating} rating • Verified Broker",
                                        fontSize = 11.sp,
                                        color = HalabaGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = broker.bio,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.chatPartnerId = broker.id
                                        viewModel.currentScreenRoute = "chat"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HalabaSoftGreen),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Chat, "Chat", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        // Trigger a simulated phone call alert
                                        viewModel.createAppointment(
                                            title = "Simulated Phone Call",
                                            date = "Today",
                                            time = "Now",
                                            partnerUserId = broker.id,
                                            note = "Phone conversation initiated with broker."
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, HalabaSoftGreen),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HalabaSoftGreen)
                                ) {
                                    Icon(Icons.Default.Call, "Call", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Schedule site visit calendar trigger
                Button(
                    onClick = { showScheduleDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HalabaDarkGreen),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, "Schedule")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Schedule Site Inspection Visit", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Submit review button
                OutlinedButton(
                    onClick = { showReviewDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                ) {
                    Icon(Icons.Default.RateReview, "Review")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Write a Service Review", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom External Quick Connect Panel (Call, WhatsApp, Telegram links)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main call trigger
                Button(
                    onClick = {
                        // Simulated phone trigger
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HalabaCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(Icons.Default.Call, "Call")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Direct Call", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Telegram simulated link
                IconButton(
                    onClick = { /* simulated Telegram */ },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF229ED9))
                ) {
                    Text(text = "✈️", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // WhatsApp simulated link
                IconButton(
                    onClick = { /* simulated WhatsApp */ },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF25D366))
                ) {
                    Text(text = "💬", fontSize = 18.sp)
                }
            }
        }
    }

    // Scheduling Dialog
    if (showScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule Site Visit", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Coordinate an offline meeting with broker ${broker?.name ?: "Admin"} to inspect this listing.",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    OutlinedTextField(
                        value = meetingTitle,
                        onValueChange = { meetingTitle = it },
                        label = { Text("Purpose") }
                    )
                    OutlinedTextField(
                        value = meetingDate,
                        onValueChange = { meetingDate = it },
                        label = { Text("Date (YYYY-MM-DD)") }
                    )
                    OutlinedTextField(
                        value = meetingTime,
                        onValueChange = { meetingTime = it },
                        label = { Text("Time") }
                    )
                    OutlinedTextField(
                        value = meetingNote,
                        onValueChange = { meetingNote = it },
                        label = { Text("Extra Notes (Optional)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createAppointment(
                            title = meetingTitle,
                            date = meetingDate,
                            time = meetingTime,
                            partnerUserId = listing.sellerId,
                            note = meetingNote
                        )
                        showScheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HalabaSoftGreen)
                ) {
                    Text("Book Visit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text("Cancel", color = HalabaCrimson)
                }
            }
        )
    }

    // Review Writing Dialog
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write Service Review", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Help the Halaba community by rating the transaction service of seller ${listing.sellerName} or broker ${listing.assignedBrokerName}.",
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= reviewRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star Stars",
                                tint = HalabaGold,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { reviewRating = star.toFloat() }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Your Review Comment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (broker != null) {
                            viewModel.addReview(broker.id, reviewRating, reviewComment)
                        }
                        viewModel.addReview(listing.sellerId, reviewRating, reviewComment)
                        showReviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HalabaSoftGreen)
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel", color = HalabaCrimson)
                }
            }
        )
    }
}

// Simulated map drawn inside Compose to show location marker reactively
@Composable
fun SimulatedMapBox(kebele: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background grid representing Halaba city blocks
            drawRect(color = Color(0xFFE8F0E8))

            // Main city roads
            val roadBrush = Brush.linearGradient(listOf(Color.White, Color.White))
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 24f
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 24f
            )

            // Secondary roads
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height / 4f),
                end = Offset(size.width, size.height / 4f),
                strokeWidth = 12f
            )

            // Red Pepper symbol/Market Square block representation
            drawRect(
                color = HalabaSoftGreen.copy(alpha = 0.3f),
                topLeft = Offset(size.width / 4f, size.height / 3f),
                size = androidx.compose.ui.geometry.Size(80f, 60f)
            )

            // Listing marker pin
            drawCircle(
                color = HalabaCrimson,
                center = Offset(size.width / 2f + 30f, size.height / 2f - 20f),
                radius = 12f
            )
            drawCircle(
                color = HalabaGold,
                center = Offset(size.width / 2f + 30f, size.height / 2f - 20f),
                radius = 6f
            )

            // Broker Office block representation
            drawRect(
                color = HalabaGold.copy(alpha = 0.35f),
                topLeft = Offset(size.width / 2f + 80f, size.height / 3f + 10f),
                size = androidx.compose.ui.geometry.Size(50f, 50f)
            )
        }

        // Overlay text labels
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ObsidianDark.copy(alpha = 0.65f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Interactive map • $kebele Office",
                fontSize = 9.sp,
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(HalabaDarkGreen)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "GPS MATCH",
                fontSize = 8.sp,
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
