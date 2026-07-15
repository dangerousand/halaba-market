package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FirebaseService {
    private const val TAG = "FirebaseService"

    /**
     * Checks if Firebase is initialized in the current application context.
     */
    fun isFirebaseInitialized(context: Context): Boolean {
        return try {
            FirebaseApp.getInstance() != null
        } catch (e: IllegalStateException) {
            false
        }
    }

    /**
     * Tries to write a listing to Google Cloud Firestore.
     */
    suspend fun uploadListingToFirestore(context: Context, listing: ListingEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized(context)) {
            Log.w(TAG, "Firebase is not initialized. Cannot upload listing.")
            return@withContext false
        }
        return@withContext try {
            val firestore = FirebaseFirestore.getInstance()
            val listingMap = hashMapOf(
                "id" to listing.id,
                "title" to listing.title,
                "description" to listing.description,
                "category" to listing.category,
                "subcategory" to listing.subcategory,
                "price" to listing.price,
                "isNegotiable" to listing.isNegotiable,
                "location" to listing.location,
                "kebele" to listing.kebele,
                "latitude" to listing.latitude,
                "longitude" to listing.longitude,
                "imageUrls" to listing.imageUrls,
                "videoUrl" to listing.videoUrl,
                "sellerId" to listing.sellerId,
                "sellerName" to listing.sellerName,
                "sellerPhone" to listing.sellerPhone,
                "assignedBrokerId" to listing.assignedBrokerId,
                "assignedBrokerName" to listing.assignedBrokerName,
                "datePosted" to listing.datePosted,
                "viewCount" to listing.viewCount,
                "favoriteCount" to listing.favoriteCount,
                "isVerified" to listing.isVerified,
                "isApproved" to listing.isApproved,
                "isSold" to listing.isSold
            )
            
            var success = false
            firestore.collection("listings").document(listing.id)
                .set(listingMap, SetOptions.merge())
                .addOnSuccessListener { 
                    success = true
                    Log.d(TAG, "Successfully synced listing ${listing.id}") 
                }
                .addOnFailureListener { e -> 
                    Log.e(TAG, "Failed to sync listing", e) 
                }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading to Firestore", e)
            false
        }
    }

    /**
     * Tries to sync a message to Google Cloud Firestore.
     */
    suspend fun uploadMessageToFirestore(context: Context, message: MessageEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized(context)) return@withContext false
        return@withContext try {
            val firestore = FirebaseFirestore.getInstance()
            val messageMap = hashMapOf(
                "id" to message.id,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "receiverId" to message.receiverId,
                "text" to message.text,
                "imageUrl" to message.imageUrl,
                "voiceUrl" to message.voiceUrl,
                "timestamp" to message.timestamp,
                "isRead" to message.isRead
            )
            firestore.collection("messages").document(message.id)
                .set(messageMap, SetOptions.merge())
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Tries to sync a user profile to Google Cloud Firestore.
     */
    suspend fun uploadUserToFirestore(context: Context, user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized(context)) return@withContext false
        return@withContext try {
            val firestore = FirebaseFirestore.getInstance()
            val userMap = hashMapOf(
                "id" to user.id,
                "name" to user.name,
                "role" to user.role,
                "phoneNumber" to user.phoneNumber,
                "email" to user.email,
                "bio" to user.bio,
                "isVerified" to user.isVerified,
                "rating" to user.rating,
                "avatarUrl" to user.avatarUrl,
                "kebele" to user.kebele
            )
            firestore.collection("users").document(user.id)
                .set(userMap, SetOptions.merge())
            true
        } catch (e: Exception) {
            false
        }
    }
}
