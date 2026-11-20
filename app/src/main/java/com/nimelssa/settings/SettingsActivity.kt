package com.nimelssa.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * SettingsActivity - Contains About card with creator info
 * Per handoff spec: Settings → About card with creator credits
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsContent()
        }
    }
}

@Composable
fun SettingsContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "About NIMelSSA",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.alignment(Alignment.CenterStart)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colors.surface),
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Agboola Israel Oluwagbogo",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.alignment(Alignment.CenterStart)
                )

                Text(
                    text = "MLS student, University of Ilorin",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alignment(Alignment.CenterStart)
                )

                Text(
                    text = "Not owned or endorsed by the University",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    modifier = Modifier.alignment(Alignment.CenterStart)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // GitHub link area
                    TextButton(
                        onClick = { /* open GitHub */ },
                        modifier = androidx.compose.uiModifier.remember { androidx.compose.ui.Modifier.weight(1f) }
                    ) {
                        Text(text = "GitHub @isygold", style = MaterialTheme.typography.bodySmall)
                    }
                    // Discord link area
                    TextButton(
                        onClick = { /* open Discord */ },
                        modifier = androidx.compose.uiModifier.remember { androidx.compose.ui.Modifier.weight(1f) }
                    ) {
                        Text(text = "Discord", style = MaterialTheme.typography.bodySmall)
                    }
                    // Telegram link area
                    TextButton(
                        onClick = { /* open Telegram */ },
                        modifier = androidx.compose.uiModifier.remember { androidx.compose.ui.Modifier.weight(1f) }
                    ) {
                        Text(text = "Telegram t.me/isygold", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}