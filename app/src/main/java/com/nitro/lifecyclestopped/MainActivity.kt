package com.nitro.lifecyclestopped

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nitro.lifecyclestopped.ui.theme.LifecycleStoppedTheme
import logcat.logcat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            LifecycleStoppedTheme {
                Scaffold(bottomBar = {
                    Bottom(currentRoute = currentRoute) {
                        navController.navigate(it) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }) {
                    Graph(navController)
                }

            }
        }
    }
}

@Composable
fun Bottom(currentRoute: String?, onNavigateRoute: (String) -> Unit) {
    BottomAppBar {
        BottomNavigationItem(selected = currentRoute == "home", onClick = {
            onNavigateRoute("home")
        }, icon = {
            Icon(imageVector = Icons.Default.Home, contentDescription = null)
        })
        BottomNavigationItem(selected = currentRoute == "map", onClick = {
            onNavigateRoute("map")
        }, icon = {
            Icon(imageVector = Icons.Default.Place, contentDescription = null)
        })
    }
}

@Composable
fun Graph(navController: NavHostController) {
    NavHost(navController = navController, "home") {
        composable("home") {
            Text("home")
        }
        composable("map") {
            LogLifecycle()
            Text("map")
        }
    }
}

@Composable
fun LogLifecycle() {
    val owner = LocalLifecycleOwner.current
    val lifecycle = owner.lifecycle
    logcat("MainActivity") { "owner $owner $lifecycle ${lifecycle.currentState}" }

    DisposableEffect(lifecycle) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            logcat("MainActivity") { "event $event" }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}