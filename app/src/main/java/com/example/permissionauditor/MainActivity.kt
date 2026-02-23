package com.example.permissionauditor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.permissionauditor.navigation.AppNavGraph
import com.example.permissionauditor.ui.theme.PermissionAuditorTheme

class MainActivity : ComponentActivity() {

    // ✅ Use mutableStateOf to make it observable by Compose
    private var pendingNavigation by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Check for navigation intent
        pendingNavigation = intent.getStringExtra("navigate_to")

        setContent {
            PermissionAuditorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        navigateToRoute = pendingNavigation
                    )
                }
            }
        }
    }

    // ✅ Handle notification clicks when app is already open
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo != null) {
            // Update the state - this will trigger recomposition
            pendingNavigation = navigateTo

            // Recreate the activity to navigate
            recreate()
        }
    }
}