package com.example.myapplication.ui.dashboard
import androidx.compose.foundation.lazy.items

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.material3.TextFieldDefaults
import com.example.myapplication.ui.community.CommunityScreen
import com.example.myapplication.ui.dashboard.EventsScreen
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.ui.draw.shadow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.myapplication.ProfileScreen
import com.example.myapplication.ImpactHistoryScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import coil.request.ImageRequest

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object Community : Screen("community", Icons.Default.People, "Community")
    object Events : Screen("events", Icons.Default.Event, "Events")
    object Chats : Screen("chats", Icons.Default.Chat, "Chats")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
}

data class Event(
    val id: Int = 0,
    val title: String = "",
    val dateRange: String = "",
    val rating: Double = 0.0,
    val organizer: String = "",
    @DrawableRes val imageRes: Int = R.drawable.event1
)

// Data class for Post
// (Add after Event data class)
data class UserPost(
    val id: String = "",
    val author: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String = "",
    val likes: List<String> = emptyList(), // user IDs
    val dislikes: List<String> = emptyList(), // user IDs
    val comments: List<Comment> = emptyList()
)

data class Comment(
    val userId: String = "",
    val author: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)

fun addPostToFirestore(
    post: UserPost,
    onSuccess: () -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("posts")
        .add(post)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e) }
}

fun loadPostsFromFirestore(
    onResult: (List<UserPost>) -> Unit,
    onError: (Exception) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("posts")
        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { result ->
            val posts = result.mapNotNull { it.toObject(UserPost::class.java) }
            onResult(posts)
        }
        .addOnFailureListener { e -> onError(e) }
}

fun loadDashboardEventsFromFirestore(
    onResult: (List<Event>) -> Unit,
    onError: (Exception) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("events")
        .get()
        .addOnSuccessListener { result ->
            val events = result.mapNotNull { doc ->
                try {
                    Event(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        title = doc.getString("title") ?: "",
                        dateRange = doc.getString("dateRange") ?: "",
                        rating = doc.getDouble("rating") ?: 0.0,
                        organizer = doc.getString("organizer") ?: "",
                        imageRes = when (doc.getLong("id")?.toInt()) {
                            1 -> R.drawable.event1
                            2 -> R.drawable.event2
                            3 -> R.drawable.event3
                            else -> R.drawable.event1
                        }
                    )
                } catch (e: Exception) {
                    null
                }
            }
            onResult(events)
        }
        .addOnFailureListener { e -> onError(e) }
}

