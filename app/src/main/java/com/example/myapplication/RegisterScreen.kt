package com.example.myapplication

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val interests: List<String> = emptyList(),
    val communities: List<String> = emptyList(),
    val events: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onFacebook: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    var isGoogleLoading by remember { mutableStateOf(false) }
    val darkGreen = Color(0xFF194D32)
    val lightBg = Color(0xFFFAF9F5)

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result as GoogleSignInAccount
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            isGoogleLoading = true
            Firebase.auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        // Check if user exists in Firestore
                        val userDoc = Firebase.firestore.collection("users").document(user.uid)
                        userDoc.get().addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                // Create user document
                                val newUser = User(
                                    uid = user.uid,
                                    email = user.email ?: "",
                                    name = user.displayName ?: "",
                                    bio = ""
                                )
                                userDoc.set(newUser).addOnSuccessListener {
                                    isGoogleLoading = false
                                    onRegister()
                                }.addOnFailureListener { e ->
                                    isGoogleLoading = false
                                    errorMessage = e.message
                                }
                            } else {
                                isGoogleLoading = false
                                onRegister()
                            }
                        }.addOnFailureListener { e ->
                            isGoogleLoading = false
                            errorMessage = e.message
                        }
                    } else {
                        isGoogleLoading = false
                        errorMessage = "Google sign-in failed."
                    }
                }
                .addOnFailureListener { e ->
                    isGoogleLoading = false
                    errorMessage = e.message
                }
        } catch (e: Exception) {
            isGoogleLoading = false
            errorMessage = e.message
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBg)
    ) {
        // Top Row: Back button and logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = darkGreen,
                modifier = Modifier.size(56.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(56.dp)
            )
        }
        // Main Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Hi!",
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = darkGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Register yourself with us",
                fontSize = 20.sp,
                color = darkGreen
            )
            Spacer(modifier = Modifier.height(32.dp))
            // Email/Phone field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Phone number or E-mail") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkGreen, RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = Color.White)
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = Color.White)
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkGreen, RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Reppeat your password") },
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = Color.White)
                },
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(image, contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password", tint = Color.White)
                    }
                },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkGreen, RoundedCornerShape(20.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            // Sign Up button
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    try {
                        Firebase.auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { authResult ->
                                // Create user document in Firestore
                                val user = User(
                                    uid = authResult.user?.uid ?: "",
                                    email = email,
                                    name = name,
                                    bio = bio
                                )
                                Firebase.firestore.collection("users")
                                    .document(authResult.user?.uid ?: "")
                                    .set(user)
                                    .addOnSuccessListener {
                                        onRegister()
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = e.message
                                    }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = e.message
                            }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    } finally {
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGreen,
                    contentColor = Color.White
                ),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Sign Up", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Login link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Already have an account? ",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                TextButton(onClick = onLogin) {
                    Text(
                        "Login",
                        color = darkGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Or continue with
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                Text(
                    "  Or continue with  ",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Social login buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    IconButton(onClick = onFacebook) {
                        Icon(
                            painter = painterResource(R.drawable.ic_facebook),
                            contentDescription = "Facebook",
                            tint = Color.Unspecified
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    IconButton(onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        val signInIntent = googleSignInClient.signInIntent
                        isGoogleLoading = true
                        googleSignInLauncher.launch(signInIntent)
                    }) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_google),
                                contentDescription = "Google",
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    IconButton(onClick = onApple) {
                        Icon(
                            painter = painterResource(R.drawable.ic_apple),
                            contentDescription = "Apple",
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}
