package com.example.myapplication.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import androidx.compose.foundation.background


data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    // define your pages
    val pages = listOf(
        OnboardingPage(
            imageRes    = R.drawable.onboarding1,
            title       = "HELP THE COMMUNITY",
            description = "Post things you’re passionate about to help out the community around us."
        ),
        OnboardingPage(
            imageRes    = R.drawable.onboarding2,
            title       = "HELP US BECOME GREEN",
            description = "Nature is the best gift to man—let’s make it better one step at a time."
        ),
        OnboardingPage(
            imageRes    = R.drawable.onboarding3,
            title       = "GET YOUR OWN BADGE",
            description = "Earn badges and become the community leader by hosting events and helping the community."
        )
    )

    var pageIndex by remember { mutableStateOf(0) }
    val page = pages[pageIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // Illustration
        Image(
            painter        = painterResource(page.imageRes),
            contentDescription = null,
            contentScale   = ContentScale.Fit,
            modifier       = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Spacer(Modifier.height(32.dp))

        // Title
        Text(
            text      = page.title,
            style     = MaterialTheme.typography.headlineMedium,
            color     = Color(0xFF044717),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Description
        Text(
            text      = page.description,
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color(0xFF044717),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        // Page indicators
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.fillMaxWidth()
        ) {
            pages.forEachIndexed { index, _ ->
                val width  = if (index == pageIndex) 32.dp else 8.dp
                val color  = if (index == pageIndex) Color(0xFF044717) else Color.LightGray
                Box(
                    Modifier
                        .padding(4.dp)
                        .height(8.dp)
                        .width(width)
                        .background(color, RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Next / Get Started button
        val buttonText = if (pageIndex < pages.lastIndex) "Next" else "Get started"
        Button(
            onClick = {
                if (pageIndex < pages.lastIndex) {
                    pageIndex++
                } else {
                    onFinished()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(buttonText)
        }

        Spacer(Modifier.height(16.dp))
    }
}
