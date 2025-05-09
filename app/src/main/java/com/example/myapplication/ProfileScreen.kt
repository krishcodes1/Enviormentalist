package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onImpactHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: ""
    var username by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var tier by remember { mutableStateOf("silver") }
    var loading by remember { mutableStateOf(true) }
    var editingName by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var uploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Load user info from Firestore
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            loading = true
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (user != null) {
                        username = doc.getString("username") ?: user.displayName ?: "User"
                    }
                    profileImageUrl = doc.getString("profileImageUrl") ?: ""
                    tier = doc.getString("tier") ?: "silver"
                    loading = false
                }
                .addOnFailureListener { e ->
                    error = e.message
                    loading = false
                }
        }
    }

    // Image picker
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && uid.isNotBlank()) {
            uploading = true
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        profileImageUrl = url.toString()
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .update("profileImageUrl", url.toString())
                        uploading = false
                    }
                }
                .addOnFailureListener { e ->
                    error = e.message
                    uploading = false
                }
        }
    }

    // Badge color and label
    val (badgeColor, badgeLabel) = when (tier) {
        "diamond" -> Color(0xFF9B59FF) to "DIAMOND"
        "gold" -> Color(0xFFFFD700) to "GOLD"
        else -> Color(0xFFB0B0B0) to "SILVER"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .shadow(8.dp, CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUrl),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp).clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFB0B0B0),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .offset(x = (-8).dp, y = (-8).dp)
                        .background(Color(0xFF388E3C), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Image",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (editingName) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = {
                        if (newUsername.isNotBlank() && uid.isNotBlank()) {
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update("username", newUsername)
                            username = newUsername
                        }
                        editingName = false
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF388E3C))
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    IconButton(onClick = {
                        newUsername = username
                        editingName = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = Color.White)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 16.dp)
                    .background(badgeColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(badgeLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            // Options List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
            ) {
                Column(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
                    ProfileOption(Icons.Default.Person, "Account & Security")
                    ProfileOption(Icons.Default.Settings, "Settings")
                    ProfileOption(Icons.Default.Receipt, "Impact History", onClick = onImpactHistory)
                    ProfileOption(Icons.Default.LocationOn, "My Addresses")
                    ProfileOption(Icons.Default.Language, "Language")
                    ProfileOption(Icons.Default.Info, "About")
                    ProfileOption(Icons.Default.Help, "Help Center")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF194D32))
                    ) {
                        Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                confirmButton = {
                    Button(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                        showLogoutDialog = false
                    }) { Text("Logout") }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
                },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") }
            )
        }
        if (loading || uploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
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

@Composable
fun ProfileOption(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFF194D32), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = Color(0xFF194D32), fontWeight = FontWeight.Medium, fontSize = 18.sp)
    }
}
