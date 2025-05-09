package com.example.myapplication.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import androidx.compose.ui.layout.ContentScale
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.material.icons.filled.ArrowBack
import com.google.firebase.storage.ktx.storage
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

// Data class for Event
data class UserEvent(
    val id: Int = 0,
    val title: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val attending: Int = 0,
    val category: String = "",
    val date: String = "" // format: yyyy-MM-dd
)

val eventCategories = listOf("All", "Cleanup", "Food Drive", "Tree Planting", "Recycling")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen() {
    var events by remember { mutableStateOf<List<UserEvent>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showPostDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<UserEvent?>(null) }

    // Function to update events optimistically
    fun updateEventsOptimistically(update: (List<UserEvent>) -> List<UserEvent>) {
        events = update(events)
    }

    // Set up real-time listener for events
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
            .orderBy("date")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    events = snapshot.documents.mapNotNull { doc ->
                        try {
                            UserEvent(
                                id = doc.getLong("id")?.toInt() ?: 0,
                                title = doc.getString("title") ?: "",
                                startTime = doc.getString("startTime") ?: "",
                                endTime = doc.getString("endTime") ?: "",
                                location = doc.getString("location") ?: "",
                                imageUrl = doc.getString("imageUrl") ?: "",
                                description = doc.getString("description") ?: "",
                                tags = (doc.get("tags") as? List<String>) ?: emptyList(),
                                attending = doc.getLong("attending")?.toInt() ?: 0,
                                category = doc.getString("category") ?: "",
                                date = doc.getString("date") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    loading = false
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            Modifier
                .matchParentSize()
                .background(Color(0xCCFAF9F5)) // semi-transparent overlay
        )
        if (selectedEvent == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAAFAF9F5))
                    .padding(bottom = 80.dp)
            ) {
                // Header
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ENVIRONMENTALIST EVENTS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Event", tint = Color.White)
                    }
                }
                // Filter Box
                Surface(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            placeholder = { Text("Search events...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        // Category dropdown
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                eventCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        // Date picker
                        OutlinedTextField(
                            value = selectedDate?.let { SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd").parse(it)!!) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pick a date") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            singleLine = true,
                            placeholder = { Text("Pick a date") }
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                searchQuery = ""
                                selectedCategory = "All"
                                selectedDate = null
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Clear Filters")
                            }
                        }
                    }
                }
                // Date display (if selected)
                selectedDate?.let {
                    Surface(
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd").parse(it)!!),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                // Upcoming Events Section
                Text(
                    "Upcoming Events",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(events) { event ->
                        EventCard(event = event, onViewDetails = { selectedEvent = event })
                    }
                }
            }
        } else {
            EventDetailsScreen(
                event = selectedEvent!!,
                onBack = { selectedEvent = null },
                onAttend = { event ->
                    // Optimistically update attendance
                    updateEventsOptimistically { currentEvents ->
                        currentEvents.map { e ->
                            if (e.id == event.id) {
                                e.copy(attending = e.attending + 1)
                            } else {
                                e
                            }
                        }
                    }

                    // Update in Firestore
                    val db = FirebaseFirestore.getInstance()
                    db.collection("events").document(event.id.toString())
                        .update("attending", event.attending + 1)
                        .addOnFailureListener { e ->
                            // Revert optimistic update on failure
                            updateEventsOptimistically { currentEvents ->
                                currentEvents.map { e ->
                                    if (e.id == event.id) {
                                        e.copy(attending = e.attending - 1)
                                    } else {
                                        e
                                    }
                                }
                            }
                            error = e.message
                        }
                }
            )
        }
        // Event creation dialog
        if (showDialog) {
            CreateEventDialog(
                onDismiss = { showDialog = false },
                onCreate = { newEvent ->
                    // Optimistically add the new event
                    updateEventsOptimistically { currentEvents ->
                        currentEvents + newEvent
                    }

                    // Update in Firestore
                    val db = FirebaseFirestore.getInstance()
                    db.collection("events")
                        .add(newEvent)
                        .addOnFailureListener { e ->
                            // Revert optimistic update on failure
                            updateEventsOptimistically { currentEvents ->
                                currentEvents.filter { it.id != newEvent.id }
                            }
                            error = e.message
                        }
                    showDialog = false
                }
            )
        }
        // Date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                val datePickerState = rememberDatePickerState()
                DatePicker(state = datePickerState)
                LaunchedEffect(datePickerState.selectedDateMillis) {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(onDismiss: () -> Unit, onCreate: (UserEvent) -> Unit) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(eventCategories[1]) }
    var tags by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Date and time pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                uploading = true
                uploadError = "Starting upload..."
                
                // Get file extension from URI
                val mimeType = context.contentResolver.getType(uri)
                val extension = when (mimeType) {
                    "image/jpeg" -> ".jpg"
                    "image/png" -> ".png"
                    else -> ".jpg" // Default to jpg if unknown
                }
                
                // Create a unique filename with timestamp and proper extension
                val timestamp = System.currentTimeMillis()
                val filename = "events/$timestamp$extension"
                
                // Get the storage reference
                val storage = Firebase.storage
                val storageRef = storage.reference.child(filename)
                
                // Get the input stream from the URI
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    uploadError = "Could not open image file"
                    uploading = false
                    return@rememberLauncherForActivityResult
                }
                
                // Create metadata for the upload
                val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                    .setContentType(mimeType)
                    .build()
                
                // Upload the file with metadata
                val uploadTask = storageRef.putStream(inputStream, metadata)
                
                uploadTask
                    .addOnSuccessListener {
                        uploadError = "Upload successful, getting download URL..."
                        // Get download URL after successful upload
                        storageRef.downloadUrl
                            .addOnSuccessListener { url ->
                                imageUrl = url.toString()
                                imageUri = uri
                                uploading = false
                                uploadError = null
                            }
                            .addOnFailureListener { e ->
                                uploadError = "Failed to get download URL: ${e.message}"
                                uploading = false
                            }
                    }
                    .addOnFailureListener { e ->
                        uploadError = "Upload failed: ${e.message}"
                        uploading = false
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                        uploadError = "Uploading: $progress%"
                    }
            } catch (e: Exception) {
                uploadError = "Error processing image: ${e.message}"
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
                    if (imageUrl.isBlank() && imageUri != null) {
                        uploadError = "Please wait for the image to finish uploading."
                        creating = false
                    } else {
                        onCreate(
                            UserEvent(
                                id = 0,
                                title = title,
                                startTime = startTime,
                                endTime = endTime,
                                location = location,
                                imageUrl = imageUrl,
                                description = description,
                                tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                attending = 0,
                                category = category,
                                date = date
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank() && location.isNotBlank() && description.isNotBlank() && date.isNotBlank() && !uploading && !creating
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
        title = { Text("Create Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                // Start Time Picker
                OutlinedTextField(
                    value = startTime,
                    onValueChange = {},
                    label = { Text("Start Time") },
                    readOnly = true,
                    modifier = Modifier.clickable { showStartTimePicker = true },
                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                )
                // End Time Picker
                OutlinedTextField(
                    value = endTime,
                    onValueChange = {},
                    label = { Text("End Time") },
                    readOnly = true,
                    modifier = Modifier.clickable { showEndTimePicker = true },
                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                )
                // Date Picker
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Date (yyyy-MM-dd)") },
                    readOnly = true,
                    modifier = Modifier.clickable { showDatePicker = true },
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                // Category dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        eventCategories.drop(1).forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags (comma separated)") })
                // Image picker
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

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(state = datePickerState)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val pickedDate = Date(millis)
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(pickedDate)
                }
            }
        }
    }
    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showStartTimePicker = false; startTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute) }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Pick Start Time") },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
    // End Time Picker Dialog
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showEndTimePicker = false; endTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute) }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Pick End Time") },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
fun EventCard(event: UserEvent, onViewDetails: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column {
            if (event.imageUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(event.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder image
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(64.dp))
                }
            }
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF194D32)
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${event.date}  ${event.startTime} - ${event.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(event.location, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(event.category, color = Color(0xFF388E3C), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                    }
                    event.tags.forEach { tag ->
                        Surface(
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(tag, color = Color.Gray, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(event.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${event.attending} attending", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("View Details", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun JoinSuccessScreen(points: Int, onContinue: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onContinue,
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
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF5))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Yay! +$points points", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = Color(0xFF194D32), modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF194D32), modifier = Modifier.size(120.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Successful!", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = Color(0xFF194D32))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Thank you for your Donation", fontSize = 18.sp, color = Color(0xFF194D32), modifier = Modifier.padding(top = 8.dp))
                    Text("You will receive the details on the event's\nin your email!!", fontSize = 16.sp, color = Color(0xFF194D32), modifier = Modifier.padding(top = 4.dp), lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Together, We Create Change—Thank You!✨", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF194D32), modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF194D32))
                    ) {
                        Text("Continue Helping", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailsScreen(event: UserEvent, onBack: () -> Unit, onAttend: (UserEvent) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: ""
    var joining by remember { mutableStateOf(false) }
    var joinError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var points by remember { mutableStateOf(0) }
    if (showSuccess) {
        JoinSuccessScreen(points = points, onContinue = onBack)
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAF9F5))) {
        // Top background and image
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Top bar
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(16.dp))) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF044717))
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                }
                // Event image
                Image(
                    painter = rememberAsyncImagePainter(event.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 48.dp)
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        // Details card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .padding(24.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF044717))
                Text(event.location.take(10), style = MaterialTheme.typography.titleMedium, color = Color(0xFF044717))
                Button(onClick = { /* TODO: Trash action */ }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF044717))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                event.tags.forEach { tag ->
                    Surface(
                        color = Color(0xFF044717),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(tag, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                Text("4.5 BY– Dr.Krish", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Text(event.description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF044717))
            Spacer(Modifier.height(16.dp))
            // Stats
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF044717), modifier = Modifier.size(32.dp))
                    Text("Signed up", color = Color(0xFF044717), style = MaterialTheme.typography.labelMedium)
                    Text("30,People", color = Color(0xFF044717), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.InvertColors, contentDescription = null, tint = Color(0xFF044717), modifier = Modifier.size(32.dp))
                    Text("Humidity", color = Color(0xFF044717), style = MaterialTheme.typography.labelMedium)
                    Text("65%", color = Color(0xFF044717), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.OpenInFull, contentDescription = null, tint = Color(0xFF044717), modifier = Modifier.size(32.dp))
                    Text("Size", color = Color(0xFF044717), style = MaterialTheme.typography.labelMedium)
                    Text("Large", color = Color(0xFF044717), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF044717), modifier = Modifier.size(32.dp))
                    Text("Donations", color = Color(0xFF044717), style = MaterialTheme.typography.labelMedium)
                    Text("\$FREE", color = Color(0xFF044717), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { /* TODO: Trash action */ }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF044717))
                }
                Button(onClick = {
                    if (uid.isNotBlank()) {
                        joining = true
                        val db = FirebaseFirestore.getInstance()
                        val joinedEvent = mapOf(
                            "eventId" to event.id.toString(),
                            "title" to event.title,
                            "city" to event.location,
                            "date" to event.date,
                            "time" to event.startTime,
                            "address" to event.location
                        )
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                val current = (doc.get("joinedEvents") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                                val alreadyJoined = current.any { (it["eventId"] as? String) == event.id.toString() }
                                if (!alreadyJoined) {
                                    current.add(joinedEvent)
                                    db.collection("users").document(uid)
                                        .update("joinedEvents", current)
                                        .addOnSuccessListener {
                                            joining = false
                                            points = Random.nextInt(10, 51)
                                            showSuccess = true
                                            onAttend(event.copy(attending = event.attending + 1))
                                        }
                                        .addOnFailureListener { e -> joinError = e.message; joining = false }
                                } else {
                                    joining = false // Already joined
                                }
                            }
                            .addOnFailureListener { e -> joinError = e.message; joining = false }
                    }
                }, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF044717)), modifier = Modifier.weight(1f).padding(horizontal = 16.dp), enabled = !joining) {
                    Text(if (joining) "Joining..." else "JOIN NOW", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                Button(onClick = { /* TODO: Favorite */ }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFF044717))
                }
            }
        }
    }

    if (joinError != null) {
        AlertDialog(
            onDismissRequest = { joinError = null },
            confirmButton = { Button(onClick = { joinError = null }) { Text("OK") } },
            title = { Text("Error Joining Event") },
            text = { Text(joinError ?: "") }
        )
    }
}

fun addEventToFirestore(
    event: UserEvent,
    onSuccess: () -> Unit = {},
    onFailure: (Exception) -> Unit = {}
) {
    val db = Firebase.firestore
    db.collection("events")
        .add(event)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e) }
}

fun loadEventsFromFirestore(
    onResult: (List<UserEvent>) -> Unit,
    onError: (Exception) -> Unit = {}
) {
    val db = Firebase.firestore
    db.collection("events")
        .get()
        .addOnSuccessListener { result ->
            val events = result.mapNotNull { it.toObject(UserEvent::class.java) }
            onResult(events)
        }
        .addOnFailureListener { e -> onError(e) }
}