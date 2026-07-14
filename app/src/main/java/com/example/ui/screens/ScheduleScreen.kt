package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MeetingEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ScheduleScreen(viewModel: MainViewModel) {
    val meetings by viewModel.userMeetings.collectAsState()
    var selectedDate by remember { mutableStateOf("15") } // Quick calendar days filter

    val julDays = listOf(
        CalendarDay("12", "Sun"),
        CalendarDay("13", "Mon"),
        CalendarDay("14", "Tue"),
        CalendarDay("15", "Wed"),
        CalendarDay("16", "Thu"),
        CalendarDay("17", "Fri"),
        CalendarDay("18", "Sat")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (viewModel.selectedLanguage == "English") "Meeting Calendar" else "የስብሰባ የቀን መቁጠሪያ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = "Event",
                tint = HalabaSoftGreen
            )
        }

        // Horizontal Quick Week Calendar Strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(julDays) { day ->
                val isSelected = selectedDate == day.dayNumber
                val bg = if (isSelected) HalabaDarkGreen else MaterialTheme.colorScheme.surfaceVariant
                val textColor = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable { selectedDate = day.dayNumber }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .widthIn(min = 40.dp)
                ) {
                    Text(
                        text = day.dayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = day.dayNumber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // Appointments List
        if (meetings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📅", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No site visits scheduled for this date range.",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(meetings) { meeting ->
                    MeetingCard(meeting = meeting, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MeetingCard(meeting: MeetingEntity, viewModel: MainViewModel) {
    val statusColor = when (meeting.status) {
        "Confirmed" -> SuccessGreen
        "Completed" -> InfoBlue
        "Cancelled" -> HalabaCrimson
        else -> AlertOrange
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = HalabaSoftGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${meeting.date} @ ${meeting.time}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = meeting.status.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Location
            Text(
                text = meeting.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = TextGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = meeting.location,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            // Note block if present
            if (meeting.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Notes: ${meeting.note}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parties involved
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛒 Buyer: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    Text(text = meeting.buyerName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📦 Seller: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    Text(text = meeting.sellerName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🤝 Broker: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    Text(text = meeting.brokerName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Action triggers based on status and active user
            if (meeting.status == "Pending") {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.updateMeetingStatus(meeting.id, "Confirmed") },
                        colors = ButtonDefaults.buttonColors(containerColor = HalabaSoftGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.updateMeetingStatus(meeting.id, "Cancelled") },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HalabaCrimson),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HalabaCrimson),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Decline", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (meeting.status == "Confirmed") {
                // Allows marking deal completed
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.updateMeetingStatus(meeting.id, "Completed") },
                    colors = ButtonDefaults.buttonColors(containerColor = InfoBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, "Complete", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Deal/Site Visit Completed", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class CalendarDay(
    val dayNumber: String,
    val dayName: String
)
