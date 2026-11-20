package com.nimelssa.pdf

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
 * PDFReaderActivity - WPS-style offline PDF navigation
 * Per handoff spec: PDF reader screen with offline navigation
 */
class PDFReaderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PDFContent()
        }
    }
}

@Composable
fun PDFContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "PDF Reader",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.alignment(Alignment.Center)
            )
            Text(
                text = "WPS-style offline PDF navigation",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alignment(Alignment.Center)
            )
            // TODO: Add PDF page viewer, navigation controls, etc.
        }
    }
}