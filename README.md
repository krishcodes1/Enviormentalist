Environmentalist_v1.0 Instructions 

# Environmentalist Community App

A modern Android application built with Jetpack Compose that connects environmentalists and promotes community engagement in environmental activities. (Seeding Instrcutions to the bottom )

---

## 📌 Features

### 🔐 Authentication
- Login and register securely
- Role-based access for community members and environmentalists
- Email verification and password reset

### 📊 Dashboard
- View featured and upcoming events
- Filter by category: Popular, Pollution, Outdoor, Community
- Search events and posts
- Real-time updates from the community

### 🌱 Community Feed
- Create and share posts
- Like, comment, and engage in discussions
- Upload images with posts

### 📅 Events
- Discover and RSVP to local and global environmental events
- View event details and ratings
- Manage attendance

### 👤 Profile & Impact
- View environmental contributions
- Track activity and achievements
- Edit profile details

---

## 👥 Contributors

- **Krish Shroff** – Project Lead & Full Stack Developer  
  - Implemented authentication system and Firebase integration  
  - Designed and developed UI components  
  - Built the event management system  
  - Led the team and coordinated development efforts
  - Testing and Troubleshooting    

- **Aminuz** – Backend Developer (Support Role)  
  - Assisted with backend logic and project setup  

- **Awias** – Backend Developer  
  - Created backend structure and initial configurations  
  - Helped define database schemas  
  - Contributed ideas and supported overall development  

- **Ayon** – Team Supporter  
  - Provided assistance with coordination and feedback  
  - Supported team communication and progress  

- **Adrian** – Team Supporter  
  - Participated in team discussions and presentations  
  - Assisted with group documentation and logistics

---

## 🚀 User Guide

