package com.nx10.androidTest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nx10.androidTest.BuildConfig
import com.nx10.asparagus.AsparagusSdk
import com.nx10.asparagus.SdkTelemetrySnapshot
import com.nx10.asparagus.contract.Value
import com.nx10.asparagus.events.Outcome
import com.nx10.asparagus.events.StandardStateKeys
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TestHarnessScreen(sdk: AsparagusSdk) {
    val snapshot by sdk.statusFlow.collectAsState()
    val uiScope = rememberCoroutineScope()

    var apiKey by rememberSaveable { mutableStateOf(BuildConfig.DEFAULT_API_KEY) }
    var demoSession by rememberSaveable { mutableStateOf(false) }

    var sensitivePaused by rememberSaveable { mutableStateOf(false) }
    var keyboardVisible by rememberSaveable { mutableStateOf(false) }
    var levelCounter by rememberSaveable { mutableIntStateOf(1) }
    var scoreCounter by rememberSaveable { mutableIntStateOf(100) }
    var feedbackBanner by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NX10 Asparagus SDK Harness", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "State: ${snapshot.sessionState.name}  •  Schema v${snapshot.schemaVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (feedbackBanner.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Text(
                        text = feedbackBanner,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            // 1. Connection & Session Card
            SectionCard(title = "1. Connection & Session") {
                Text(
                    text = "Endpoint: ${snapshot.sessionStartEndpoint}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Deployment API Key") },
                    placeholder = { Text("Paste runtime API key") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                ToggleRow(
                    label = "Use Production Endpoint (default: Stage)",
                    checked = snapshot.useProduction,
                    onCheckedChange = { sdk.useProduction = it },
                )
                ToggleRow(
                    label = "Demo Session (collects nothing)",
                    checked = demoSession,
                    onCheckedChange = { demoSession = it },
                )

                Spacer(Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            if (apiKey.isBlank()) {
                                feedbackBanner = "Enter a Deployment API Key first"
                            } else {
                                val envName = if (snapshot.useProduction) "Production" else "Stage"
                                feedbackBanner = "Starting $envName session..."
                                sdk.startSession(
                                    apiKey = apiKey.trim(),
                                    demo = demoSession,
                                )
                            }
                        },
                    ) { Text("Start Session") }

                    OutlinedButton(
                        onClick = {
                            sdk.stopSession()
                            feedbackBanner = "Stopping session & flushing final batch"
                        },
                    ) { Text("Stop Session") }

                    FilledTonalButton(
                        onClick = {
                            sdk.flushWindowNow()
                            feedbackBanner = "Requested window flush"
                        },
                    ) { Text("Flush Window Now") }

                    FilledTonalButton(
                        onClick = {
                            sdk.drainQueue()
                            feedbackBanner = "Triggered disk queue drain"
                        },
                    ) { Text("Drain Queue") }
                }
            }

            // 2. Live Telemetry & Queue Status Card
            LiveStatusCard(
                snapshot = snapshot,
                sensitivePaused = sensitivePaused,
                keyboardVisible = keyboardVisible,
                onToggleTelemetry = { enabled ->
                    if (enabled) sdk.startTelemetry() else sdk.stopTelemetry()
                },
                onToggleSensitive = { paused ->
                    sensitivePaused = paused
                    sdk.setSensitiveTouchPaused(paused)
                },
                onToggleKeyboard = { visible ->
                    keyboardVisible = visible
                    sdk.setKeyboardVisible(visible)
                },
            )

            // 3. Multi-Touch Sandbox Pad Card
            MultiTouchSandboxCard(touchPaused = snapshot.touchPaused)

            // 4. Events, Spans & Game State Playground Card
            SectionCard(title = "4. Events, Spans & Game State Playground") {
                Text(
                    "Core Events & Spans",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(onClick = {
                        sdk.coreEvents.levelStarted(
                            level = "level_$levelCounter",
                            attemptNumber = 1.0,
                            difficulty = 2.0,
                        )
                    }) { Text("Start Level") }

                    FilledTonalButton(onClick = {
                        sdk.coreEvents.levelSucceeded(
                            level = "level_$levelCounter",
                            score = scoreCounter.toDouble(),
                            duration = 12.5,
                        )
                        levelCounter++
                    }) { Text("Succeed Level") }

                    OutlinedButton(onClick = {
                        sdk.coreEvents.levelFailed(
                            level = "level_$levelCounter",
                            score = (scoreCounter / 2).toDouble(),
                            duration = 8.0,
                        )
                    }) { Text("Fail Level") }

                    FilledTonalButton(onClick = {
                        sdk.coreEvents.adStarted(type = "rewarded", placement = "harness_shop")
                    }) { Text("Start Rewarded Ad") }

                    OutlinedButton(onClick = {
                        sdk.coreEvents.adFinished(type = "rewarded", placement = "harness_shop", completed = true)
                    }) { Text("Finish Rewarded Ad") }

                    FilledTonalButton(onClick = {
                        sdk.coreEvents.adStarted(type = "banner", placement = "harness_bottom")
                    }) { Text("Start Banner Ad") }

                    OutlinedButton(onClick = {
                        sdk.coreEvents.adFinished(type = "banner", placement = "harness_bottom", completed = true)
                    }) { Text("Finish Banner Ad") }

                    FilledTonalButton(onClick = {
                        sdk.coreEvents.adRevenue(
                            type = "rewarded",
                            amount = 0.02,
                            currency = "USD",
                            outcome = Outcome.CONVERTED,
                            network = "test_network",
                            reason = "ad_displayed",
                        )
                    }) { Text("Ad Revenue (Converted)") }

                    FilledTonalButton(onClick = {
                        sdk.coreEvents.inAppPurchase(
                            product = "starter_pack",
                            price = 1.99,
                            currency = "USD",
                            outcome = Outcome.CONVERTED,
                            reason = "purchase_completed",
                            quantity = 1,
                        )
                    }) { Text("IAP Purchase (Converted)") }

                    OutlinedButton(onClick = {
                        sdk.sendEvent(
                            name = "harness_custom_event",
                            fields = mapOf(
                                "label" to Value.Text("Café 小店 🛒"),
                                "score" to Value.Number(scoreCounter.toDouble()),
                                "active" to Value.Flag(true),
                            ),
                            span = "custom_span",
                        )
                    }) { Text("Custom Event") }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))

                Text(
                    "Game State (PUT / PATCH)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(onClick = {
                        sdk.setAttributes(
                            mapOf(
                                StandardStateKeys.LEVEL to Value.Text("level_$levelCounter"),
                                StandardStateKeys.SCREEN to Value.Text("harness"),
                                StandardStateKeys.MODE to Value.Text("sandbox"),
                                "score" to Value.Number(scoreCounter.toDouble()),
                            ),
                        )
                    }) { Text("Put State") }

                    FilledTonalButton(onClick = {
                        scoreCounter += 50
                        sdk.setAttribute("score", Value.Number(scoreCounter.toDouble()))
                    }) { Text("Patch Score") }

                    OutlinedButton(onClick = {
                        sdk.removeAttribute("score")
                    }) { Text("Remove Key") }

                    OutlinedButton(onClick = {
                        sdk.clearAttributes()
                    }) { Text("Clear State") }

                    OutlinedButton(onClick = {
                        sdk.setAttribute("nx10_reserved_test", Value.Flag(true))
                    }) { Text("Try Reserved nx10_ Key") }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))

                Text(
                    "Player Compliance Endpoints",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        uiScope.launch {
                            val res = sdk.personalDataRequested()
                            feedbackBanner = "Data Access: $res"
                        }
                    }) { Text("Request Data Access") }

                    OutlinedButton(onClick = {
                        uiScope.launch {
                            val res = sdk.personalDataDeletionRequested()
                            feedbackBanner = "Data Deletion: $res"
                        }
                    }) { Text("Request Data Deletion") }
                }
            }

            // 5. Activity & Upload Log Card
            SectionCard(title = "5. Activity & Upload Log (${snapshot.recentLogs.size})") {
                if (snapshot.recentLogs.isEmpty()) {
                    Text(
                        text = "No activity yet. Start a session to capture telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        snapshot.recentLogs.asReversed().forEach { entry ->
                            Text(
                                text = entry,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LiveStatusCard(
    snapshot: SdkTelemetrySnapshot,
    sensitivePaused: Boolean,
    keyboardVisible: Boolean,
    onToggleTelemetry: (Boolean) -> Unit,
    onToggleSensitive: (Boolean) -> Unit,
    onToggleKeyboard: (Boolean) -> Unit,
) {
    SectionCard(title = "2. Live Telemetry & Queue Status") {
        StatusRow("Environment", if (snapshot.useProduction) "PRODUCTION" else "STAGE")
        StatusRow("Session State", snapshot.sessionState.name)
        StatusRow("Session ID", snapshot.idSession.ifEmpty { "—" })
        StatusRow("Source (Device) ID", snapshot.idSource.ifEmpty { "—" })
        StatusRow(
            "Settings / Window",
            "${snapshot.settingsVersion.ifEmpty { "built-in" }} • ${snapshot.batchLengthS}s • gzip=${snapshot.compress}",
        )
        StatusRow("Device Share", snapshot.settingsLabel.ifEmpty { "—" })
        StatusRow(
            "Install History",
            "sessions=${snapshot.historySessionsBefore} • collected=${snapshot.historyCollectedSecondsBefore}s • max=${snapshot.historyLongestSessionSecondsBefore}s",
        )
        StatusRow("Collection Enabled", "${snapshot.collectionEnabled} • nextSeq=${snapshot.nextSequence}")
        StatusRow("Hardware Sensors", "gyro=${snapshot.hasGyroscope} • mag=${snapshot.hasMagnetometer}")
        StatusRow(
            "SDK System State",
            "brightness=${snapshot.screenBrightness?.let { "%.2f".format(Locale.US, it) } ?: "—"} • locked=${snapshot.screenLocked ?: "—"}",
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        StatusRow("Motion Samples (window)", "${snapshot.motionSamplesInWindow}")
        StatusRow(
            "Touch Contacts / Samples",
            "${snapshot.touchContactsInWindow} / ${snapshot.touchSamplesInWindow} (paused=${snapshot.touchPaused})",
        )
        StatusRow("Events / State Records", "${snapshot.eventsInWindow} / ${snapshot.stateRecordsInWindow} (v${snapshot.stateVersion})")
        StatusRow("Open Spans", if (snapshot.openSpans.isEmpty()) "None" else snapshot.openSpans.joinToString(", "))
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        StatusRow("Disk Queue", "${snapshot.queuedBatchesOnDisk} batches (${snapshot.queuedBytesOnDisk} B)")
        StatusRow(
            "Uploaded / Refused / Dropped",
            "${snapshot.uploadedBatchesTotal} / ${snapshot.refusedBatchesTotal} / ${snapshot.pendingDroppedBatches}",
        )

        Spacer(Modifier.height(8.dp))
        ToggleRow(
            label = "Telemetry Capture (start/stopTelemetry)",
            checked = snapshot.telemetryEnabled,
            onCheckedChange = onToggleTelemetry,
        )
        ToggleRow(
            label = "Sensitive Screen Touch Pause",
            checked = sensitivePaused,
            onCheckedChange = onToggleSensitive,
        )
        ToggleRow(
            label = "Keyboard Visible",
            checked = keyboardVisible,
            onCheckedChange = onToggleKeyboard,
        )
    }
}

@Composable
private fun MultiTouchSandboxCard(touchPaused: Boolean) {
    val activePointers = remember { mutableStateMapOf<PointerId, Offset>() }
    val pointerPalette = listOf(
        Color(0xFF1E88E5),
        Color(0xFFE53935),
        Color(0xFF43A047),
        Color(0xFFFB8C00),
        Color(0xFF8E24AA),
    )

    SectionCard(title = "3. Multi-Touch Sandbox Pad") {
        Text(
            text = if (touchPaused) {
                "Touch capture is PAUSED (sensitive/keyboard). Active fingers: ${activePointers.size}"
            } else {
                "Touch or drag 1–5 fingers inside the pad (captures raw contacts without scrolling). Active: ${activePointers.size}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                )
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp),
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            for (change in event.changes) {
                                if (change.pressed) {
                                    activePointers[change.id] = change.position
                                } else {
                                    activePointers.remove(change.id)
                                }
                                change.consume()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var idx = 0
                for ((_, pos) in activePointers) {
                    val color = pointerPalette[idx % pointerPalette.size]
                    drawCircle(color = color.copy(alpha = 0.25f), radius = 64f, center = pos)
                    drawCircle(color = color, radius = 26f, center = pos)
                    idx++
                }
            }
            if (activePointers.isEmpty()) {
                Text(
                    text = "MULTI-TOUCH PAD",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
