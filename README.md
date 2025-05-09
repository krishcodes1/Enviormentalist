Environmentalist Community App

A modern Android application built with Jetpack Compose that connects environmentalists and promotes community engagement in environmental activities.

Contributors

- [Your Name] - Project Lead & Full Stack Development
  - Implemented authentication system
  - Designed and developed UI components
  - Set up Firebase integration
  - Created event management system

- [Team Member 2] - Backend Development
  - Implemented database schema
  - Set up Firebase security rules
  - Created API endpoints
  - Managed data models

- [Team Member 3] - UI/UX Design
  - Designed user interface
  - Created app icons and assets
  - Implemented Material Design 3 components
  - Designed user flows

Features

1. User Authentication
Login/Register: Secure authentication system

Profile Management: Customize your profile and track your impact

Role-based Access: Different views for environmentalists and community members

2. Dashboard
Featured Events: Highlighted environmental events

Category Filtering: Browse events by categories (Popular, Pollution, Outdoor, Community)

Search Functionality: Find events and posts easily

Real-time Updates: Live feed of community activities

3. Community Feed
Post Creation: Share your environmental initiatives

Interactive Posts: Like, dislike, and comment on posts

Image Support: Upload images with your posts

Discussion Threads: Engage in meaningful conversations

4. Events
Event Discovery: Browse upcoming environmental events

Event Details: Comprehensive information about each event

RSVP System: Register for events you're interested in

Rating System: Rate and review events

5. Profile & Impact
Impact History: Track your environmental contributions

Activity Log: View your past engagements

Achievements: Monitor your environmental impact

User Guide

Installation
1. Download the app from the Google Play Store (link to be added)
2. Open the app and create an account
3. Verify your email address
4. Complete your profile setup

Basic Usage
1. Home Screen
   - View featured events
   - Browse community posts
   - Access quick actions

2. Events
   - Browse upcoming events
   - Filter by category
   - RSVP to events
   - View event details

3. Community
   - Create posts
   - Like and comment
   - Share environmental initiatives
   - Join discussions

4. Profile
   - View your impact
   - Track achievements
   - Manage settings
   - View activity history

Technology Stack

Hardware Requirements
- Android device with Android 7.0 (API level 24) or higher
- Minimum 2GB RAM
- 100MB free storage space
- Internet connection

Software Requirements
- Android Studio Arctic Fox (2020.3.1) or newer
- Android SDK 24 or higher
- Kotlin 1.8.0 or higher
- JDK 11 or higher
- Git for version control

Development Tools
- Android Studio
- Firebase Console
- Git
- Postman (for API testing)

Setup Guide

Development Environment Setup
1. Install Required Software
   # Install Android Studio
   # Download from: https://developer.android.com/studio

   # Install JDK 11
   # Download from: https://adoptium.net/

2. Clone the Repository
   git clone https://github.com/yourusername/environmentalist-app.git
   cd environmentalist-app

3. Firebase Setup
   - Create a new Firebase project at https://console.firebase.google.com
   - Enable Authentication (Email/Password and Google Sign-in)
   - Enable Firestore Database
   - Enable Storage
   - Download google-services.json and place it in the app directory

4. Build and Run
   # Open project in Android Studio
   # Sync Gradle files
   # Run the app on an emulator or physical device

Production Environment Setup
1. Firebase Configuration
   - Set up production Firebase project
   - Configure security rules
   - Set up backup and monitoring

2. App Signing
   - Generate release keystore
   - Configure signing in build.gradle
   - Create release build

3. Deployment
   - Upload to Google Play Console
   - Configure app settings
   - Submit for review

Packages and APIs

Firebase Services
1. Authentication
   - Purpose: Secure user authentication
   - Methods: Email/Password, Google Sign-in
   - Implementation: Firebase Auth SDK
   implementation(com.google.firebase:firebase-auth-ktx)

2. Firestore Database
   - Purpose: Real-time data storage
   - Structure: Collections and documents
   - Implementation: Firebase Firestore SDK
   implementation(com.google.firebase:firebase-firestore-ktx)

3. Storage
   - Purpose: Image and file storage
   - Implementation: Firebase Storage SDK
   implementation(com.google.firebase:firebase-storage-ktx)

UI Components
1. Jetpack Compose
   - Purpose: Modern UI toolkit
   - Version: 1.4.0
   - Implementation:
   implementation(androidx.compose.ui:ui)
   implementation(androidx.compose.material3:material3)

2. Coil
   - Purpose: Image loading
   - Version: 2.4.0
   - Implementation:
   implementation(io.coil-kt:coil-compose:2.4.0)

Navigation
1. Navigation Compose
   - Purpose: Screen navigation
   - Version: 2.7.7
   - Implementation:
   implementation(androidx.navigation:navigation-compose:2.7.7)

API Endpoints

Authentication
POST /auth/signup
POST /auth/login
POST /auth/google

Events
GET /events
GET /events/{id}
POST /events/{id}/rsvp

Posts
GET /posts
POST /posts
PUT /posts/{id}
DELETE /posts/{id}

User Profile
GET /users/{id}
PUT /users/{id}
GET /users/{id}/impact

Security

Authentication Methods
1. Email/Password
   - Secure password hashing
   - Email verification
   - Password reset functionality

2. Google Sign-in
   - OAuth 2.0 implementation
   - Secure token handling
   - Profile data synchronization

Data Security
1. Firestore Rules
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId} {
         allow read: if request.auth != null;
         allow write: if request.auth.uid == userId;
       }
     }
   }

2. Storage Rules
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /{allPaths=**} {
         allow read: if request.auth != null;
         allow write: if request.auth != null;
       }
     }
   }

Support

For support, please:
1. Check the FAQ (docs/FAQ.md)
2. Open an issue in the repository
3. Contact the development team at [support@environmentalist-app.com]

License

This project is licensed under the MIT License - see the LICENSE file for details.

Acknowledgments
- Material Design 3
- Jetpack Compose
- Firebase
- Coil
- Android Jetpack Libraries

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