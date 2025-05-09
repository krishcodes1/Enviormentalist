package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

// Data class for joined event
data class JoinedEvent(
    val eventId: String = "",
    val title: String = "",
    val city: String = "",
    val date: String = "", // yyyy-MM-dd
    val time: String = "", // HH:mm
    val address: String = ""
)

@Composable
fun ImpactHistoryScreen(onBack: () -> Unit = {}) {
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: ""
    var joinedEvents by remember { mutableStateOf<List<JoinedEvent>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Function to update joined events optimistically
    fun updateJoinedEventsOptimistically(update: (List<JoinedEvent>) -> List<JoinedEvent>) {
        joinedEvents = update(joinedEvents)
    }

    // Set up real-time listener for joined events
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        error = e.message
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val events = (snapshot.get("joinedEvents") as? List<Map<String, Any>>)?.mapNotNull { map ->
                            try {
                                JoinedEvent(
                                    eventId = map["eventId"] as? String ?: "",
                                    title = map["title"] as? String ?: "",
                                    city = map["city"] as? String ?: "",
                                    date = map["date"] as? String ?: "",
                                    time = map["time"] as? String ?: "",
                                    address = map["address"] as? String ?: ""
                                )
                            } catch (e: Exception) {
                                null
                            }
                        } ?: emptyList()
                        joinedEvents = events.sortedByDescending { it.date + it.time }
                        loading = false
                    }
                }
        }
    }

    // Group events by Today, Yesterday, Earlier
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        .let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.time) }
    val todayEvents = joinedEvents.filter { it.date == today }
    val yesterdayEvents = joinedEvents.filter { it.date == yesterday }
    val earlierEvents = joinedEvents.filter { it.date != today && it.date != yesterday }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF194D32))
                }
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                "Impact History",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp, end = 16.dp)
            )
            // Today
            if (todayEvents.isNotEmpty()) {
                SectionTitle("Today")
                todayEvents.forEach { event ->
                    ImpactEventCard(event)
                }
            }
            // Yesterday
            if (yesterdayEvents.isNotEmpty()) {
                SectionTitle("Yesterday")
                yesterdayEvents.forEach { event ->
                    ImpactEventCard(event)
                }
            }
            // Earlier
            if (earlierEvents.isNotEmpty()) {
                SectionTitle("Earlier")
                earlierEvents.forEach { event ->
                    ImpactEventCard(event)
                }
            }
        }
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)),
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

    // Update when joining a new event
    fun onJoinEvent(newEvent: JoinedEvent) {
        // Optimistically add the new event
        updateJoinedEventsOptimistically { currentEvents ->
            (currentEvents + newEvent).sortedByDescending { it.date + it.time }
        }

        // Update in Firestore
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)
        userRef.get().addOnSuccessListener { doc ->
            val currentEvents = (doc.get("joinedEvents") as? List<Map<String, Any>>) ?: emptyList()
            val updatedEvents = currentEvents + mapOf(
                "eventId" to newEvent.eventId,
                "title" to newEvent.title,
                "city" to newEvent.city,
                "date" to newEvent.date,
                "time" to newEvent.time,
                "address" to newEvent.address
            )
            userRef.update("joinedEvents", updatedEvents)
                .addOnFailureListener { e ->
                    // Revert optimistic update on failure
                    updateJoinedEventsOptimistically { currentEvents ->
                        currentEvents.filter { it.eventId != newEvent.eventId }
                    }
                    error = e.message
                }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White),
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp, end = 16.dp)
    )
}

@Composable
fun ImpactEventCard(event: JoinedEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (event.date < SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF194D32),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF194D32))
                Spacer(Modifier.weight(1f))
                Text(event.city, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF194D32))
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Text(event.date, fontSize = 16.sp, color = Color(0xFF194D32))
                Spacer(Modifier.width(16.dp))
                Text(event.time, fontSize = 16.sp, color = Color(0xFF194D32))
            }
            Spacer(Modifier.height(8.dp))
            Text("Address", fontWeight = FontWeight.Bold, color = Color(0xFF194D32))
            Text(event.address, fontSize = 16.sp, color = Color(0xFF194D32))
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { /* TODO: Show more details */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF194D32))
            ) {
                Text(if (event.date == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) "View More Details" else "More Details", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
} 