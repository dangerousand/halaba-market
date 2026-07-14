package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isVerified = :isVerified WHERE id = :id")
    suspend fun verifyUser(id: String, isVerified: Boolean)

    @Query("UPDATE users SET rating = :newRating WHERE id = :id")
    suspend fun updateUserRating(id: String, newRating: Float)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
}

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings WHERE isApproved = 1 AND isSold = 0 ORDER BY datePosted DESC")
    fun getApprovedActiveListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings ORDER BY datePosted DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getListingById(id: String): ListingEntity?

    @Query("SELECT * FROM listings WHERE category = :category AND isApproved = 1 AND isSold = 0 ORDER BY datePosted DESC")
    fun getListingsByCategory(category: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE sellerId = :sellerId ORDER BY datePosted DESC")
    fun getListingsBySeller(sellerId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE assignedBrokerId = :brokerId ORDER BY datePosted DESC")
    fun getListingsByBroker(brokerId: String): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    @Query("UPDATE listings SET isApproved = :isApproved WHERE id = :id")
    suspend fun approveListing(id: String, isApproved: Boolean)

    @Query("UPDATE listings SET isSold = :isSold WHERE id = :id")
    suspend fun markListingSold(id: String, isSold: Boolean)

    @Query("UPDATE listings SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementViewCount(id: String)

    @Query("UPDATE listings SET favoriteCount = favoriteCount + :delta WHERE id = :id")
    suspend fun updateFavoriteCount(id: String, delta: Int)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY timestamp ASC")
    fun getChatHistory(user1: String, user2: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :receiverId AND senderId = :senderId")
    suspend fun markMessagesAsRead(receiverId: String, senderId: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
}

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings WHERE buyerId = :userId OR sellerId = :userId OR brokerId = :userId ORDER BY date ASC, time ASC")
    fun getMeetingsForUser(userId: String): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity)

    @Query("UPDATE meetings SET status = :status WHERE id = :meetingId")
    suspend fun updateMeetingStatus(meetingId: String, status: String)

    @Query("DELETE FROM meetings WHERE id = :meetingId")
    suspend fun deleteMeeting(meetingId: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE revieweeId = :userId ORDER BY timestamp DESC")
    fun getReviewsForUser(userId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT AVG(rating) FROM reviews WHERE revieweeId = :userId")
    suspend fun getAverageRating(userId: String): Float?
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}

@Dao
interface AdDao {
    @Query("SELECT * FROM advertisements ORDER BY position")
    fun getAllAds(): Flow<List<AdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdEntity)

    @Query("DELETE FROM advertisements WHERE id = :id")
    suspend fun deleteAd(id: String)
}