### 📲 Installation
1. Download the app from the [Google Play Store](#) *(link coming soon)*
2. Open the app and create an account
3. Verify your email address
4. Complete your profile setup

### 🧭 Basic Navigation
- **Home**: View featured events & quick actions  
- **Community**: Engage with environmental posts  
- **Events**: RSVP, filter, and explore  
- **Profile**: Track impact and manage settings  

---

## 🛠️ Technology Stack

### 💻 Requirements

#### Hardware
- Android device with Android 7.0+ (API 24+)
- Minimum 2GB RAM and 100MB free storage

#### Software
- Android Studio Arctic Fox (2020.3.1) or newer
- Kotlin 1.8.0+, JDK 11+
- Git for version control

### 🧰 Tools
- Android Studio  
- Firebase Console  
- Git  
- Postman (for API testing)

---

## ⚙️ Development Setup

### Step 1: Install Required Software
```bash
# Android Studio
https://developer.android.com/studio

# JDK 11
https://adoptium.net/
```

### Step 2: Clone the Repository
```bash
git clone [https://github.com/krishcodes1/Environmentalist_v1.0]
cd environmentalist-app
```

### Step 3: Configure Firebase
- Create Firebase project: https://console.firebase.google.com  
- Enable:
  - Email/Password Auth
  - Firestore
  - Firebase Storage  
- Download `google-services.json` and place in `app/`

### Step 4: Build & Run
- Open project in Android Studio  
- Sync Gradle  
- Run the app on an emulator or device  

---

## 🌐 Production Setup

### Firebase
- Separate production project
- Security rules and monitoring configured

### App Signing & Deployment
```bash
# Generate keystore
# Add signingConfigs to build.gradle
# Create release build and upload to Google Play Console
```

---

## 📦 Packages & APIs

### 🔐 Firebase Authentication
```kotlin
implementation("com.google.firebase:firebase-auth-ktx")
```

### 🗂️ Firestore Database
```kotlin
implementation("com.google.firebase:firebase-firestore-ktx")
```

### 🖼️ Firebase Storage
```kotlin
implementation("com.google.firebase:firebase-storage-ktx")
```

### 🎨 UI Libraries
```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Coil for image loading
implementation("io.coil-kt:coil-compose:2.4.0")
```

### 📍 Navigation
```kotlin
implementation("androidx.navigation:navigation-compose:2.7.7")
```

---

## 🔗 API Endpoints

### Authentication
```
POST /auth/signup
POST /auth/login
POST /auth/google
```

### Events
```
GET /events
GET /events/{id}
POST /events/{id}/rsvp
```

### Posts
```
GET /posts
POST /posts
PUT /posts/{id}
DELETE /posts/{id}
```

### User Profile
```
GET /users/{id}
PUT /users/{id}
GET /users/{id}/impact
```

---

## 🔒 Security

### Firestore Rules
```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
  }
}
```

### Storage Rules
```js
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```


Codebase Documentation

Main Components:

1. MainActivity.kt
   - Entry point of the application
   - Handles navigation between screens
   - Initializes Firebase
   - Manages authentication state

2. LoginScreen.kt
   - Handles user authentication
   - Implements email/password login
   - Integrates Google Sign-in
   - Manages login state and error handling

3. RegisterScreen.kt
   - User registration functionality
   - Form validation
   - Firebase user creation
   - Profile data initialization

4. DashboardScreen.kt
   - Main application interface
   - Displays events and posts
   - Handles user interactions
   - Manages real-time updates

5. EventCard.kt
   - Displays event information
   - Handles RSVP functionality
   - Manages event ratings
   - Implements event sharing

Database Schema and Scripts

1. Users Collection
   ```javascript
   // Users Collection Structure
   users/{userId} {
     uid: string,
     email: string,
     name: string,
     bio: string,
     profileImageUrl: string,
     interests: array<string>,
     communities: array<string>,
     events: array<string>,
     createdAt: timestamp,
     lastLogin: timestamp
   }
   ```

2. Events Collection
   ```javascript
   // Events Collection Structure
   events/{eventId} {
     id: string,
     title: string,
     description: string,
     startDate: timestamp,
     endDate: timestamp,
     location: {
       address: string,
       latitude: number,
       longitude: number
     },
     organizer: {
       id: string,
       name: string
     },
     category: string,
     imageUrl: string,
     participants: array<string>,
     rating: number,
     createdAt: timestamp
   }
   ```

3. Posts Collection
   ```javascript
   // Posts Collection Structure
   posts/{postId} {
     id: string,
     author: {
       id: string,
       name: string,
       profileImageUrl: string
     },
     content: string,
     imageUrl: string,
     likes: array<string>,
     dislikes: array<string>,
     comments: array<{
       id: string,
       author: {
         id: string,
         name: string
       },
       content: string,
       createdAt: timestamp
     }>,
     createdAt: timestamp
   }
   ```

4. Comments Collection
   ```javascript
   // Comments Collection Structure
   comments/{commentId} {
     id: string,
     postId: string,
     author: {
       id: string,
       name: string,
       profileImageUrl: string
     },
     content: string,
     likes: array<string>,
     createdAt: timestamp
   }
   ```

Sample Data Insertion Scripts:

1. Create User
   ```javascript
   // Create new user
   db.collection('users').doc(userId).set({
     uid: userId,
     email: 'user@example.com',
     name: 'John Doe',
     bio: 'Environmental enthusiast',
     profileImageUrl: 'https://example.com/profile.jpg',
     interests: ['recycling', 'cleanup'],
     communities: [],
     events: [],
     createdAt: firebase.firestore.FieldValue.serverTimestamp(),
     lastLogin: firebase.firestore.FieldValue.serverTimestamp()
   });
   ```

2. Create Event
   ```javascript
   // Create new event
   db.collection('events').doc(eventId).set({
     id: eventId,
     title: 'Beach Cleanup',
     description: 'Join us for a beach cleanup event',
     startDate: new Date('2024-04-01T10:00:00'),
     endDate: new Date('2024-04-01T14:00:00'),
     location: {
       address: '123 Beach Road',
       latitude: 40.7128,
       longitude: -74.0060
     },
     organizer: {
       id: 'org123',
       name: 'Eco Warriors'
     },
     category: 'cleanup',
     imageUrl: 'https://example.com/event.jpg',
     participants: [],
     rating: 0,
     createdAt: firebase.firestore.FieldValue.serverTimestamp()
   });
   ```

3. Create Post
   ```javascript
   // Create new post
   db.collection('posts').doc(postId).set({
     id: postId,
     author: {
       id: userId,
       name: 'John Doe',
       profileImageUrl: 'https://example.com/profile.jpg'
     },
     content: 'Just completed a beach cleanup!',
     imageUrl: 'https://example.com/post.jpg',
     likes: [],
     dislikes: [],
     comments: [],
     createdAt: firebase.firestore.FieldValue.serverTimestamp()
   });
   ```

4. Create Comment
   ```javascript
   // Create new comment
   db.collection('comments').doc(commentId).set({
     id: commentId,
     postId: postId,
     author: {
       id: userId,
       name: 'Jane Smith',
       profileImageUrl: 'https://example.com/jane.jpg'
     },
     content: 'Great work!',
     likes: [],
     createdAt: firebase.firestore.FieldValue.serverTimestamp()
   });
   ```

Indexes:
```javascript
// Required Firestore indexes
{
  "indexes": [
    {
      "collectionGroup": "events",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "category", "order": "ASCENDING" },
        { "fieldPath": "startDate", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "posts",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "author.id", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    }
  ]
}
```

-------------
**SEEDING** 
- To seed the database, replace the contents of MainActivity.kt with the code from MainActivitySeed.kt-(No longer needed I have merged file ). Relaunch the app and tap the 'Seed Database' button.


```

package com.example.myapplication.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.util.*
import kotlin.random.Random

class DatabaseSeeder {
    private val db = FirebaseFirestore.getInstance()
    private val random = Random(System.currentTimeMillis())

    // Sample data arrays
    private val firstNames = listOf("John", "Jane", "Michael", "Sarah", "David", "Emma", "James", "Olivia")
    private val lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller")
    private val interests = listOf("recycling", "cleanup", "conservation", "sustainability", "gardening", "wildlife")
    private val eventTitles = listOf(
        "Beach Cleanup", "Tree Planting", "Recycling Drive",
        "Community Garden", "Wildlife Conservation", "Eco Workshop"
    )
    private val eventDescriptions = listOf(
        "Join us for a community cleanup event",
        "Help us plant trees in the local park",
        "Learn about recycling and sustainability",
        "Participate in our community garden project",
        "Support local wildlife conservation efforts",
        "Attend our environmental workshop"
    )
    private val locations = listOf(
        mapOf(
            "address" to "123 Beach Road",
            "latitude" to 40.7128,
            "longitude" to -74.0060
        ),
        mapOf(
            "address" to "456 Park Avenue",
            "latitude" to 40.7829,
            "longitude" to -73.9654
        ),
        mapOf(
            "address" to "789 Garden Street",
            "latitude" to 40.7589,
            "longitude" to -73.9851
        )
    )

    fun seedDatabase() {
        // Create 10 random users
        repeat(10) {
            createRandomUser()
        }

        // Create 15 random events
        repeat(15) {
            createRandomEvent()
        }

        // Create 20 random posts
        repeat(20) {
            createRandomPost()
        }
    }

    private fun createRandomUser() {
        val userId = UUID.randomUUID().toString()
        val firstName = firstNames.random()
        val lastName = lastNames.random()
        
        val user = mapOf(
            "uid" to userId,
            "email" to "${firstName.lowercase()}.${lastName.lowercase()}@example.com",
            "name" to "$firstName $lastName",
            "bio" to "Environmental enthusiast passionate about ${interests.random()}",
            "profileImageUrl" to "https://example.com/profiles/${userId}.jpg",
            "interests" to interests.shuffled().take(random.nextInt(1, 4)),
            "communities" to listOf<String>(),
            "events" to listOf<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "lastLogin" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(userId).set(user)
    }

    private fun createRandomEvent() {
        val eventId = UUID.randomUUID().toString()
        val startDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, random.nextInt(1, 30))
            set(Calendar.HOUR_OF_DAY, random.nextInt(9, 17))
        }.time

        val endDate = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.HOUR_OF_DAY, random.nextInt(2, 5))
        }.time

        val event = mapOf(
            "id" to eventId,
            "title" to eventTitles.random(),
            "description" to eventDescriptions.random(),
            "startDate" to startDate,
            "endDate" to endDate,
            "location" to locations.random(),
            "organizer" to mapOf(
                "id" to UUID.randomUUID().toString(),
                "name" to "${firstNames.random()} ${lastNames.random()}"
            ),
            "category" to listOf("cleanup", "conservation", "education", "community").random(),
            "imageUrl" to "https://example.com/events/${eventId}.jpg",
            "participants" to listOf<String>(),
            "rating" to random.nextDouble(3.0, 5.0),
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("events").document(eventId).set(event)
    }

    private fun createRandomPost() {
        val postId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val firstName = firstNames.random()
        val lastName = lastNames.random()

        val post = mapOf(
            "id" to postId,
            "author" to mapOf(
                "id" to userId,
                "name" to "$firstName $lastName",
                "profileImageUrl" to "https://example.com/profiles/${userId}.jpg"
            ),
            "content" to "Just participated in ${eventTitles.random()}! It was amazing to see the community come together for environmental conservation.",
            "imageUrl" to "https://example.com/posts/${postId}.jpg",
            "likes" to listOf<String>(),
            "dislikes" to listOf<String>(),
            "comments" to listOf<Map<String, Any>>(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("posts").document(postId).set(post)

        // Add 2-5 random comments to each post
        repeat(random.nextInt(2, 6)) {
            createRandomComment(postId)
        }
    }

    private fun createRandomComment(postId: String) {
        val commentId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val firstName = firstNames.random()
        val lastName = lastNames.random()

        val comment = mapOf(
            "id" to commentId,
            "postId" to postId,
            "author" to mapOf(
                "id" to userId,
                "name" to "$firstName $lastName",
                "profileImageUrl" to "https://example.com/profiles/${userId}.jpg"
            ),
            "content" to listOf(
                "Great initiative!",
                "Count me in for the next one!",
                "This is exactly what our community needs.",
                "Amazing work everyone!",
                "Looking forward to more events like this."
            ).random(),
            "likes" to listOf<String>(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("comments").document(commentId).set(comment)
    }
}
```
---

## 📞 Support

- Contact: Kshroff@nyit.edu

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) © 2025 Krish Shroff and the Environmentalist Group.

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Firebase](https://firebase.google.com/)
- [Material Design 3](https://m3.material.io/)
- [Coil](https://coil-kt.github.io/coil/)


