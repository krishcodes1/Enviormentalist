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