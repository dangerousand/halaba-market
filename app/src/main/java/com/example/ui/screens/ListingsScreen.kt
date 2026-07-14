package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ListingEntity
import com.example.ui.MainViewModel
import com.example.ui.components.ShimmerPlaceholder
import com.example.ui.theme.*

@Composable
fun ListingsScreen(viewModel: MainViewModel) {
    val listings by viewModel.allListings.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    var isVoiceActive by remember { mutableStateOf(false) }

    // Kebeles list
    val kebeles = listOf("All", "Kebele 01", "Kebele 02", "Kebele 03", "Kebele 04", "Kebele 05")
    val categories = listOf("All", "Land", "Houses", "Apartments", "Rentals", "Bajaj", "Spices", "Livestock")

    // Filter Logic client-side
    val filteredListings = listings.filter { item ->
        val matchQuery = item.title.contains(viewModel.searchQuery, ignoreCase = true) || 
                         item.description.contains(viewModel.searchQuery, ignoreCase = true)
        val matchCategory = viewModel.selectedCategory == "All" || item.category == viewModel.selectedCategory
        val matchKebele = viewModel.selectedKebele == "All" || item.kebele == viewModel.selectedKebele
        
        val minPrice = viewModel.minPriceQuery.toDoubleOrNull() ?: 0.0
        val maxPrice = viewModel.maxPriceQuery.toDoubleOrNull() ?: Double.MAX_VALUE
        val matchPrice = item.price in minPrice..maxPrice
        
        val matchVerified = !viewModel.filterOnlyVerified || item.isVerified
        
        // Ensure approved and not sold
        val activeAndApproved = item.isApproved && !item.isSold

        matchQuery && matchCategory && matchKebele && matchPrice && matchVerified && activeAndApproved
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Filter Header
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = { Text("Search land, spices, bajaj...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (viewModel.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                            IconButton(onClick = {
                                isVoiceActive = true
                                // Simulated voice recognizer filling keywords
                                viewModel.searchQuery = "Red Pepper"
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice",
                                    tint = if (isVoiceActive) HalabaCrimson else HalabaSoftGreen
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HalabaSoftGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (showFilters) HalabaDarkGreen else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (showFilters) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Category Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = viewModel.selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) HalabaDarkGreen else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Expanded Filters Drawer Panel (Animated visibility)
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detailed Filters",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kebeles Selector Row
                    Text(
                        text = "Select Halaba Kebele:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        kebeles.forEach { kebele ->
                            val isSelected = viewModel.selectedKebele == kebele
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) HalabaSoftGreen else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.selectedKebele = kebele }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = kebele,
                                    fontSize = 11.sp,
                                    color = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price range inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.minPriceQuery,
                            onValueChange = { viewModel.minPriceQuery = it },
                            label = { Text("Min Price (ETB)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = viewModel.maxPriceQuery,
                            onValueChange = { viewModel.maxPriceQuery = it },
                            label = { Text("Max Price (ETB)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checkbox for verified listings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.filterOnlyVerified = !viewModel.filterOnlyVerified }
                    ) {
                        Checkbox(
                            checked = viewModel.filterOnlyVerified,
                            onCheckedChange = { viewModel.filterOnlyVerified = it },
                            colors = CheckboxDefaults.colors(checkedColor = HalabaSoftGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Only show Verified Listings (Certified by municipality)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clear Filters Link
                    Text(
                        text = "Reset Filters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HalabaCrimson,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                viewModel.selectedCategory = "All"
                                viewModel.selectedKebele = "All"
                                viewModel.searchQuery = ""
                                viewModel.minPriceQuery = ""
                                viewModel.maxPriceQuery = ""
                                viewModel.filterOnlyVerified = false
                            }
                            .padding(8.dp)
                    )
                }
            }
        }

        // Listings Result List / Grid
        if (filteredListings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No listings found matching parameters.",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredListings) { listing ->
                    ListingGridItem(listing = listing) {
                        viewModel.loadListingDetail(listing.id)
                        viewModel.currentScreenRoute = "detail"
                    }
                }
            }
        }
    }
}

@Composable
fun ListingGridItem(
    listing: ListingEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HalabaDarkGreen.copy(alpha = 0.2f),
                                HalabaSoftGreen.copy(alpha = 0.05f)
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
                Text(text = emoji, fontSize = 44.sp)

                if (listing.isVerified) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(HalabaGold)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = ObsidianDark,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = listing.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${listing.category} • ${listing.kebele}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 1.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (listing.price > 1000) "${String.format("%,.0f", listing.price)} ETB" else "${listing.price} ETB",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HalabaSoftGreen
                    )
                    
                    if (listing.isNegotiable) {
                        Text(
                            text = "Neg.",
                            fontSize = 8.sp,
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
