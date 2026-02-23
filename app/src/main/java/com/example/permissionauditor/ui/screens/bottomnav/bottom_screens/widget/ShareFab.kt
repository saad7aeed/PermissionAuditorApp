package com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.widget

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun ShareFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,  // Background of FAB
        contentColor = MaterialTheme.colorScheme.error,  // Icon color
        shape = androidx.compose.foundation.shape.CircleShape, // Optional: perfect circle
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp
        ) // optional elevation
    ) {
        Icon(
            imageVector = Icons.Default.Replay,
            contentDescription = "Share"
        )
    }
}
