package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdEntity
import com.example.data.ListingEntity
import com.example.data.UserEntity
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.HalabaPatternDivider
import com.example.ui.components.PulseNotificationBell
import com.example.ui.theme.*

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val listings by viewModel.activeListings.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val ads by viewModel.allAds.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()
    
    val unreadNotifications = notifications.any { !it.isRead }
    val brokers = allUsers.filter { it.role == "broker" }

    val categories = listOf(
        CategoryData("Land", "⛰️", HalabaDarkGreen),
        CategoryData("Houses", "🏠", HalabaSoftGreen),
        CategoryData("Bajaj", "🛺", HalabaGold),
        CategoryData("Spices", "🌶️", HalabaCrimson),
        CategoryData("Livestock", "🐐", HalabaEarthBrown),
        CategoryData("Rentals", "🏬", InfoBlue),
        CategoryData("Electronics", "📱", AlertOrange)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Selam, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "Guest"} 👋",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Role: ${currentUser?.role?.replaceFirstChar { it.uppercase() }} • Halaba City",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Interactive Language Switcher
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            viewModel.selectedLanguage = if (viewModel.selectedLanguage == "English") "Amharic" else "English"
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (viewModel.selectedLanguage == "English") "🇬🇧 EN" else "🇪🇹 AM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                PulseNotificationBell(
                    hasNotifications = unreadNotifications,
                    onClick = { viewModel.currentScreenRoute = "notifications" }
                )
            }
        }

        // Main Scrolling Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Weather & Drying Status Widget (Simulated Halaba Culture climate)
            WeatherWidget()

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { viewModel.currentScreenRoute = "listings" }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (viewModel.selectedLanguage == "English") "Search land, houses, Bajajs..." else "መሬት፣ ቤት፣ ባጃጅ ይፈልጉ...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = HalabaSoftGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Horizontal Grid
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (viewModel.selectedLanguage == "English") "Categories" else "ምድቦች",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { cat ->
                        CategoryPill(category = cat) {
                            viewModel.selectedCategory = cat.name
                            viewModel.currentScreenRoute = "listings"
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Featured Promotional Ads Carousel
            if (ads.isNotEmpty()) {
                AdBanner(ad = ads.first())
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Featured listings
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (viewModel.selectedLanguage == "English") "Featured Listings" else "የተመረጡ እቃዎች",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HalabaSoftGreen,
                        modifier = Modifier.clickable {
                            viewModel.selectedCategory = "All"
                            viewModel.currentScreenRoute = "listings"
                        }
                    )
                }

                if (listings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No listings available.", color = TextGray)
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(listings) { item ->
                            ListingRowItem(listing = item) {
                                viewModel.loadListingDetail(item.id)
                                viewModel.currentScreenRoute = "detail"
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Traditional Pattern Accent
            HalabaPatternDivider(height = 12.dp, alpha = 0.4f)

            Spacer(modifier = Modifier.height(20.dp))

            // Verified Local Brokers section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (viewModel.selectedLanguage == "English") "Broker of the Week" else "የሳምንቱ ደላላ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (brokers.isNotEmpty()) {
                    val primaryBroker = brokers.first()
                    BrokerHighlightCard(broker = primaryBroker) {
                        viewModel.chatPartnerId = primaryBroker.id
                        viewModel.currentScreenRoute = "chat"
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WeatherWidget() {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "☀️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Halaba City • 24°C",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Optimal sun-drying conditions for Pepper! 🌶️",
                    fontSize = 11.sp,
                    color = HalabaSoftGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HalabaGold.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SPICE EXPO LIVE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlertOrange
                )
            }
        }
    }
}

@Composable
fun CategoryPill(
    category: CategoryData,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(category.color.copy(alpha = 0.1f))
            .border(1.dp, category.color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .widthIn(min = 64.dp)
    ) {
        Text(text = category.emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun AdBanner(ad: AdEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        HalabaDarkGreen,
                        HalabaSoftGreen
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(HalabaGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SPONSORED AD",
                        fontSize = 8.sp,
                        color = ObsidianDark,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ad.partnerName,
                    color = PureWhite.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ad.title,
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = ad.description,
                color = PureWhite.copy(alpha = 0.85f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ListingRowItem(
    listing: ListingEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Simulated Visual Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HalabaDarkGreen.copy(alpha = 0.4f),
                                HalabaSoftGreen.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Category Specific Large Graphic Emoji
                val emoji = when (listing.category) {
                    "Land" -> "⛰️"
                    "Houses" -> "🏠"
                    "Bajaj" -> "🛺"
                    "Spices" -> "🌶️"
                    "Livestock" -> "🐐"
                    else -> "📦"
                }
                Text(text = emoji, fontSize = 48.sp)

                // Verified Sticker
                if (listing.isVerified) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(HalabaGold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = ObsidianDark,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "VERIFIED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianDark
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = listing.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${listing.category} • ${listing.kebele}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (listing.price > 1000) "${String.format("%,.0f", listing.price)} ETB" else "${listing.price} ETB",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HalabaSoftGreen
                    )
                    
                    if (listing.isNegotiable) {
                        Text(
                            text = "Negotiable",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertOrange,
                            modifier = Modifier
                                .border(1.dp, AlertOrange.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrokerHighlightCard(
    broker: UserEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, HalabaGold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(HalabaDarkGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤝", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = broker.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Elite",
                        tint = HalabaGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = broker.bio,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "⭐ ${broker.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HalabaGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active in ${broker.kebele}",
                        fontSize = 11.sp,
                        color = HalabaSoftGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Contact",
                tint = HalabaSoftGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class CategoryData(
    val name: String,
    val emoji: String,
    val color: Color
)
