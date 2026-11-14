package com.diary.petcare.ui.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diary.petcare.R
import com.diary.petcare.core.Constants

/** Side drawer opened from the menu icon on the home screen. */
@Composable
fun AppDrawerContent(
    homeSelected: Boolean,
    onHome: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    ModalDrawerSheet {
        Column(Modifier.padding(horizontal = 28.dp, vertical = 24.dp)) {
            Text("🐾", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.drawer_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(Modifier.padding(bottom = 8.dp))
        DrawerItem(Icons.Outlined.Home, stringResource(R.string.nav_home), selected = homeSelected, onClick = onHome)
        DrawerItem(Icons.Outlined.StarRate, stringResource(R.string.drawer_rate_us)) {
            onClose()
            openRateUs(context)
        }
        DrawerItem(Icons.Outlined.PrivacyTip, stringResource(R.string.drawer_privacy_policy)) {
            onClose()
            openUrl(context, Constants.PRIVACY_POLICY_URL)
        }
        DrawerItem(Icons.AutoMirrored.Outlined.ExitToApp, stringResource(R.string.drawer_exit)) {
            (context as? Activity)?.finishAffinity()
        }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, selected: Boolean = false, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
}

/** Opens the Play Store page for this app; falls back to the web page if the store app is missing. */
private fun openRateUs(context: Context) {
    val id = context.packageName
    val market = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PLAY_STORE_APP_URI + id))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(market)
    } catch (_: ActivityNotFoundException) {
        openUrl(context, Constants.PLAY_STORE_WEB_URL + id)
    }
}

/** Hands the link to the phone's browser. The app needs no INTERNET permission for this. */
private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.drawer_no_browser, Toast.LENGTH_SHORT).show()
    }
}
