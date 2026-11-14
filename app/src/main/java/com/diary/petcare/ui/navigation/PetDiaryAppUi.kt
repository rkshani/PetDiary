package com.diary.petcare.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

/** Root UI: side drawer, bottom bar for the top-level screens, and the nav host. */
@Composable
fun PetDiaryAppUi(
    startDestination: String = Routes.HOME,
    navController: NavHostController = rememberNavController(),
) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentDestination?.route }
    val onHome = currentDestination?.route == Routes.HOME
    // v2: AskNotificationPermissionOnce() (see app/src/v2/README.md)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    val navigateTop: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BackHandler(enabled = drawerState.isOpen) { closeDrawer() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Swipe to open only on the home screen, so it does not fight with tabs and pagers.
        gesturesEnabled = onHome || drawerState.isOpen,
        drawerContent = {
            AppDrawerContent(
                homeSelected = onHome,
                onHome = {
                    closeDrawer()
                    if (!onHome) navigateTop(Routes.HOME)
                },
                onClose = closeDrawer,
            )
        },
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        TopLevelDestination.entries.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTop(dest.route) },
                                icon = { Icon(if (selected) dest.selectedIcon else dest.icon, contentDescription = null) },
                                label = { Text(stringResource(dest.labelRes)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            PetDiaryNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding).consumeWindowInsets(padding),
                onOpenDrawer = { scope.launch { drawerState.open() } },
            )
        }
    }
}
