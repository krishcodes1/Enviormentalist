package com.example.myapplication.ui.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import coil.compose.rememberAsyncImagePainter

data class Post(
    val id: Int,
    val author: String,
    val authorAvatar: Int,
    val title: String,
    val content: String,
    val imageUrl: Int?,
    val upvotes: Int,
    val comments: Int,
    val timeAgo: String,
    val community: String
)

data class Community(
    val name: String = "",
    val description: String = "",
    val imageUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen() {
    var communities by remember { mutableStateOf(listOf<Community>()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCommunity by remember { mutableStateOf<Community?>(null) }

    // Function to update communities optimistically
    fun updateCommunitiesOptimistically(update: (List<Community>) -> List<Community>) {
        communities = update(communities)
    }

    // Load communities from Firestore
    LaunchedEffect(Unit) {
        val db = Firebase.firestore
        db.collection("communities")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    communities = snapshot.documents.mapNotNull { it.toObject(Community::class.java) }
                }
            }
    }

    if (selectedCommunity != null) {
        CommunityDetailScreen(
            community = selectedCommunity!!,
            onBack = { selectedCommunity = null }
        )
        return
    }

    Box(Modifier.fillMaxSize()) {
        // App background image (blurred/faded)
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize().blur(8.dp).background(Color.White.copy(alpha = 0.10f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Top Bar
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Community",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }
            )

            // Modern pill-shaped floating tabs
            var selectedTabIndex by remember { mutableStateOf(0) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("All", "Popular", "New", "Following").forEachIndexed { index, tab ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selectedTabIndex == index) Color(0xFF388E3C) else Color.White.copy(alpha = 0.7f),
                        shadowElevation = if (selectedTabIndex == index) 6.dp else 0.dp,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable { selectedTabIndex = index }
                    ) {
                        Text(
                            text = tab,
                            color = if (selectedTabIndex == index) Color.White else Color(0xFF388E3C),
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Community List as modern bubbles
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(communities) { community ->
                    CommunityBubbleCard(community = community, onView = { selectedCommunity = community })
                }
            }
        }
        // Modern circular FAB
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = Color(0xFF388E3C),
            contentColor = Color.White,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 88.dp)
                .size(64.dp)
                .shadow(12.dp, RoundedCornerShape(50))
        ) {
            Icon(Icons.Default.GroupAdd, contentDescription = "Create Community", modifier = Modifier.size(32.dp))
        }
        // Community creation dialog
        if (showCreateDialog) {
            CreateCommunityDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { newCommunity, onSuccess, onFailure ->
                    // Optimistically add the new community
                    updateCommunitiesOptimistically { currentCommunities ->
                        currentCommunities + newCommunity
                    }

                    // Update in Firestore
                    Firebase.firestore.collection("communities")
                        .add(newCommunity)
                        .addOnSuccessListener {
                            showCreateDialog = false
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            // Revert optimistic update on failure
                            updateCommunitiesOptimistically { currentCommunities ->
                                currentCommunities.filter { it.name != newCommunity.name }
                            }
                            error = e.message
                            onFailure(e.message)
                        }
                }
            )
        }
    }
}

