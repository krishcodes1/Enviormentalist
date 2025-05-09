package com.example.myapplication.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun CredentialsScreen(
    onBack: () -> Unit = {},
    onLogin: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onRegister: () -> Unit = {}
) {
    val darkGreen = Color(0xFF044717)

    Box(Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = darkGreen)
        }

        // Bottom sheet
        Surface(
            color = Color(0xFFFAF9F5),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter)
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Welcome Back!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = darkGreen
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "We’re so excited to see you again",
                    style = MaterialTheme.typography.bodyMedium,
                    color = darkGreen
                )
                Spacer(Modifier.height(24.dp))

                // Username
                var username by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Phone number or E-mail") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Password
                var password by remember { mutableStateOf("") }
                var visible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (visible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (visible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.Start)) {
                    Text("Forgot your password?", color = darkGreen)
                }
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Login", color = Color.White)
                }
                Spacer(Modifier.height(16.dp))

                Row {
                    Text("Don’t have an account? ", color = Color.Gray)
                    TextButton(onClick = onRegister) {
                        Text("Register", color = darkGreen)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CredentialsScreenPreview() {
    MyApplicationTheme {
        CredentialsScreen()
    }
}
