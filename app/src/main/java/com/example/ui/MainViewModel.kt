package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    val repository = Repository(AppDatabase.getDatabase(application))

    // --- State: App-wide Configurations ---
    var selectedLanguage by mutableStateOf("English") // "English", "Amharic", "Halabisa"
    var isDarkTheme by mutableStateOf(false)
    var isHighContrast by mutableStateOf(false)
    var isLargeText by mutableStateOf(false)

    // --- State: Current Session User & Role ---
    var currentUserId by mutableStateOf("buyer_1")
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

    // --- Actions: Session Management ---
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
                    email = "$role@halababroker.com",
                    bio = "Temporary test profile for $role role.",
                    isVerified = true,
                    kebele = "Kebele 01"
                )
                repository.insertUser(newUser)
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
        subcategory: String,
        price: Double,
        isNegotiable: Boolean,
        kebele: String,
        imageRefs: String
    , onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val seller = currentUserState.value
            val brokers = allUsers.value.filter { it.role == "broker" }
            val assignedBroker = brokers.randomOrNull()

            val newListing = ListingEntity(
                id = "list_${UUID.randomUUID().toString().take(6)}",
                title = title,
                description = description,
                category = category,
                subcategory = subcategory,
                price = price,
                isNegotiable = isNegotiable,
                location = "Halaba City Center",
                kebele = kebele,
                imageUrls = imageRefs.ifEmpty { "demo_image" },
                sellerId = currentUserId,
                sellerName = seller?.name ?: "Unknown Seller",
                assignedBrokerId = assignedBroker?.id ?: "",
                assignedBrokerName = assignedBroker?.name ?: ""
            )
            repository.insertListing(newListing)
            
            // Add automatic matching alert
            val notify = NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                title = "Listing Published Successfully!",
                body = "Your listing '${title}' is now live. Broker ${assignedBroker?.name ?: "Admin"} has been assigned to assist you with negotiations."
            )
            repository.insertNotification(notify)

            // Alert assigned broker
            if (assignedBroker != null) {
                val brokerNotify = NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = assignedBroker.id,
                    title = "New Deal Assigned",
                    body = "You have been assigned as broker to Abebe's listing: '${title}'."
                )
                repository.insertNotification(brokerNotify)
            }

            withContext(Dispatchers.Main) {
                onComplete()
            }
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

            // Notification alert
            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                title = "New chat from ${partnerUser.name}",
                body = responseText.take(50) + "..."
            )
            repository.insertNotification(notification)
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
        }
    }

    fun updateMeetingStatus(meetId: String, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMeetingStatus(meetId, status)
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
}
