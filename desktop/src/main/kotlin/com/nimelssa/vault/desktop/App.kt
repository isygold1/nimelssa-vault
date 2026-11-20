package com.nimelssa.vault.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nimelssa.vault.desktop.auth.FirebaseAuthClient
import com.nimelssa.vault.desktop.data.MatricNo
import kotlinx.coroutines.launch

private enum class AuthMode { LOGIN, SIGNUP }

private val Bg = Color(0xFF0B1220)
private val CardBg = Color(0xFF1C273E)
private val CardBorder = Color(0xFF23314F)
private val Muted = Color(0xFF94A3B8)
private val Faint = Color(0xFF64748B)
private val Brand = Color(0xFF0C826B)
private val Levels = listOf("100L", "200L", "300L", "400L", "500L")

@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme(primary = Brand)) {
        var mode by remember { mutableStateOf(AuthMode.LOGIN) }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var matricNo by remember { mutableStateOf("") }
        var classLevel by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmVisible by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var notice by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        fun submit() {
            error = null
            notice = null

            if (email.isBlank() || password.isBlank()) {
                error = "Email and password are required."
                return
            }
            if (mode == AuthMode.SIGNUP) {
                if (name.isBlank()) {
                    error = "Full name is required."
                    return
                }
                if (!MatricNo.isValid(matricNo)) {
                    error = "Enter a valid matric number (e.g. MLS/22/1234)."
                    return
                }
                if (classLevel.isBlank()) {
                    error = "Select your class level."
                    return
                }
                if (password.length < 8) {
                    error = "Password must be at least 8 characters."
                    return
                }
                if (password != confirmPassword) {
                    error = "Passwords do not match."
                    return
                }
            }

            isLoading = true
            scope.launch {
                val result = if (mode == AuthMode.SIGNUP) {
                    FirebaseAuthClient.signUp(email.trim(), password)
                } else {
                    FirebaseAuthClient.signIn(email.trim(), password)
                }
                isLoading = false
                result
                    .onSuccess { user ->
                        notice = if (mode == AuthMode.SIGNUP) {
                            "Account created. Check your email to verify it."
                        } else {
                            "Signed in as ${user.email ?: email.trim()}."
                        }
                    }
                    .onFailure { error = it.message ?: "Authentication failed." }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().background(Bg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NIMELSSA Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text("Desktop", color = Muted)
                Spacer(Modifier.height(24.dp))

                Surface(
                    color = CardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            text = if (mode == AuthMode.LOGIN) "Sign in" else "Create account",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))

                        if (mode == AuthMode.SIGNUP) {
                            AuthField(name, { name = it }, "Full name")
                            Spacer(Modifier.height(12.dp))
                        }

                        AuthField(email, { email = it }, "Email")

                        if (mode == AuthMode.SIGNUP) {
                            Spacer(Modifier.height(12.dp))
                            AuthField(
                                value = matricNo,
                                onChange = { matricNo = it.uppercase() },
                                label = "Matric number (MLS/22/1234)"
                            )
                            Spacer(Modifier.height(12.dp))
                            LevelDropdown(classLevel) { classLevel = it }
                        }

                        Spacer(Modifier.height(12.dp))
                        PasswordField(
                            value = password,
                            onChange = { password = it },
                            label = "Password",
                            visible = passwordVisible,
                            onToggle = { passwordVisible = !passwordVisible }
                        )

                        if (mode == AuthMode.SIGNUP) {
                            Spacer(Modifier.height(12.dp))
                            PasswordField(
                                value = confirmPassword,
                                onChange = { confirmPassword = it },
                                label = "Confirm password",
                                visible = confirmVisible,
                                onToggle = { confirmVisible = !confirmVisible }
                            )
                        }

                        error?.let {
                            Spacer(Modifier.height(12.dp))
                            Text(it, color = Color(0xFFF87171), style = MaterialTheme.typography.bodySmall)
                        }
                        notice?.let {
                            Spacer(Modifier.height(12.dp))
                            Text(it, color = Color(0xFF34D399), style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = ::submit,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (mode == AuthMode.LOGIN) "Sign in" else "Create verified account",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                mode = if (mode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN
                                error = null
                                notice = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (mode == AuthMode.LOGIN) "Need an account? Sign up"
                                else "Already have an account? Sign in"
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Built by Agboola Israel Oluwagbogo, an MLS student at the University of Ilorin — not owned or endorsed by the University.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "GitHub @isygold · Discord isygold · t.me/isygold",
                    style = MaterialTheme.typography.labelSmall,
                    color = Faint,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AuthField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password"
                )
            }
        },
        colors = fieldColors()
    )
}

@Composable
private fun LevelDropdown(value: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value.ifBlank { "Class level" },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text("▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Levels.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level) },
                    onClick = {
                        onSelect(level)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White,
    focusedBorderColor = Brand,
    unfocusedBorderColor = CardBorder,
    focusedLabelColor = Muted,
    unfocusedLabelColor = Muted
)
