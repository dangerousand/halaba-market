package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(private val db: AppDatabase) {

    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()
    val allListings: Flow<List<ListingEntity>> = db.listingDao().getAllListings()
    val activeListings: Flow<List<ListingEntity>> = db.listingDao().getApprovedActiveListings()
    val allAds: Flow<List<AdEntity>> = db.adDao().getAllAds()
    val allMeetings: Flow<List<MeetingEntity>> = db.meetingDao().getAllMeetings()
    val allReviews: Flow<List<ReviewEntity>> = db.reviewDao().getAllReviews()

    init {
        // Pre-populate data asynchronously on startup if empty
        CoroutineScope(Dispatchers.IO).launch {
            populateInitialData()
        }
    }

    // --- Users ---
    suspend fun getUserById(id: String): UserEntity? = db.userDao().getUserById(id)
    suspend fun getUserByEmail(email: String): UserEntity? = db.userDao().getUserByEmail(email)
    fun getUsersByRole(role: String): Flow<List<UserEntity>> = db.userDao().getUsersByRole(role)
    suspend fun insertUser(user: UserEntity) = db.userDao().insertUser(user)
    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)
    suspend fun verifyUser(id: String, isVerified: Boolean) = db.userDao().verifyUser(id, isVerified)
    suspend fun deleteUser(id: String) = db.userDao().deleteUser(id)

    // --- Listings ---
    suspend fun getListingById(id: String): ListingEntity? = db.listingDao().getListingById(id)
    fun getListingsByCategory(category: String): Flow<List<ListingEntity>> = db.listingDao().getListingsByCategory(category)
    fun getListingsBySeller(sellerId: String): Flow<List<ListingEntity>> = db.listingDao().getListingsBySeller(sellerId)
    fun getListingsByBroker(brokerId: String): Flow<List<ListingEntity>> = db.listingDao().getListingsByBroker(brokerId)
    suspend fun insertListing(listing: ListingEntity) = db.listingDao().insertListing(listing)
    suspend fun approveListing(id: String, isApproved: Boolean) = db.listingDao().approveListing(id, isApproved)
    suspend fun markListingSold(id: String, isSold: Boolean) = db.listingDao().markListingSold(id, isSold)
    suspend fun incrementViewCount(id: String) = db.listingDao().incrementViewCount(id)
    suspend fun updateFavoriteCount(id: String, delta: Int) = db.listingDao().updateFavoriteCount(id, delta)
    suspend fun deleteListing(id: String) = db.listingDao().deleteListing(id)

    // --- Messaging ---
    fun getChatHistory(user1: String, user2: String): Flow<List<MessageEntity>> = db.messageDao().getChatHistory(user1, user2)
    suspend fun sendMessage(message: MessageEntity) = db.messageDao().insertMessage(message)
    suspend fun markMessagesAsRead(receiverId: String, senderId: String) = db.messageDao().markMessagesAsRead(receiverId, senderId)
    suspend fun deleteMessage(messageId: String) = db.messageDao().deleteMessage(messageId)

    // --- Calendar & Meetings ---
    fun getMeetingsForUser(userId: String): Flow<List<MeetingEntity>> = db.meetingDao().getMeetingsForUser(userId)
    suspend fun getMeetingById(meetingId: String): MeetingEntity? = db.meetingDao().getMeetingById(meetingId)
    suspend fun scheduleMeeting(meeting: MeetingEntity) = db.meetingDao().insertMeeting(meeting)
    suspend fun updateMeetingStatus(meetingId: String, status: String) = db.meetingDao().updateMeetingStatus(meetingId, status)
    suspend fun deleteMeeting(meetingId: String) = db.meetingDao().deleteMeeting(meetingId)

    // --- Reviews ---
    fun getReviewsForUser(userId: String): Flow<List<ReviewEntity>> = db.reviewDao().getReviewsForUser(userId)
    suspend fun submitReview(review: ReviewEntity) {
        db.reviewDao().insertReview(review)
        val avg = db.reviewDao().getAverageRating(review.revieweeId)
        if (avg != null) {
            db.userDao().updateUserRating(review.revieweeId, avg)
        }
    }

    // --- Notifications ---
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> = db.notificationDao().getNotificationsForUser(userId)
    suspend fun insertNotification(notification: NotificationEntity) = db.notificationDao().insertNotification(notification)
    suspend fun markNotificationsAsRead(userId: String) = db.notificationDao().markAllAsRead(userId)
    suspend fun markNotificationAsRead(id: String) = db.notificationDao().markAsRead(id)

    // --- Advertisements ---
    suspend fun insertAd(ad: AdEntity) = db.adDao().insertAd(ad)
    suspend fun deleteAd(id: String) = db.adDao().deleteAd(id)

    // --- Data Pre-population for Halaba City ---
    private suspend fun populateInitialData() = withContext(Dispatchers.IO) {
        // Check if database is populated already by querying users
        val currentUsers = db.userDao().getAllUsers().firstOrNull()
        if (!currentUsers.isNullOrEmpty()) return@withContext

        // 1. Initial Users (Admin and standard users)
        val admin = UserEntity(
            id = "admin_1",
            name = "Halaba City Admin Portal",
            role = "admin",
            phoneNumber = "+251910010101",
            email = "admin@halabamarket.com",
            bio = "Official platform regulator and moderator for Halaba Market.",
            isVerified = true,
            avatarUrl = "",
            kebele = "Kebele 01",
            password = "admin"
        )
        val broker1 = UserEntity(
            id = "broker_1",
            name = "Muluken Hailu",
            role = "user",
            phoneNumber = "+251921345678",
            email = "muluken.user@gmail.com",
            bio = "Expert in Halaba commercial plots and residential properties. Over 8 years of local experience.",
            isVerified = true,
            rating = 4.9f,
            avatarUrl = "",
            kebele = "Kebele 01",
            password = "1234"
        )
        val broker2 = UserEntity(
            id = "broker_2",
            name = "Chala Tolosa",
            role = "user",
            phoneNumber = "+251934567890",
            email = "chala.user@gmail.com",
            bio = "Specialized in Bajaj vehicles, agricultural products, and livestock negotiations.",
            isVerified = true,
            rating = 4.7f,
            avatarUrl = "",
            kebele = "Kebele 03",
            password = "1234"
        )
        val seller1 = UserEntity(
            id = "seller_1",
            name = "Alemayehu Kebede",
            role = "user",
            phoneNumber = "+251911223344",
            email = "abebe.k@gmail.com",
            bio = "Local commercial crop vendor and property owner in Halaba.",
            isVerified = true,
            rating = 4.5f,
            avatarUrl = "",
            kebele = "Kebele 02",
            password = "1234"
        )
        val seller2 = UserEntity(
            id = "seller_2",
            name = "Halaba Agro Cooperative",
            role = "user",
            phoneNumber = "+251912556677",
            email = "coop@halaba.gov.et",
            bio = "Official cooperative offering high-grade agricultural input, seeds, and wholesale red pepper.",
            isVerified = true,
            rating = 4.8f,
            avatarUrl = "",
            kebele = "Kebele 01",
            password = "1234"
        )
        val buyer1 = UserEntity(
            id = "buyer_1",
            name = "Yonas Tesfaye",
            role = "user",
            phoneNumber = "+251944556677",
            email = "buyer.demo@gmail.com",
            bio = "Local Halaba citizen looking for property and a Bajaj vehicle.",
            isVerified = true,
            rating = 5.0f,
            avatarUrl = "",
            kebele = "Kebele 01",
            password = "1234"
        )

        db.userDao().insertUser(admin)
        db.userDao().insertUser(broker1)
        db.userDao().insertUser(broker2)
        db.userDao().insertUser(seller1)
        db.userDao().insertUser(seller2)
        db.userDao().insertUser(buyer1)

        // 2. Initial Listings (Properties, Agricultural, Vehicles, Bajaj, Spices)
        val listings = listOf(
            ListingEntity(
                id = "list_1",
                title = "Prime Fertile Land near Halaba Roundabout",
                description = "Exceptional 800 sqm agricultural/commercial plot. Clean title deeds. Directly accessible from the main asphalt road, ideal for warehouses or premium agricultural storage.",
                category = "Land",
                subcategory = "Commercial Plots",
                price = 1450000.0,
                isNegotiable = true,
                location = "Near Halaba Roundabout",
                kebele = "Kebele 01",
                sellerId = "seller_1",
                sellerName = "Alemayehu Kebede",
                sellerPhone = "+251911223344",
                assignedBrokerId = "broker_1",
                assignedBrokerName = "Muluken Hailu",
                viewCount = 145,
                favoriteCount = 28,
                isVerified = true
            ),
            ListingEntity(
                id = "list_2",
                title = "Spacious Traditional Halaba House",
                description = "Beautiful G+0 residential house with 4 bedrooms, 2 bathrooms, spacious compound, security wall, and traditional garden. Located in a quiet, highly secure residential neighborhood.",
                category = "Houses",
                subcategory = "Rentals/Sales",
                price = 3200000.0,
                isNegotiable = true,
                location = "Residential Green Area",
                kebele = "Kebele 02",
                sellerId = "seller_1",
                sellerName = "Alemayehu Kebede",
                sellerPhone = "+251911223344",
                assignedBrokerId = "broker_1",
                assignedBrokerName = "Muluken Hailu",
                viewCount = 92,
                favoriteCount = 14,
                isVerified = true
            ),
            ListingEntity(
                id = "list_3",
                title = "High-Grade Halaba Red Pepper (Mitmita & Berbere)",
                description = "Premium grade, naturally sun-dried local Halaba red pepper. Known for its intense aroma, bright red color, and high pungency. Available for wholesale in 100kg sacks.",
                category = "Agricultural Products",
                subcategory = "Wholesale Agricultural",
                price = 180.0, // Per Kg
                isNegotiable = false,
                location = "Halaba Central Spices Market",
                kebele = "Kebele 01",
                sellerId = "seller_2",
                sellerName = "Halaba Agro Cooperative",
                sellerPhone = "+251912556677",
                assignedBrokerId = "broker_2",
                assignedBrokerName = "Chala Tolosa",
                viewCount = 310,
                favoriteCount = 67,
                isVerified = true
            ),
            ListingEntity(
                id = "list_4",
                title = "TVS King Deluxe Bajaj - Red Body",
                description = "Year 2024 model in pristine condition. Single owner, low mileage (12,000 km). Fully serviced with strong engine, red body, and custom decorative seats. Extremely reliable for city transport.",
                category = "Vehicles",
                subcategory = "Vehicles",
                price = 240000.0,
                isNegotiable = true,
                location = "Halaba Main Station",
                kebele = "Kebele 03",
                sellerId = "seller_1",
                sellerName = "Alemayehu Kebede",
                sellerPhone = "+251911223344",
                assignedBrokerId = "broker_2",
                assignedBrokerName = "Chala Tolosa",
                viewCount = 205,
                favoriteCount = 42,
                isVerified = true
            ),
            ListingEntity(
                id = "list_5",
                title = "Healthy Fat Heifer (Halaba Local Cattle)",
                description = "Organic fed, vaccinated, and highly healthy local breed heifer. Perfect for milk production or upcoming holiday feast. Direct delivery available within Halaba City limits.",
                category = "Livestock",
                subcategory = "Cattle",
                price = 45000.0,
                isNegotiable = true,
                location = "Halaba Livestock Market",
                kebele = "Kebele 04",
                sellerId = "seller_2",
                sellerName = "Halaba Agro Cooperative",
                sellerPhone = "+251912556677",
                assignedBrokerId = "broker_2",
                assignedBrokerName = "Chala Tolosa",
                viewCount = 85,
                favoriteCount = 9,
                isVerified = false
            ),
            ListingEntity(
                id = "list_6",
                title = "Modern Commercial Shop Space for Rent",
                description = "Newly finished 45 sqm retail boutique shop. Ground floor, double glass front, high foot traffic area right opposite Halaba Central Mall. Perfect for electronics, fashion, or pharmaceuticals.",
                category = "Houses",
                subcategory = "Shops",
                price = 15000.0, // Monthly
                isNegotiable = true,
                location = "Main Market Avenue",
                kebele = "Kebele 01",
                sellerId = "seller_1",
                sellerName = "Alemayehu Kebede",
                sellerPhone = "+251911223344",
                assignedBrokerId = "broker_1",
                assignedBrokerName = "Muluken Hailu",
                viewCount = 118,
                favoriteCount = 19,
                isVerified = true
            )
        )

        for (listing in listings) {
            db.listingDao().insertListing(listing)
        }

        // 3. Advertisements
        val ads = listOf(
            AdEntity(
                id = "ad_1",
                title = "Halaba Spices Expo 2026",
                partnerName = "Halaba Municipal Tourism Board",
                description = "Join the annual Spices & Coffee exhibition this weekend at the City Stadium! Celebrate the best red pepper in Ethiopia.",
                imageUrl = "expo",
                position = "home_top"
            ),
            AdEntity(
                id = "ad_2",
                title = "Siltie-Halaba Commercial Bank Loan Services",
                partnerName = "SHCB",
                description = "Microfinance and equipment loans for Bajaj operators and local farmers. Low rates, simple approvals.",
                imageUrl = "bank",
                position = "home_mid"
            )
        )
        for (ad in ads) {
            db.adDao().insertAd(ad)
        }

        // 4. Sample Messages
        val messages = listOf(
            MessageEntity(
                id = "msg_1",
                senderId = "seller_1",
                senderName = "Alemayehu Kebede",
                receiverId = "buyer_1",
                text = "Hello! I heard from our broker that you are interested in the Prime Land near the Roundabout. It's a great spot. Let me know when we can meet."
            ),
            MessageEntity(
                id = "msg_2",
                senderId = "broker_1",
                senderName = "Muluken Hailu",
                receiverId = "buyer_1",
                text = "Welcome! I am your assigned broker. I can help negotiate the land or the residential house with the seller. I've set up a meeting on the calendar for us."
            )
        )
        for (msg in messages) {
            db.messageDao().insertMessage(msg)
        }

        // 5. Sample Reviews
        val reviews = listOf(
            ReviewEntity(
                id = "rev_1",
                reviewerId = "buyer_1",
                reviewerName = "Yonas Tesfaye",
                revieweeId = "broker_1",
                rating = 5.0f,
                comment = "Excellent service! Muluken guided us through the entire land verification process with absolute transparency."
            ),
            ReviewEntity(
                id = "rev_2",
                reviewerId = "seller_1",
                reviewerName = "Alemayehu Kebede",
                revieweeId = "broker_2",
                rating = 4.5f,
                comment = "Chala matched our TVS Bajaj with a buyer in less than three days. Highly recommended."
            )
        )
        for (rev in reviews) {
            db.reviewDao().insertReview(rev)
        }

        // 6. Sample Meetings
        val meetings = listOf(
            MeetingEntity(
                id = "meet_1",
                title = "Site Visit: Prime Land Roundabout",
                date = "2026-07-15",
                time = "09:30 AM",
                buyerId = "buyer_1",
                sellerId = "seller_1",
                brokerId = "broker_1",
                buyerName = "Yonas Tesfaye",
                sellerName = "Alemayehu Kebede",
                brokerName = "Muluken Hailu (Broker of the Week)",
                location = "Halaba Roundabout Commercial Area",
                status = "Confirmed",
                note = "Bring copies of municipal map and land clearance certificate."
            )
        )
        for (meet in meetings) {
            db.meetingDao().insertMeeting(meet)
        }

        // 7. Initial notifications
        val notifications = listOf(
            NotificationEntity(
                id = "not_1",
                userId = "buyer_1",
                title = "Welcome to Halaba Market!",
                body = "Discover land, houses, Bajajs, livestock, and local spices directly. Halaba's local buyers and sellers are ready to connect.",
                isRead = false
            ),
            NotificationEntity(
                id = "not_2",
                userId = "buyer_1",
                title = "New Message from Alemayehu Kebede",
                body = "Alemayehu sent you a message: 'Hello! I heard from our broker...'",
                isRead = false
            )
        )
        for (not in notifications) {
            db.notificationDao().insertNotification(not)
        }
    }
}
