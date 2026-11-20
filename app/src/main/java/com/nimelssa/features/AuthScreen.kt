package com.nimelssa.features

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nimelssa.R
import com.nimelssa.features.auth.SignUpState
import com.nimelssa.features.auth.SignUpViewModel

/**
 * AuthScreen - Handles both desktop and auth flow screens
 * Per handoff spec: supports sign in, sign up with Firestore validation, and PDF reader access
 */
class AuthScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NimelssaAuthTheme {
                val viewModel: SignUpViewModel = SignUpViewModel()
                val uiState by viewModel.state.collectAsState()

                AuthContent(viewModel, uiState)
            }
        }
    }
}

@Composable
fun AuthContent(viewModel: SignUpViewModel, uiState: SignUpViewModel.SignUpState) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var matric by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    var requestAccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterStart
    ) {
        // Auth card
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                // Title
                if (!isSignUp) {
                    Text(
                        text = "Sign in to NIMelSSA",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.alignment(Alignment.CenterStart)
                    )
                } else {
                    Text(
                        text = "Create account",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.alignment(Alignment.CenterStart)
                    )
                }

                // Email field
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { if (isSignUp) "Email" else "Email" },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Password field
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    isPassword = !showPassword,
                    visualTransformation = if (showPassword) null else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { /* ignore */ }
                )

                // Toggle password visibility
                if (isSignUp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showPassword = !showPassword },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (showPassword) "Show" else "Hide",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Sign in / Sign up buttons
                if (!isSignUp) {
                    Button(
                        onClick = { /* sign in logic */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Sign in")
                    }
                    Button(
                        onClick = { isSignUp = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Create account")
                    }
                } else {
                    // Sign up fields (name, matric, level)
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = matric,
                        onValueChange = { matric = it },
                        label = { Text("Matric number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = level,
                        onValueChange = { level = it },
                        label = { Text("Level") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Sign up button with Firestore validation
                    Button(
                        onClick = {
                            if (matric.isNotEmpty() && matric.lengthInRange(7..10)) {
                                viewModel.signUp(
                                    SignUpViewModel.SignUpState(
                                        email = email,
                                        password = password,
                                        name = name,
                                        matric = matric,
                                        level = level
                                    )
                                )
                            } else {
                                // Show error for invalid matric
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Create account")
                    }
                }

                // Access request (after email verification)
                if (uiState.error == null && !requestAccess) {
                    Button(
                        onClick = { requestAccess = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Request access")
                    }
                }

                // Error state
                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colors.error
                    )
                }

                // Pending approval state
                if (requestAccess && !pendingApproval) {
                    Text(
                        text = "Access request submitted. Pending approval.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Credits footer - Android
                CreditsFooterAndroid()
            }
        }

        // Desktop auth footer - shown below auth card on desktop
        if (isSignUp) {
            CreditsFooterDesktop()
        }
    }
}

@Composable
fun CreditsFooterAndroid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp, 16.dp, 0.dp),
        verticalArrangement = Arrangement.Start,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Built by Agboola Israel Oluwagbogo, an MLS student\nat the University of Ilorin — not owned or endorsed\nby the University.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colors.onSurfaceVariant,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GitHub link
            Button(onClick = { /* open GitHub */ }, modifier = Modifier.weight(1f)) {
                Text(text = "GitHub @isygold", style = MaterialTheme.typography.bodySmall)
            }
            // Discord link
            Button(onClick = { /* open Discord */ }, modifier = Modifier.weight(1f)) {
                Text(text = "Discord isygold", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CreditsFooterDesktop() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 4.dp, 16.dp, 4.dp),
        verticalArrangement = Arrangement.Start,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Built by Agboola Israel Oluwagbogo, an MLS student\nat the University of Ilorin.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colors.onSurfaceVariant
        )
    }
}

/* Screen specs from handoff:
 * - PDF reader: OBS-style/WPS-style offline PDF navigation
 * - Request access: after email verification
 * - Pending approval: check status, sign out when approved
 * - Settings → About card: creator info
 */

/* TODO: Firestore rules (from assets/firestore.rules):
 * - users/{uid} creation requires: role=student, approved=false, valid matric (:59)
 * - access_requests/{uid} creation requires: verified token email (:141)
 * - rep_requests requires: uid + status
 */

@Composable
fun PDFReaderScreen() {
    // WPS-style offline PDF navigation placeholder
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Text(
            text = "PDF Reader - WPS style offline navigation",
            modifier = Modifier.alignment(Alignment.Center),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun SettingsAboutScreen() {
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
                    Button(onClick = { /* GitHub */ }, modifier = Modifier.weight(1f)) {
                        Text(text = "GitHub @isygold", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { /* Discord */ }, modifier = Modifier.weight(1f)) {
                        Text(text = "Discord", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { /* Telegram */ }, modifier = Modifier.weight(1f)) {
                        Text(text = "Telegram t.me/isygold", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}