@Composable
fun DashboardScreen(
    events: List<Event> = emptyList(), // default for preview
    onEventClick: (Event) -> Unit = {}
) {
    val categories = listOf("Popular", "Pollution", "Outdoor", "Community")
    var selectedCat by remember { mutableStateOf(categories.first()) }
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var posts by remember { mutableStateOf<List<UserPost>>(emptyList()) }
    var showPostDialog by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var eventsState by remember { mutableStateOf<List<Event>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showImpactHistory by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf(false) }
    var selectedPostForComments by remember { mutableStateOf<UserPost?>(null) }
    var discussionPost by remember { mutableStateOf<UserPost?>(null) }
    var selectedEventForDetails by remember { mutableStateOf<Event?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

    // Function to update a post optimistically
    fun updatePostOptimistically(postId: String, update: (UserPost) -> UserPost) {
        posts = posts.map { post ->
            if (post.id == postId) update(post) else post
        }
    }

    // Set up real-time listener for posts
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    posts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(UserPost::class.java)
                    }
                    loading = false
                }
            }
    }

    // Load events from Firestore on first launch
    LaunchedEffect(Unit) {
        loadDashboardEventsFromFirestore(
            onResult = { eventsState = it },
            onError = { error = it.message }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        // Event Details Overlay
        if (selectedEventForDetails != null) {
            ModernEventDetailsOverlay(
                event = selectedEventForDetails!!,
                onClose = { selectedEventForDetails = null }
            )
        } else if (discussionPost != null) {
            PostDiscussionScreen(
                post = discussionPost!!,
                onBack = { discussionPost = null }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main content (no bottom padding)
                Box(modifier = Modifier.weight(1f, fill = true)) {
                    when (selectedScreen) {
                        Screen.Home -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 0.dp)
                            ) {
                                // Top Bar: ENVIRONMENTALIST and Bell Icon
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "ENVIRONMENTALIST",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                letterSpacing = 4.sp
                                            ),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { /* TODO: Notifications */ },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Notifications",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                                // Search bar in white container (styled)
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                            .height(56.dp)
                                            .shadow(4.dp, RoundedCornerShape(20.dp), clip = false)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.8f),
                                                modifier = Modifier.padding(start = 16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(modifier = Modifier.weight(1f)) {
                                                TextField(
                                                    value = searchQuery,
                                                    onValueChange = { searchQuery = it },
                                                    placeholder = { Text("Search for activities", color = Color.White.copy(alpha = 0.7f)) },
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent,
                                                        focusedIndicatorColor = Color.Transparent,
                                                        unfocusedIndicatorColor = Color.Transparent,
                                                        cursorColor = Color.White
                                                    ),
                                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            IconButton(
                                                onClick = { /* TODO: Filter/Settings */ },
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Tune,
                                                    contentDescription = "Filter",
                                                    tint = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                                // Featured event
                                item {
                                    eventsState.firstOrNull()?.let { featured ->
                                        EventCard(featured, fullWidth = true, onClick = onEventClick)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                // Category tabs
                                item {
                                    LazyRow(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(categories.size) { idx ->
                                            val cat = categories[idx]
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (cat == selectedCat) Color(0xFF388E3C) else Color.White.copy(alpha = 0.7f),
                                                shadowElevation = if (cat == selectedCat) 6.dp else 0.dp,
                                                modifier = Modifier
                                                    .clickable { selectedCat = cat }
                                            ) {
                                                Text(
                                                    text = cat,
                                                    color = if (cat == selectedCat) Color.White else Color(0xFF388E3C),
                                                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                                                    fontWeight = if (cat == selectedCat) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                // Events list for selected cat
                                item {
                                    LazyRow(
                                        Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(eventsState.filter { itMatchesCategory(it, selectedCat) && it.title.contains(searchQuery, ignoreCase = true) }) { event ->
                                            EventCard(event, fullWidth = false, onClick = { selectedEventForDetails = event })
                                        }
                                    }
                                }
                                // Community Feed title
                                item {
                                    Text(
                                        "Community Feed",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                                    )
                                }
                                // Posts list
                                items(posts.filter { it.content.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true) }) { post ->
                                    PostCard(
                                        post,
                                        onLike = { postToLike ->
                                            // Optimistically update the UI
                                            updatePostOptimistically(postToLike.id) { currentPost ->
                                                val updatedLikes = if (currentPost.likes.contains(userId))
                                                    currentPost.likes - userId
                                                else
                                                    currentPost.likes + userId
                                                val updatedDislikes = currentPost.dislikes - userId
                                                currentPost.copy(
                                                    likes = updatedLikes,
                                                    dislikes = updatedDislikes
                                                )
                                            }

                                            // Update in Firestore
                                            val ref = FirebaseFirestore.getInstance().collection("posts").document(postToLike.id)
                                            val updatedLikes = if (postToLike.likes.contains(userId)) postToLike.likes - userId else postToLike.likes + userId
                                            val updatedDislikes = postToLike.dislikes - userId
                                            ref.update(mapOf(
                                                "likes" to updatedLikes,
                                                "dislikes" to updatedDislikes
                                            )).addOnFailureListener { e ->
                                                // Revert optimistic update on failure
                                                updatePostOptimistically(postToLike.id) { currentPost ->
                                                    currentPost.copy(
                                                        likes = postToLike.likes,
                                                        dislikes = postToLike.dislikes
                                                    )
                                                }
                                                error = "Failed to update like: ${e.message}"
                                            }
                                        },
                                        onDislike = { postToDislike ->
                                            // Optimistically update the UI
                                            updatePostOptimistically(postToDislike.id) { currentPost ->
                                                val updatedDislikes = if (currentPost.dislikes.contains(userId))
                                                    currentPost.dislikes - userId
                                                else
                                                    currentPost.dislikes + userId
                                                val updatedLikes = currentPost.likes - userId
                                                currentPost.copy(
                                                    dislikes = updatedDislikes,
                                                    likes = updatedLikes
                                                )
                                            }

                                            // Update in Firestore
                                            val ref = FirebaseFirestore.getInstance().collection("posts").document(postToDislike.id)
                                            val updatedDislikes = if (postToDislike.dislikes.contains(userId)) postToDislike.dislikes - userId else postToDislike.dislikes + userId
                                            val updatedLikes = postToDislike.likes - userId
                                            ref.update(mapOf(
                                                "dislikes" to updatedDislikes,
                                                "likes" to updatedLikes
                                            )).addOnFailureListener { e ->
                                                // Revert optimistic update on failure
                                                updatePostOptimistically(postToDislike.id) { currentPost ->
                                                    currentPost.copy(
                                                        dislikes = postToDislike.dislikes,
                                                        likes = postToDislike.likes
                                                    )
                                                }
                                                error = "Failed to update dislike: ${e.message}"
                                            }
                                        },
                                        onComment = { postToComment ->
                                            selectedPostForComments = postToComment
                                            showCommentDialog = true
                                        },
                                        onViewDiscussion = { postToView -> discussionPost = postToView }
                                    )
                                }
                            }
                            // FAB for creating post
                            FloatingActionButton(
                                onClick = { showPostDialog = true },
                                containerColor = Color(0xFF388E3C),
                                contentColor = Color.White,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 24.dp, bottom = 88.dp)
                                    .size(64.dp)
                                    .shadow(12.dp, RoundedCornerShape(50))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create Post", modifier = Modifier.size(32.dp))
                            }
                            // Post creation dialog
                            if (showPostDialog) {
                                CreatePostDialog(
                                    onDismiss = { showPostDialog = false },
                                    onCreate = { newPost ->
                                        addPostToFirestore(newPost,
                                            onSuccess = {
                                                // No need to manually reload posts as the listener will handle it
                                                showPostDialog = false
                                            },
                                            onFailure = { e ->
                                                error = e.message
                                                showPostDialog = false
                                            }
                                        )
                                    }
                                )
                            }
                            if (showCommentDialog && selectedPostForComments != null) {
                                CommentDialog(
                                    post = selectedPostForComments!!,
                                    onDismiss = { showCommentDialog = false; selectedPostForComments = null },
                                    onAddComment = { commentContent ->
                                        val newComment = Comment(
                                            userId = userId,
                                            author = userName,
                                            content = commentContent,
                                            timestamp = System.currentTimeMillis()
                                        )

                                        // Optimistically update the UI
                                        updatePostOptimistically(selectedPostForComments!!.id) { currentPost ->
                                            currentPost.copy(
                                                comments = currentPost.comments + newComment
                                            )
                                        }

                                        // Update in Firestore
                                        val ref = FirebaseFirestore.getInstance().collection("posts").document(selectedPostForComments!!.id)
                                        val updatedComments = selectedPostForComments!!.comments + newComment
                                        ref.update("comments", updatedComments).addOnFailureListener { e ->
                                            // Revert optimistic update on failure
                                            updatePostOptimistically(selectedPostForComments!!.id) { currentPost ->
                                                currentPost.copy(
                                                    comments = selectedPostForComments!!.comments
                                                )
                                            }
                                            error = "Failed to add comment: ${e.message}"
                                        }

                                        showCommentDialog = false
                                        selectedPostForComments = null
                                    }
                                )
                            }
                        }
                        Screen.Community -> {
                            CommunityScreen()
                        }
                        Screen.Events -> {
                            EventsScreen()
                        }
                        Screen.Chats -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text("Chats Screen - Coming Soon", modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        Screen.Profile -> {
                            if (showImpactHistory) {
                                ImpactHistoryScreen(onBack = { showImpactHistory = false })
                            } else {
                                ProfileScreen(
                                    onLogout = {
                                        selectedScreen = Screen.Home
                                    },
                                    onImpactHistory = { showImpactHistory = true }
                                )
                            }
                        }

                        else -> {}
                    }
                }
                // Fixed bottom navigation bar
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A1A1A).copy(alpha = 0.95f)),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    val screens = listOf(
                        Screen.Home,
                        Screen.Community,
                        Screen.Events,
                        Screen.Chats,
                        Screen.Profile
                    )
                    screens.forEach { screen ->
                        NavigationBarItem(
                            selected = selectedScreen == screen,
                            onClick = { selectedScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label,
                                    tint = if (selectedScreen == screen)
                                        Color(0xFF4CAF50)
                                    else
                                        Color.White.copy(alpha = 0.6f)
                                )
                            },
                            label = {
                                Text(
                                    screen.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedScreen == screen)
                                        Color(0xFF4CAF50)
                                    else
                                        Color.White.copy(alpha = 0.6f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF4CAF50),
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                selectedTextColor = Color(0xFF4CAF50),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .padding(horizontal = 1.dp)
                                .height(56.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun itMatchesCategory(event: Event, category: String): Boolean {
    return category == "Popular"
}

@Composable
fun EventCard(
    event: Event,
    fullWidth: Boolean,
    onClick: (Event) -> Unit
) {
    val cardModifier = if (fullWidth) {
        Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp)
    } else {
        Modifier
            .width(280.dp)
            .height(360.dp)
    }
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(event.imageRes)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .build()
    )
    Card(
        modifier = cardModifier
            .clickable { onClick(event) }
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                // Event Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (fullWidth) 160.dp else 200.dp)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                    // Rating overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${event.rating}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF194D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Event Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF194D32)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            event.dateRange,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF388E3C)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "By ${event.organizer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // View Details Button
                    Button(
                        onClick = { onClick(event) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF388E3C)
                        )
                    ) {
                        Text(
                            "View Details",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// PostCard composable
@Composable
fun PostCard(
    post: UserPost,
    onLike: (UserPost) -> Unit,
    onDislike: (UserPost) -> Unit,
    onComment: (UserPost) -> Unit,
    onViewDiscussion: (UserPost) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val liked = post.likes.contains(userId)
    val disliked = post.dislikes.contains(userId)
    val likeAnim = animateFloatAsState(targetValue = if (liked) 1.2f else 1f, animationSpec = tween(200))
    val dislikeAnim = animateFloatAsState(targetValue = if (disliked) 1.2f else 1f, animationSpec = tween(200))
    val likeColor by animateColorAsState(if (liked) Color(0xFF388E3C) else Color(0xFFB0B0B0), animationSpec = tween(200))
    val dislikeColor by animateColorAsState(if (disliked) Color(0xFFD32F2F) else Color(0xFFB0B0B0), animationSpec = tween(200))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable { onViewDiscussion(post) }
            .shadow(10.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar (placeholder with initials)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF388E3C).copy(alpha = 0.15f), shape = RoundedCornerShape(50))
                        .border(2.dp, Color(0xFF388E3C), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF388E3C)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.author, fontWeight = FontWeight.Bold, color = Color(0xFF194D32), fontSize = 17.sp)
                    Text(
                        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(post.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(post.content, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF222222))
            if (post.imageUrl.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onLike(post) }, modifier = Modifier.graphicsLayer(scaleX = likeAnim.value, scaleY = likeAnim.value)) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = likeColor
                    )
                }
                Text("${post.likes.size}", color = Color(0xFF888888), fontSize = 15.sp)
                IconButton(onClick = { onDislike(post) }, modifier = Modifier.graphicsLayer(scaleX = dislikeAnim.value, scaleY = dislikeAnim.value)) {
                    Icon(
                        imageVector = if (disliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "Dislike",
                        tint = dislikeColor
                    )
                }
                Text("${post.dislikes.size}", color = Color(0xFF888888), fontSize = 15.sp)
                IconButton(onClick = { onComment(post) }) {
                    Icon(Icons.Default.Comment, contentDescription = "Comment", tint = Color(0xFF388E3C))
                }
                Text("${post.comments.size}", color = Color(0xFF888888), fontSize = 15.sp)
            }
        }
    }
}

// CreatePostDialog composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onCreate: (UserPost) -> Unit) {
    var content by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploading = true
            val storageRef = FirebaseStorage.getInstance().reference.child("post_images/${System.currentTimeMillis()}.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        imageUrl = url.toString()
                        imageUri = uri
                        uploading = false
                    }
                }
                .addOnFailureListener { e ->
                    uploadError = e.message
                    uploading = false
                }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val author = "User" // TODO: Replace with actual user name
                    val post = UserPost(
                        id = UUID.randomUUID().toString(),
                        author = author,
                        content = content,
                        timestamp = System.currentTimeMillis(),
                        imageUrl = imageUrl
                    )
                    onCreate(post)
                },
                enabled = content.isNotBlank() && !uploading
            ) {
                Text(if (uploading) "Uploading..." else "Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Create Post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("What's on your mind?") })
                Button(onClick = { launcher.launch("image/*") }, enabled = !uploading) {
                    Text(if (imageUrl.isBlank()) "Pick Image" else "Change Image")
                }
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                if (uploadError != null) {
                    Text("Upload error: $uploadError", color = Color.Red)
                }
            }
        }
    )
}

