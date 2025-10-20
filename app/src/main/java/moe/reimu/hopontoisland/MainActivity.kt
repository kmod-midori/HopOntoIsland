@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package moe.reimu.hopontoisland

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.reimu.hopontoisland.ui.theme.HopOntoIslandTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val shortcut = ShortcutInfoCompat.Builder(this, "capture")
            .setShortLabel(getString(R.string.capture_shortcut_label))
            .setLongLabel(getString(R.string.capture_shortcut_label))
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_notifications))
            .setIntent(Intent(this, CaptureActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut.build())

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            HopOntoIslandTheme {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
                    }
                ) { innerPadding ->
                    MainView(
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun MainView(snackbarHostState: SnackbarHostState, modifier: Modifier = Modifier) {
    val notificationPermissionState =
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val context = LocalContext.current

    var canPostPromotedNotifications by remember { mutableStateOf(false) }
    LifecycleResumeEffect(context) {
        val notifManager = NotificationManagerCompat.from(context)
        canPostPromotedNotifications = notifManager.canPostPromotedNotifications()
        onPauseOrDispose { }
    }

    var serviceEnabled by remember { mutableStateOf(false) }
    LifecycleResumeEffect(context) {
        serviceEnabled = TextAccessibilityService.getInstance() != null
        onPauseOrDispose { }
    }

    MainList(modifier = modifier) {
        if (!canPostPromotedNotifications) {
            item {
                DefaultCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    MyIcon(ImageVector.vectorResource(R.drawable.ic_warning))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.cannot_post_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(stringResource(R.string.cannot_post_desc))
                    }
                }
            }
        }
        if (!serviceEnabled) {
            item {
                DefaultCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    onClick = {
                        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    MyIcon(ImageVector.vectorResource(R.drawable.ic_warning))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.a11y_disabled_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(stringResource(R.string.a11y_disabled_desc))
                    }
                }
            }
        }
        item {
            if (notificationPermissionState.status.isGranted) {
                DefaultCard(onClick = {
                    postLiveUpdate(
                        context, "Test", "Test", "Test",
                        R.drawable.ic_notifications, "#3DDC84".toColorInt()
                    )
                }) {
                    MyIcon(ImageVector.vectorResource(R.drawable.ic_notifications))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.test_notification_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(stringResource(R.string.test_notification_desc))
                    }
                }
            } else {
                DefaultCard(
                    onClick = { notificationPermissionState.launchPermissionRequest() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    MyIcon(ImageVector.vectorResource(R.drawable.ic_warning))
                    Text(
                        stringResource(R.string.grant_noti_perm_label),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        item {
            DefaultCard {
                ModelSettings(snacbkarHostState = snackbarHostState)
            }
        }
    }
}

@Composable
fun MainList(modifier: Modifier = Modifier, content: LazyListScope.() -> Unit) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        content = content
    )
}

@Composable
fun DefaultCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(), colors = colors
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun DefaultCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable RowScope.() -> Unit
) {
    Card(
        onClick = onClick, modifier = modifier.fillMaxWidth(), colors = colors
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun MyIcon(imageVector: ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .padding(end = 16.dp),
    )
}

@Composable
fun ModelSettings(snacbkarHostState: SnackbarHostState) {
    val app = MyApplication.getInstance()
    val modelUrlState =
        rememberTextFieldState(initialText = remember { app.getSettings().modelUrl.orEmpty() })
    val modelKeyState =
        rememberTextFieldState(initialText = remember { app.getSettings().modelKey.orEmpty() })
    val modelNameState =
        rememberTextFieldState(initialText = remember { app.getSettings().modelName.orEmpty() })
    val settingsValid = modelUrlState.text.startsWith("http") &&
            modelKeyState.text.isNotBlank() &&
            modelNameState.text.isNotBlank()
    val scope = rememberCoroutineScope()

    Column {
        Text(
            stringResource(R.string.model_label),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            state = modelUrlState,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(stringResource(R.string.model_url_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            state = modelKeyState,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(stringResource(R.string.model_key_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            state = modelNameState,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(stringResource(R.string.model_name_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val settings = app.getSettings()
                    settings.modelUrl = modelUrlState.text.toString()
                    settings.modelKey = modelKeyState.text.toString()
                    settings.modelName = modelNameState.text.toString()
                }
                snacbkarHostState.showSnackbar(
                    MyApplication.getInstance().getString(R.string.saved_msg)
                )
            }
        }, enabled = settingsValid) {
            Text(stringResource(R.string.save_label))
        }
    }
}