// Modern community bubble card
@Composable
fun CommunityBubbleCard(community: Community, onView: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .shadow(10.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.90f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Community avatar (image or placeholder)
            val avatarPainter = if (community.imageUrl.isNotBlank()) {
                rememberAsyncImagePainter(community.imageUrl)
            } else {
                painterResource(R.drawable.avatar1)
            }
            Image(
                painter = avatarPainter,
                contentDescription = "Community Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF388E3C).copy(alpha = 0.10f), CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF194D32)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = community.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF444444),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onView,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text("View Community", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCommunityDialog(onDismiss: () -> Unit, onCreate: (Community, () -> Unit, (String?) -> Unit) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploading = true
            val storageRef = Firebase.storage.reference.child("community_images/${System.currentTimeMillis()}.jpg")
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
                    creating = true
                    onCreate(
                        Community(
                            name = name,
                            description = description,
                            imageUrl = imageUrl
                        ),
                        { creating = false; onDismiss() },
                        { err -> creating = false; uploadError = err }
                    )
                },
                enabled = name.isNotBlank() && description.isNotBlank() && !uploading && !creating
            ) {
                if (creating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Text(if (uploading) "Uploading..." else "Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !creating && !uploading) { Text("Cancel") }
        },
        title = { Text("Create Community") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Community Name") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Button(onClick = { launcher.launch("image/*") }, enabled = !uploading && !creating) {
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

// Community detail screen with posts and chat
@Composable
fun CommunityDetailScreen(community: Community, onBack: () -> Unit) {
    var posts by remember { mutableStateOf(listOf<String>()) }
    var newPost by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<String>()) }
    var newMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Function to update posts optimistically
    fun updatePostsOptimistically(update: (List<String>) -> List<String>) {
        posts = update(posts)
    }

    // Function to update chat messages optimistically
    fun updateChatMessagesOptimistically(update: (List<String>) -> List<String>) {
        chatMessages = update(chatMessages)
    }

    // Set up real-time listeners
    LaunchedEffect(community.name) {
        val db = Firebase.firestore
        val communityRef = db.collection("communities").document(community.name)
        
        // Listen for posts
        communityRef.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    posts = snapshot.documents.mapNotNull { it.getString("content") }
                }
            }

        // Listen for chat messages
        communityRef.collection("chat")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    chatMessages = snapshot.documents.mapNotNull { it.getString("message") }
                }
            }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(bottom = 80.dp)
        ) {
            // Top bar
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(12.dp))) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF194D32))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(community.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            // Posts section
            Text("Posts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(posts) { post ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(post, modifier = Modifier.padding(16.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = newPost, onValueChange = { newPost = it }, label = { Text("Add a post...") }, modifier = Modifier.weight(1f))
                Button(onClick = {
                    if (newPost.isNotBlank()) {
                        // Optimistically add the new post
                        updatePostsOptimistically { currentPosts ->
                            currentPosts + newPost
                        }

                        // Update in Firestore
                        Firebase.firestore.collection("communities").document(community.name).collection("posts")
                            .add(mapOf(
                                "content" to newPost,
                                "timestamp" to System.currentTimeMillis()
                            ))
                            .addOnFailureListener { e ->
                                // Revert optimistic update on failure
                                updatePostsOptimistically { currentPosts ->
                                    currentPosts.filter { it != newPost }
                                }
                                error = e.message
                            }
                        newPost = ""
                    }
                }, enabled = newPost.isNotBlank()) {
                    Text("Post")
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            // Chat section
            Text("Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(chatMessages) { msg ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(msg, modifier = Modifier.padding(16.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = newMessage, onValueChange = { newMessage = it }, label = { Text("Type a message...") }, modifier = Modifier.weight(1f))
                Button(onClick = {
                    if (newMessage.isNotBlank()) {
                        // Optimistically add the new message
                        updateChatMessagesOptimistically { currentMessages ->
                            currentMessages + newMessage
                        }

                        // Update in Firestore
                        Firebase.firestore.collection("communities").document(community.name).collection("chat")
                            .add(mapOf(
                                "message" to newMessage,
                                "timestamp" to System.currentTimeMillis()
                            ))
                            .addOnFailureListener { e ->
                                // Revert optimistic update on failure
                                updateChatMessagesOptimistically { currentMessages ->
                                    currentMessages.filter { it != newMessage }
                                }
                                error = e.message
                            }
                        newMessage = ""
                    }
                }, enabled = newMessage.isNotBlank()) {
                    Text("Send")
                }
            }
        }
        if (loading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        if (error != null) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: $error", color = Color.Red)
                        Button(onClick = { error = null }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    MyApplicationTheme {
        CommunityScreen()
    }
} 