@Composable
fun CommentDialog(post: UserPost, onDismiss: () -> Unit, onAddComment: (String) -> Unit) {
    var commentText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { if (commentText.isNotBlank()) onAddComment(commentText) }, enabled = commentText.isNotBlank()) {
                Text("Add Comment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Comments") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(post.comments) { comment ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Column(Modifier.padding(8.dp)) {
                                Text(comment.author, fontWeight = FontWeight.Bold, color = Color(0xFF194D32))
                                Text(comment.content)
                                Text(SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(comment.timestamp)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
                OutlinedTextField(value = commentText, onValueChange = { commentText = it }, label = { Text("Add a comment...") })
            }
        }
    )
}

// Full discussion screen for a post
@Composable
fun PostDiscussionScreen(post: UserPost, onBack: () -> Unit) {
    var comments by remember { mutableStateOf(post.comments) }
    var commentText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

    // Real-time comments
    LaunchedEffect(post.id) {
        val ref = FirebaseFirestore.getInstance().collection("posts").document(post.id)
        val listener = ref.addSnapshotListener { snapshot, _ ->
            val updated = snapshot?.toObject(UserPost::class.java)
            if (updated != null) comments = updated.comments
        }
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(12.dp))) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF194D32))
                }
                Spacer(Modifier.width(8.dp))
                Text("Discussion", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(post.author, fontWeight = FontWeight.Bold, color = Color(0xFF194D32))
                    Text(post.content)
                    if (post.imageUrl.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Image(
                            painter = rememberAsyncImagePainter(post.imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Text("Comments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(comments) { comment ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Column(Modifier.padding(8.dp)) {
                            Text(comment.author, fontWeight = FontWeight.Bold, color = Color(0xFF194D32))
                            Text(comment.content)
                            Text(SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(comment.timestamp)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = commentText, onValueChange = { commentText = it }, label = { Text("Add a comment...") }, modifier = Modifier.weight(1f))
                Button(onClick = {
                    if (commentText.isNotBlank()) {
                        loading = true
                        val ref = FirebaseFirestore.getInstance().collection("posts").document(post.id)
                        val newComment = Comment(userId = userId, author = userName, content = commentText, timestamp = System.currentTimeMillis())
                        ref.get().addOnSuccessListener { doc ->
                            val current = doc.toObject(UserPost::class.java)?.comments ?: emptyList()
                            val updated = current + newComment
                            ref.update("comments", updated).addOnSuccessListener {
                                commentText = ""
                                loading = false
                            }
                        }
                    }
                }, enabled = commentText.isNotBlank() && !loading) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Send")
                }
            }
        }
    }
}

