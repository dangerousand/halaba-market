package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface SupabaseApi {
    @POST("rest/v1/{table}")
    suspend fun upsertData(
        @Path("table") table: String,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=minimal",
        @Header("Content-Type") contentType: String = "application/json",
        @Body body: RequestBody
    ): retrofit2.Response<Unit>
}

object SupabaseService {
    private const val TAG = "SupabaseService"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit? by lazy {
        val url = getSupabaseUrl()
        if (url.isEmpty()) {
            Log.e(TAG, "Supabase URL is empty. Retrofit not initialized.")
            null
        } else {
            Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        }
    }

    private val api: SupabaseApi? by lazy {
        retrofit?.create(SupabaseApi::class.java)
    }

    fun getSupabaseUrl(): String {
        return try {
            val url = BuildConfig.SUPABASE_URL
            if (url.endsWith("/")) url else "$url/"
        } catch (e: Exception) {
            ""
        }
    }

    fun getSupabaseKey(): String {
        return try {
            BuildConfig.SUPABASE_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun isConfigured(): Boolean {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        return url.isNotEmpty() && url.contains("supabase") && key.isNotEmpty() && key != "PLACEHOLDER_SUPABASE_KEY"
    }

    /**
     * Helper to upsert any record to Supabase via its REST API
     */
    private suspend fun upsertToTable(table: String, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            Log.w(TAG, "Supabase is not properly configured.")
            return@withContext false
        }

        val apiService = api ?: return@withContext false
        val key = getSupabaseKey()

        try {
            val requestBody = jsonString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val response = apiService.upsertData(
                table = table,
                apiKey = key,
                authorization = "Bearer $key",
                body = requestBody
            )
            
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully upserted data to Supabase table: $table")
                true
            } else {
                Log.e(TAG, "Failed to upsert to Supabase table: $table. Error: ${response.code()} ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error upserting to Supabase table $table", e)
            false
        }
    }

    suspend fun uploadUser(user: UserEntity): Boolean {
        val json = """
            {
                "id": "${user.id}",
                "name": "${escapeJson(user.name)}",
                "role": "${user.role}",
                "phoneNumber": "${user.phoneNumber}",
                "email": "${user.email}",
                "bio": "${escapeJson(user.bio)}",
                "isVerified": ${user.isVerified},
                "rating": ${user.rating},
                "avatarUrl": "${user.avatarUrl}",
                "kebele": "${user.kebele}"
            }
        """.trimIndent()
        return upsertToTable("users", json)
    }

    suspend fun uploadListing(listing: ListingEntity): Boolean {
        val json = """
            {
                "id": "${listing.id}",
                "title": "${escapeJson(listing.title)}",
                "description": "${escapeJson(listing.description)}",
                "category": "${listing.category}",
                "subcategory": "${listing.subcategory}",
                "price": ${listing.price},
                "isNegotiable": ${listing.isNegotiable},
                "location": "${escapeJson(listing.location)}",
                "kebele": "${listing.kebele}",
                "latitude": ${listing.latitude},
                "longitude": ${listing.longitude},
                "imageUrls": "${listing.imageUrls}",
                "videoUrl": "${listing.videoUrl}",
                "sellerId": "${listing.sellerId}",
                "sellerName": "${escapeJson(listing.sellerName)}",
                "sellerPhone": "${listing.sellerPhone}",
                "assignedBrokerId": "${listing.assignedBrokerId}",
                "assignedBrokerName": "${escapeJson(listing.assignedBrokerName)}",
                "datePosted": ${listing.datePosted},
                "viewCount": ${listing.viewCount},
                "favoriteCount": ${listing.favoriteCount},
                "isVerified": ${listing.isVerified},
                "isApproved": ${listing.isApproved},
                "isSold": ${listing.isSold}
            }
        """.trimIndent()
        return upsertToTable("listings", json)
    }

    suspend fun uploadMessage(message: MessageEntity): Boolean {
        val json = """
            {
                "id": "${message.id}",
                "senderId": "${message.senderId}",
                "senderName": "${escapeJson(message.senderName)}",
                "receiverId": "${message.receiverId}",
                "text": "${escapeJson(message.text)}",
                "imageUrl": "${message.imageUrl}",
                "voiceUrl": "${message.voiceUrl}",
                "timestamp": ${message.timestamp},
                "isRead": ${message.isRead}
            }
        """.trimIndent()
        return upsertToTable("messages", json)
    }

    suspend fun uploadMeeting(meeting: MeetingEntity): Boolean {
        val json = """
            {
                "id": "${meeting.id}",
                "title": "${escapeJson(meeting.title)}",
                "date": "${meeting.date}",
                "time": "${meeting.time}",
                "buyerId": "${meeting.buyerId}",
                "sellerId": "${meeting.sellerId}",
                "brokerId": "${meeting.brokerId}",
                "buyerName": "${escapeJson(meeting.buyerName)}",
                "sellerName": "${escapeJson(meeting.sellerName)}",
                "brokerName": "${escapeJson(meeting.brokerName)}",
                "location": "${escapeJson(meeting.location)}",
                "status": "${meeting.status}",
                "note": "${escapeJson(meeting.note)}"
            }
        """.trimIndent()
        return upsertToTable("meetings", json)
    }

    suspend fun uploadReview(review: ReviewEntity): Boolean {
        val json = """
            {
                "id": "${review.id}",
                "reviewerId": "${review.reviewerId}",
                "reviewerName": "${escapeJson(review.reviewerName)}",
                "revieweeId": "${review.revieweeId}",
                "rating": ${review.rating},
                "comment": "${escapeJson(review.comment)}",
                "timestamp": ${review.timestamp}
            }
        """.trimIndent()
        return upsertToTable("reviews", json)
    }

    suspend fun uploadNotification(notification: NotificationEntity): Boolean {
        val json = """
            {
                "id": "${notification.id}",
                "userId": "${notification.userId}",
                "title": "${escapeJson(notification.title)}",
                "body": "${escapeJson(notification.body)}",
                "timestamp": ${notification.timestamp},
                "isRead": ${notification.isRead}
            }
        """.trimIndent()
        return upsertToTable("notifications", json)
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
