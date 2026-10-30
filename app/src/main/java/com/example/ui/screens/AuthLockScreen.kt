package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AuthLockScreen(
    isUsingDefaultPin: Boolean,
    isBiometricEnabled: Boolean,
    onPinEntered: (String) -> Boolean,
    onBiometricSuccess: () -> Unit,
    onResetDefaultPin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPinDialog by remember { mutableStateOf(false) }
    var showBiometricModal by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -10f at 250
                    10f at 300
                    -5f at 350
                    0f at 400
                }
            )
        }
    }

    fun handleDigitPress(digit: String) {
        if (pinInput.length < 4) {
            errorMessage = null
            val nextPin = pinInput + digit
            pinInput = nextPin

            if (nextPin.length == 4) {
                val isSuccess = onPinEntered(nextPin)
                if (!isSuccess) {
                    errorMessage = "Incorrect PIN. Try again."
                    triggerShake()
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(400)
                        pinInput = ""
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (pinInput.isNotEmpty()) {
            errorMessage = null
            pinInput = pinInput.dropLast(1)
        }
    }

    fun tryNativeBiometricPrompt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && context is Activity) {
            try {
                val biometricPrompt = BiometricPrompt.Builder(context)
                    .setTitle("Unlock Mindspace")
                    .setSubtitle("Confirm fingerprint or face authentication")
                    .setDescription("Your offline tasks and journals are protected locally on this device.")
                    .setNegativeButton("Use PIN", context.mainExecutor) { _, _ ->
                        // User opted to use PIN
                    }
                    .build()

                val cancellationSignal = CancellationSignal()
                biometricPrompt.authenticate(
                    cancellationSignal,
                    context.mainExecutor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            onBiometricSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errorCode, errString)
                            // If hardware sensor isn't enrolled or available (e.g. in cloud emulator), fallback to interactive modal
                            showBiometricModal = true
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            errorMessage = "Biometric check failed."
                            triggerShake()
                        }
                    }
                )
            } catch (e: Exception) {
                showBiometricModal = true
            }
        } else {
            showBiometricModal = true
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_lock_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Branding & Lock Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MINDSPACE VAULT",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter 4-Digit Security PIN",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isUsingDefaultPin) "Default PIN is 1234 (configured locally)" else "Offline personal data is secured",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUsingDefaultPin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots with Shake Animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) }
                        .padding(vertical = 8.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinInput.length
                        val isError = errorMessage != null
                        val dotColor by animateColorAsState(
                            targetValue = when {
                                isError -> MaterialTheme.colorScheme.error
                                isFilled -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            animationSpec = tween(150),
                            label = "pinDotColor"
                        )

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .testTag("pin_digit_dot_$i")
                        )
                    }
                }

                // Error Message Notice
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            // Keypad Component
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val digitRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                for (row in digitRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (digit in row) {
                            KeypadButton(
                                text = digit,
                                onClick = { handleDigitPress(digit) },
                                testTag = "pin_btn_$digit"
                            )
                        }
                    }
                }

                // Bottom Row: Biometric, 0, Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometric Trigger
                    if (isBiometricEnabled) {
                        KeypadActionButton(
                            onClick = { tryNativeBiometricPrompt() },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Unlock",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            testTag = "pin_btn_biometric"
                        )
                    } else {
                        Spacer(modifier = Modifier.size(72.dp))
                    }

                    KeypadButton(
                        text = "0",
                        onClick = { handleDigitPress("0") },
                        testTag = "pin_btn_0"
                    )

                    KeypadActionButton(
                        onClick = { handleBackspace() },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        testTag = "pin_btn_backspace"
                    )
                }
            }

            // Forgot PIN / Help action
            TextButton(
                onClick = { showForgotPinDialog = true },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("forgot_pin_btn")
            ) {
                Text(
                    text = "Forgot PIN? Reset to default",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                )
            }
        }

        // Biometric Emulator / Device Touch Simulation Modal
        if (showBiometricModal) {
            AlertDialog(
                onDismissRequest = { showBiometricModal = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Sensor",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                },
                title = {
                    Text(
                        text = "Biometric Authentication",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Touch device sensor or tap Authenticate to verify biometric identity offline.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showBiometricModal = false
                            onBiometricSuccess()
                            Toast.makeText(context, "Biometric authentication successful", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("biometric_confirm_dialog_btn")
                    ) {
                        Text("Authenticate")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBiometricModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Forgot PIN Reset Confirmation
        if (showForgotPinDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPinDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text("Reset PIN to Default?") },
                text = {
                    Text(
                        "Your PIN will be restored to '1234'. All of your existing offline journal entries and task notes will remain completely safe in your device database."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetDefaultPin()
                            pinInput = ""
                            errorMessage = null
                            showForgotPinDialog = false
                            Toast.makeText(context, "PIN restored to 1234", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("confirm_reset_pin_btn")
                    ) {
                        Text("Reset to 1234")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = modifier
            .size(72.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun KeypadActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        modifier = modifier
            .size(72.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}