// Modern Event Details Overlay
@Composable
fun ModernEventDetailsOverlay(event: Event, onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth()) {
                        // Modern image with fade-in and placeholder
                        val painter = rememberAsyncImagePainter(
                            model = event.imageRes,
                            placeholder = painterResource(R.drawable.placeholder),
                            error = painterResource(R.drawable.placeholder)
                        )
                        Image(
                            painter = painter,
                            contentDescription = event.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.White, RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                        }
                    }
                    Column(Modifier.padding(24.dp)) {
                        Text(event.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF194D32))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF388E3C))
                            Spacer(Modifier.width(6.dp))
                            Text(event.dateRange, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF388E3C))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                            Text("${event.rating} BY– ${event.organizer}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Event details and description go here...", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF194D32))
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { /* RSVP logic */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF194D32))
                        ) {
                            Text("RSVP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    val sampleEvents = listOf(
        Event(
            id = 1,
            title = "Feed the Homeless",
            dateRange = "Oct 10 – Oct 20",
            rating = 4.5,
            organizer = "Dr.Krish",
            imageRes = R.drawable.event1
        ),
        Event(
            id = 2,
            title = "Clean Central Park",
            dateRange = "Oct 05 – Oct 15",
            rating = 4.2,
            organizer = "Dr.Krish",
            imageRes = R.drawable.event2
        ),
        Event(
            id = 3,
            title = "Community Garden",
            dateRange = "Nov 01 – Nov 10",
            rating = 4.8,
            organizer = "Dr.Krish",
            imageRes = R.drawable.event3
        )
    )
    DashboardScreen(events = sampleEvents) { /* TODO */ }
}
