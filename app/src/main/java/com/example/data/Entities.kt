package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String, // "buyer", "seller", "broker", "admin"
    val phoneNumber: String,
    val email: String,
    val bio: String,
    val isVerified: Boolean = false,
    val rating: Float = 5.0f,
    val avatarUrl: String = "",
    val kebele: String = "Kebele 01"
)

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val subcategory: String,
    val price: Double,
    val isNegotiable: Boolean = true,
    val location: String = "Halaba City",
    val kebele: String = "Kebele 01",
    val latitude: Double = 7.3114,
    val longitude: Double = 37.9867,
    val imageUrls: String = "", // Comma-separated image descriptors or references
    val videoUrl: String = "",
    val sellerId: String,
    val sellerName: String,
    val assignedBrokerId: String = "",
    val assignedBrokerName: String = "",
    val datePosted: Long = System.currentTimeMillis(),
    val viewCount: Int = 0,
    val favoriteCount: Int = 0,
    val isVerified: Boolean = false,
    val isApproved: Boolean = true,
    val isSold: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val text: String,
    val imageUrl: String = "",
    val voiceUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: String, // e.g., "2026-07-12"
    val time: String, // e.g., "10:30 AM"
    val buyerId: String,
    val sellerId: String,
    val brokerId: String,
    val buyerName: String,
    val sellerName: String,
    val brokerName: String,
    val location: String = "Halaba City Center",
    val status: String = "Pending", // "Pending", "Confirmed", "Completed", "Cancelled"
    val note: String = ""
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val reviewerId: String,
    val reviewerName: String,
    val revieweeId: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "advertisements")
data class AdEntity(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String,
    val partnerName: String,
    val description: String,
    val position: String = "home_top" // "home_top", "home_mid", "search_sidebar"
)
