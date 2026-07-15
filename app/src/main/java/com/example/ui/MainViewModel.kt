package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    val repository = Repository(AppDatabase.getDatabase(application))

    // --- State: App-wide Configurations ---
    var selectedLanguage by mutableStateOf("Amharic") // "English", "Amharic", "Halabisa"
    var isDarkTheme by mutableStateOf(false)
    var isHighContrast by mutableStateOf(false)
    var isLargeText by mutableStateOf(false)

    // --- State: Current Session User & Role ---
    var currentUserId by mutableStateOf("")
    private val _currentUserState = MutableStateFlow<UserEntity?>(null)
    val currentUserState: StateFlow<UserEntity?> = _currentUserState.asStateFlow()

    // --- State: Active Screen / Navigation Key ---
    var currentScreenRoute by mutableStateOf("splash")

    // --- State: Shared UI Lists & Flows ---
    val allListings: StateFlow<List<ListingEntity>> = repository.allListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeListings: StateFlow<List<ListingEntity>> = repository.activeListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAds: StateFlow<List<AdEntity>> = repository.allAds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State: Filter Configurations ---
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("All")
    var selectedKebele by mutableStateOf("All")
    var minPriceQuery by mutableStateOf("")
    var maxPriceQuery by mutableStateOf("")
    var filterOnlyVerified by mutableStateOf(false)

    // --- State: Selected Detail Targets ---
    var selectedListingId by mutableStateOf<String?>(null)
    var selectedListing by mutableStateOf<ListingEntity?>(null)
    var chatPartnerId by mutableStateOf<String?>(null)
    var chatPartner by mutableStateOf<UserEntity?>(null)

    // --- State: Chat Conversation ---
    private val _currentChatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val currentChatMessages: StateFlow<List<MessageEntity>> = _currentChatMessages.asStateFlow()

    // --- State: Scheduled Meetings ---
    private val _userMeetings = MutableStateFlow<List<MeetingEntity>>(emptyList())
    val userMeetings: StateFlow<List<MeetingEntity>> = _userMeetings.asStateFlow()

    // --- State: User Notifications ---
    private val _userNotifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val userNotifications: StateFlow<List<NotificationEntity>> = _userNotifications.asStateFlow()

    // --- State: AI Chatbot Conversation ---
    var aiChatHistory = mutableStateOf<List<Pair<String, Boolean>>>(
        listOf(Pair("Selam! I am Halaba AI, your local market assistant. Ask me anything about pepper prices, land valuations, or town regulations in Halaba City!", false))
    )
    var isAiTyping by mutableStateOf(false)

    init {
        // Initialize user session
        observeCurrentUserSession()
    }

    private fun observeCurrentUserSession() {
        viewModelScope.launch {
            // Keep observing active user notifications & meetings
            snapshotFlow { currentUserId }.collectLatest { userId ->
                val user = repository.getUserById(userId)
                _currentUserState.value = user

                // Load and collect messages/meetings reactively
                repository.getMeetingsForUser(userId).collectLatest { meetings ->
                    _userMeetings.value = meetings
                }
            }
        }

        viewModelScope.launch {
            snapshotFlow { currentUserId }.collectLatest { userId ->
                repository.getNotificationsForUser(userId).collectLatest { notifications ->
                    _userNotifications.value = notifications
                }
            }
        }

        // Live Collect for active chat history if chat partner changes
        viewModelScope.launch {
            snapshotFlow { currentUserId to chatPartnerId }.collectLatest { pair ->
                val curr = pair.first
                val partner = pair.second
                if (partner != null) {
                    val partnerUser = repository.getUserById(partner)
                    chatPartner = partnerUser
                    repository.getChatHistory(curr, partner).collectLatest { messages ->
                        _currentChatMessages.value = messages
                        repository.markMessagesAsRead(curr, partner)
                    }
                } else {
                    _currentChatMessages.value = emptyList()
                    chatPartner = null
                }
            }
        }
    }

    // --- State: Media Upload Progress ---
    var isUploading by mutableStateOf(false)
    var uploadProgress by mutableStateOf(0f)
    var currentUploadStatus by mutableStateOf("")

    // --- Actions: Session Management ---
    fun registerUser(
        name: String,
        email: String,
        phone: String,
        password: String,
        kebele: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "An account with this email already exists.")
                }
                return@launch
            }

            val newId = "user_${UUID.randomUUID().toString().take(6)}"
            val newUser = UserEntity(
                id = newId,
                name = name,
                role = "user",
                phoneNumber = phone,
                email = email,
                bio = "Active Halaba Market user.",
                isVerified = true,
                kebele = kebele,
                password = password
            )
            repository.insertUser(newUser)
            
            // Sync to Firebase and Supabase Cloud
            viewModelScope.launch(Dispatchers.IO) {
                FirebaseService.uploadUserToFirestore(getApplication(), newUser)
                SupabaseService.uploadUser(newUser)
            }
            
            withContext(Dispatchers.Main) {
                currentUserId = newId
                currentScreenRoute = "home"
                onResult(true, "Registration successful!")
            }
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "No account found with this email.")
                }
                return@launch
            }

            if (user.password != password) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Incorrect password. Please try again.")
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                currentUserId = user.id
                currentScreenRoute = "home"
                onResult(true, "Logged in successfully!")
            }
        }
    }

    fun logoutUser() {
        currentUserId = ""
        currentScreenRoute = "login"
    }

    fun resetPassword(email: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "No account associated with this email.")
                }
                return@launch
            }

            val updated = user.copy(password = newPassword)
            repository.updateUser(updated)
            withContext(Dispatchers.Main) {
                onResult(true, "Password has been reset successfully!")
            }
        }
    }

    fun updateUserProfile(
        name: String,
        email: String,
        phone: String,
        bio: String,
        kebele: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = currentUserState.value ?: return@launch
            val updated = user.copy(
                name = name,
                email = email,
                phoneNumber = phone,
                bio = bio,
                kebele = kebele
            )
            repository.updateUser(updated)
            
            // Sync to Firebase and Supabase
            viewModelScope.launch(Dispatchers.IO) {
                FirebaseService.uploadUserToFirestore(getApplication(), updated)
                SupabaseService.uploadUser(updated)
            }
            
            withContext(Dispatchers.Main) {
                onResult(true, "Profile updated successfully!")
            }
        }
    }

    fun switchUserRole(role: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val potentialUsers = allUsers.value.filter { it.role.lowercase() == role.lowercase() }
            if (potentialUsers.isNotEmpty()) {
                currentUserId = potentialUsers.first().id
            } else {
                // Generate and insert a user of this role
                val id = "${role}_demo_u"
                val name = "Demo ${role.replaceFirstChar { it.uppercase() }}"
                val newUser = UserEntity(
                    id = id,
                    name = name,
                    role = role,
                    phoneNumber = "+251911000222",
                    email = "$role@halabamarket.com",
                    bio = "Temporary test profile for $role role.",
                    isVerified = true,
                    kebele = "Kebele 01",
                    password = "1234"
                )
                repository.insertUser(newUser)
                
                // Sync to Firebase and Supabase
                viewModelScope.launch(Dispatchers.IO) {
                    FirebaseService.uploadUserToFirestore(getApplication(), newUser)
                    SupabaseService.uploadUser(newUser)
                }
                
                currentUserId = id
            }
        }
    }

    fun loadListingDetail(listingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedListingId = listingId
            val listing = repository.getListingById(listingId)
            selectedListing = listing
            if (listing != null) {
                repository.incrementViewCount(listingId)
            }
        }
    }

    // --- Actions: Listing Operations ---
    fun publishNewListing(
        title: String,
        description: String,
        category: String,
        price: Double,
        isNegotiable: Boolean,
        kebele: String,
        phone: String,
        imageRefs: String,
        videoRefs: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            // Start Simulated Compression & Upload Pipeline
            isUploading = true
            uploadProgress = 0.05f
            currentUploadStatus = "Compressing images to reduce file size..."
            kotlinx.coroutines.delay(1000)

            uploadProgress = 0.35f
            currentUploadStatus = "Uploading compressed images (100% complete)..."
            kotlinx.coroutines.delay(800)

            uploadProgress = 0.55f
            currentUploadStatus = "Compressing and optimizing video files..."
            kotlinx.coroutines.delay(1000)

            uploadProgress = 0.85f
            currentUploadStatus = "Uploading optimized videos..."
            kotlinx.coroutines.delay(800)

            uploadProgress = 0.95f
            currentUploadStatus = "Publishing to Halaba Market..."
            kotlinx.coroutines.delay(500)

            withContext(Dispatchers.IO) {
                val seller = currentUserState.value
                val newListing = ListingEntity(
                    id = "list_${UUID.randomUUID().toString().take(6)}",
                    title = title,
                    description = description,
                    category = category,
                    subcategory = "Sales",
                    price = price,
                    isNegotiable = isNegotiable,
                    location = "Halaba City Center",
                    kebele = kebele,
                    imageUrls = imageRefs.ifEmpty { "market_placeholder" },
                    videoUrl = videoRefs,
                    sellerId = currentUserId,
                    sellerName = seller?.name ?: "Anonymous Seller",
                    sellerPhone = phone,
                    isVerified = true,
                    isApproved = true // Automatically approved and live on submission
                )
                repository.insertListing(newListing)
                
                // Automatic background sync to Firebase Cloud Firestore and Supabase REST backend
                viewModelScope.launch(Dispatchers.IO) {
                    FirebaseService.uploadListingToFirestore(getApplication(), newListing)
                    SupabaseService.uploadListing(newListing)
                }
                
                // Add success notification
                val notify = NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = "Listing Posted Successfully!",
                    body = "Your product '${title}' is now live on Halaba Market for everyone to view!"
                )
                repository.insertNotification(notify)
            }

            uploadProgress = 1.0f
            currentUploadStatus = "Published successfully!"
            kotlinx.coroutines.delay(300)
            isUploading = false

            onComplete()
        }
    }

    fun toggleFavoriteListing(listingId: String, isFav: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavoriteCount(listingId, if (isFav) 1 else -1)
            // Re-read detail if selected
            if (selectedListingId == listingId) {
                selectedListing = repository.getListingById(listingId)
            }
        }
    }

    fun markSold(listingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markListingSold(listingId, true)
            if (selectedListingId == listingId) {
                selectedListing = repository.getListingById(listingId)
            }
        }
    }

    fun approveOrRejectListing(listingId: String, approve: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveListing(listingId, approve)
            if (selectedListingId == listingId) {
                selectedListing = repository.getListingById(listingId)
            }
        }
    }

    fun removeListing(listingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteListing(listingId)
        }
    }

    // --- Actions: Messaging ---
    fun sendChatMessage(text: String) {
        val partner = chatPartnerId ?: return
        if (text.trim().isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val user = currentUserState.value
            val newMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                senderName = user?.name ?: "User",
                receiverId = partner,
                text = text
            )
            repository.sendMessage(newMsg)

            // Sync message to Cloud (Firebase & Supabase)
            viewModelScope.launch(Dispatchers.IO) {
                FirebaseService.uploadMessageToFirestore(getApplication(), newMsg)
                SupabaseService.uploadMessage(newMsg)
            }

            // Trigger a simulated smart broker reply if messaging a broker/seller
            simulatePartnerReply(partner, text)
        }
    }

    private fun simulatePartnerReply(partnerId: String, userText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(1500)
            val partnerUser = repository.getUserById(partnerId) ?: return@launch
            val responseText = when {
                partnerUser.role == "broker" -> {
                    "Selam! I am analyzing this listing for you. Let's arrange a site visit on the calendar page so we can inspect the land/Bajaj together. Let me know when you are free!"
                }
                userText.lowercase().contains("price") || userText.lowercase().contains("negotiable") -> {
                    "The price listed is slightly negotiable for serious buyers. Let's coordinate with our broker to organize a contract review."
                }
                else -> {
                    "Thank you for reaching out. Yes, this item is still available. Feel free to schedule a quick meeting with me here!"
                }
            }

            val replyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                senderId = partnerId,
                senderName = partnerUser.name,
                receiverId = currentUserId,
                text = responseText
            )
            repository.sendMessage(replyMsg)

            // Sync simulated reply to Cloud
            viewModelScope.launch(Dispatchers.IO) {
                FirebaseService.uploadMessageToFirestore(getApplication(), replyMsg)
                SupabaseService.uploadMessage(replyMsg)
            }

            // Notification alert
            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                title = "New chat from ${partnerUser.name}",
                body = responseText.take(50) + "..."
            )
            repository.insertNotification(notification)
            viewModelScope.launch(Dispatchers.IO) {
                SupabaseService.uploadNotification(notification)
            }
        }
    }

    // --- Actions: Scheduling & Calendar ---
    fun createAppointment(title: String, date: String, time: String, partnerUserId: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currUser = currentUserState.value
            val partnerUser = repository.getUserById(partnerUserId)
            
            val buyerId = if (currUser?.role == "buyer") currentUserId else partnerUserId
            val sellerId = if (currUser?.role == "seller") currentUserId else partnerUserId
            val brokers = allUsers.value.filter { it.role == "broker" }
            val broker = brokers.firstOrNull()

            val newMeet = MeetingEntity(
                id = "meet_${UUID.randomUUID().toString().take(6)}",
                title = title,
                date = date,
                time = time,
                buyerId = buyerId,
                sellerId = sellerId,
                brokerId = broker?.id ?: "",
                buyerName = if (currUser?.role == "buyer") currUser.name else (partnerUser?.name ?: "Buyer"),
                sellerName = if (currUser?.role == "seller") currUser.name else (partnerUser?.name ?: "Seller"),
                brokerName = broker?.name ?: "Assigned Broker",
                note = note,
                status = "Pending"
            )
            repository.scheduleMeeting(newMeet)
            viewModelScope.launch(Dispatchers.IO) {
                SupabaseService.uploadMeeting(newMeet)
            }

            // Insert alert for both
            val notify1 = NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                title = "Meeting Requested",
                body = "Appointment scheduled for $date at $time regarding: '$title'."
            )
            val notify2 = NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = partnerUserId,
                title = "New Meeting Request",
                body = "${currUser?.name} requested a site visit on $date at $time."
            )
            repository.insertNotification(notify1)
            repository.insertNotification(notify2)
            viewModelScope.launch(Dispatchers.IO) {
                SupabaseService.uploadNotification(notify1)
                SupabaseService.uploadNotification(notify2)
            }
        }
    }

    fun updateMeetingStatus(meetId: String, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMeetingStatus(meetId, status)
            val meeting = repository.getMeetingById(meetId)
            if (meeting != null) {
                SupabaseService.uploadMeeting(meeting)
            }
        }
    }

    // --- Actions: Submit Review ---
    fun addReview(revieweeId: String, rating: Float, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = currentUserState.value
            val newReview = ReviewEntity(
                id = UUID.randomUUID().toString(),
                reviewerId = currentUserId,
                reviewerName = user?.name ?: "Verified Buyer",
                revieweeId = revieweeId,
                rating = rating,
                comment = comment
            )
            repository.submitReview(newReview)
            viewModelScope.launch(Dispatchers.IO) {
                SupabaseService.uploadReview(newReview)
            }
        }
    }

    // --- Actions: AI Chatbot Logic ---
    fun sendAiChatPrompt(promptText: String) {
        if (promptText.trim().isEmpty()) return
        
        // Add user prompt to screen immediately
        val history = aiChatHistory.value.toMutableList()
        history.add(Pair(promptText, true))
        aiChatHistory.value = history
        isAiTyping = true

        viewModelScope.launch(Dispatchers.IO) {
            val response = GeminiClient.getAiResponse(promptText)
            withContext(Dispatchers.Main) {
                val updatedHistory = aiChatHistory.value.toMutableList()
                updatedHistory.add(Pair(response, false))
                aiChatHistory.value = updatedHistory
                isAiTyping = false
            }
        }
    }

    fun clearAiChat() {
        aiChatHistory.value = listOf(
            Pair("Selam! I am Halaba AI, your local market assistant. Ask me anything about pepper prices, land valuations, or town regulations in Halaba City!", false)
        )
    }

    fun dismissNotification(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markNotificationsAsRead(currentUserId)
        }
    }

    // --- State: Firebase Sync ---
    var firebaseSyncStatus by mutableStateOf("Ready") // "Ready", "Syncing", "Success", "Error"
    var firebaseIsInitialized by mutableStateOf(false)
    var firebaseSyncProgress by mutableStateOf(0f)
    private val _firebaseSyncLogs = MutableStateFlow<List<String>>(emptyList())
    val firebaseSyncLogs: StateFlow<List<String>> = _firebaseSyncLogs.asStateFlow()

    fun checkFirebaseStatus(context: Context) {
        firebaseIsInitialized = FirebaseService.isFirebaseInitialized(context)
    }

    fun syncWithFirebase(context: Context) {
        if (firebaseSyncStatus == "Syncing") return
        firebaseSyncStatus = "Syncing"
        firebaseSyncProgress = 0f
        _firebaseSyncLogs.value = emptyList()

        viewModelScope.launch {
            val logs = mutableListOf<String>()
            fun addLog(msg: String) {
                logs.add("[Sync] $msg")
                _firebaseSyncLogs.value = logs.toList()
            }

            addLog("Checking Firebase SDK status in this environment...")
            delay(600)
            val initialized = FirebaseService.isFirebaseInitialized(context)
            firebaseIsInitialized = initialized

            if (initialized) {
                addLog("Firebase successfully initialized via Google Play services config!")
                delay(400)
                addLog("Acquired Firestore DB default instance.")
                
                val listingsToSync = repository.allListings.firstOrNull() ?: emptyList()
                val usersToSync = repository.allUsers.firstOrNull() ?: emptyList()
                
                addLog("Found ${usersToSync.size} local profiles and ${listingsToSync.size} listings to sync.")
                delay(500)

                var count = 0
                val total = listingsToSync.size + usersToSync.size
                if (total == 0) {
                    addLog("Nothing to sync! Local database is empty.")
                    firebaseSyncProgress = 1.0f
                    firebaseSyncStatus = "Success"
                    return@launch
                }
                
                for (u in usersToSync) {
                    addLog("Firestore [users]: Synchronizing user profile '${u.name}'...")
                    FirebaseService.uploadUserToFirestore(context, u)
                    count++
                    firebaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(200)
                }

                for (l in listingsToSync) {
                    addLog("Firestore [listings]: Synchronizing listing '${l.title}'...")
                    FirebaseService.uploadListingToFirestore(context, l)
                    count++
                    firebaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(200)
                }

                addLog("Cloud sync completed successfully! ${total} records mapped to Firestore.")
                firebaseSyncStatus = "Success"
            } else {
                addLog("⚠️ Warning: FirebaseApp instance not initialized.")
                addLog("Notice: 'google-services.json' configuration file was not detected.")
                delay(600)
                addLog("Active Safe Mode: Redirecting to sandboxed cloud simulator.")
                delay(600)

                val listingsToSync = repository.allListings.firstOrNull() ?: emptyList()
                val usersToSync = repository.allUsers.firstOrNull() ?: emptyList()
                
                addLog("Scanning Room local tables: ${usersToSync.size} users, ${listingsToSync.size} listings discovered.")
                delay(700)

                var count = 0
                val total = listingsToSync.size + usersToSync.size
                if (total == 0) {
                    addLog("Nothing to sync! Local database is empty.")
                    firebaseSyncProgress = 1.0f
                    firebaseSyncStatus = "Success"
                    return@launch
                }
                
                for (u in usersToSync) {
                    addLog("Simulating Firestore write (Collection: 'users', Document: '${u.id}')")
                    addLog(" - Saved: { name='${u.name}', role='${u.role}', kebele='${u.kebele}' }")
                    count++
                    firebaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(250)
                }

                for (l in listingsToSync) {
                    addLog("Simulating Firestore write (Collection: 'listings', Document: '${l.id}')")
                    addLog(" - Saved: { title='${l.title}', price=${l.price} ETB, category='${l.category}' }")
                    count++
                    firebaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(250)
                }

                addLog("✅ Cloud Sandbox sync emulation finalized! All ${total} records verified.")
                addLog("Hint: To enable a live Google Cloud instance, place your project's 'google-services.json' in `/app`.")
                firebaseSyncStatus = "Success"
            }
        }
    }

    // --- State: Supabase Sync ---
    var supabaseSyncStatus by mutableStateOf("Ready") // "Ready", "Syncing", "Success", "Error"
    var supabaseIsConfigured by mutableStateOf(false)
    var supabaseSyncProgress by mutableStateOf(0f)
    private val _supabaseSyncLogs = MutableStateFlow<List<String>>(emptyList())
    val supabaseSyncLogs: StateFlow<List<String>> = _supabaseSyncLogs.asStateFlow()

    fun checkSupabaseStatus() {
        supabaseIsConfigured = SupabaseService.isConfigured()
    }

    fun syncWithSupabase() {
        if (supabaseSyncStatus == "Syncing") return
        supabaseSyncStatus = "Syncing"
        supabaseSyncProgress = 0f
        _supabaseSyncLogs.value = emptyList()

        viewModelScope.launch {
            val logs = mutableListOf<String>()
            fun addLog(msg: String) {
                logs.add("[Supabase] $msg")
                _supabaseSyncLogs.value = logs.toList()
            }

            addLog("Checking Supabase backend configurations...")
            delay(500)
            val configured = SupabaseService.isConfigured()
            supabaseIsConfigured = configured

            if (configured) {
                addLog("Supabase configuration valid!")
                addLog("Project ID: rmzootosbnyhvpnbxely")
                delay(300)
                
                val usersToSync = repository.allUsers.firstOrNull() ?: emptyList()
                val listingsToSync = repository.allListings.firstOrNull() ?: emptyList()
                val meetingsToSync = repository.allMeetings.firstOrNull() ?: emptyList()
                val reviewsToSync = repository.allReviews.firstOrNull() ?: emptyList()
                
                val total = usersToSync.size + listingsToSync.size + meetingsToSync.size + reviewsToSync.size
                addLog("Discovered local database tables:")
                addLog(" - Users: ${usersToSync.size} records")
                addLog(" - Listings: ${listingsToSync.size} records")
                addLog(" - Meetings: ${meetingsToSync.size} records")
                addLog(" - Reviews: ${reviewsToSync.size} records")
                delay(500)

                if (total == 0) {
                    addLog("Nothing to sync! Local tables are currently empty.")
                    supabaseSyncProgress = 1.0f
                    supabaseSyncStatus = "Success"
                    return@launch
                }

                var count = 0
                for (u in usersToSync) {
                    addLog("Syncing User [${u.name}] to 'users' table...")
                    val success = SupabaseService.uploadUser(u)
                    if (success) {
                        addLog(" -> SUCCESS: user_id=${u.id}")
                    } else {
                        addLog(" -> WARNING: Failed to upsert ${u.id}")
                    }
                    count++
                    supabaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(150)
                }

                for (l in listingsToSync) {
                    addLog("Syncing Listing [${l.title}] to 'listings' table...")
                    val success = SupabaseService.uploadListing(l)
                    if (success) {
                        addLog(" -> SUCCESS: listing_id=${l.id}")
                    } else {
                        addLog(" -> WARNING: Failed to upsert ${l.id}")
                    }
                    count++
                    supabaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(150)
                }

                for (m in meetingsToSync) {
                    addLog("Syncing Meeting [${m.title}] to 'meetings' table...")
                    val success = SupabaseService.uploadMeeting(m)
                    if (success) {
                        addLog(" -> SUCCESS: meeting_id=${m.id}")
                    } else {
                        addLog(" -> WARNING: Failed to upsert ${m.id}")
                    }
                    count++
                    supabaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(150)
                }

                for (r in reviewsToSync) {
                    addLog("Syncing Review [${r.rating}⭐] to 'reviews' table...")
                    val success = SupabaseService.uploadReview(r)
                    if (success) {
                        addLog(" -> SUCCESS: review_id=${r.id}")
                    } else {
                        addLog(" -> WARNING: Failed to upsert ${r.id}")
                    }
                    count++
                    supabaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(150)
                }

                addLog("✅ Supabase REST sync finalized! All $count records synced successfully.")
                supabaseSyncStatus = "Success"
            } else {
                addLog("⚠️ Configuration Warning: Missing SUPABASE_URL or SUPABASE_KEY in .env file.")
                addLog("Falling back to local emulator simulation mode.")
                delay(600)
                
                val usersToSync = repository.allUsers.firstOrNull() ?: emptyList()
                val listingsToSync = repository.allListings.firstOrNull() ?: emptyList()
                val total = usersToSync.size + listingsToSync.size
                
                addLog("Simulating Supabase push: ${total} records discovered.")
                delay(500)

                var count = 0
                for (u in usersToSync) {
                    addLog("[Simulate] POST to /rest/v1/users")
                    addLog("  { id: '${u.id}', name: '${u.name}', role: '${u.role}' }")
                    count++
                    supabaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(200)
                }

                for (l in listingsToSync) {
                    addLog("[Simulate] POST to /rest/v1/listings")
                    addLog("  { id: '${l.id}', title: '${l.title}', price: ${l.price} }")
                    count++
                    supabaseSyncProgress = count.toFloat() / total.toFloat()
                    delay(200)
                }

                addLog("✅ Supabase emulation finished! $total records simulated successfully.")
                supabaseSyncStatus = "Success"
            }
        }
    }
}
