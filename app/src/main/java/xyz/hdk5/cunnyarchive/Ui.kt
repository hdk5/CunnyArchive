package xyz.hdk5.cunnyarchive

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Article
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Actions : BottomNavigationItem("actions", Icons.Filled.Home, "Actions")
    object Log : BottomNavigationItem("log", Icons.Outlined.Article, "Log")
}

@Composable
fun MainComposable() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        Navigation(navController)
    }
}

@Composable
fun TopBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) }
    )
}

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomNavigationItem.Actions.route,
    ) {
        composable(BottomNavigationItem.Actions.route) { ActionsScreen() }
        composable(BottomNavigationItem.Log.route) { LogScreen() }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavigationItem.Actions,
        BottomNavigationItem.Log,
    )
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, item.title) },
                label = { Text(item.title) },
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }

                }
            )
        }
    }
}

@Composable
fun ActionsScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.open_directory_note))
        Spacer(modifier = Modifier.height(8.dp))
        OpenDirectoryButton()

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.uncensor_note))
        Spacer(modifier = Modifier.height(8.dp))
        UncensorButton()

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.recensor_note))
        Spacer(modifier = Modifier.height(8.dp))
        RecensorButton()
    }
}

@Composable
fun OpenDirectoryButton() {
    val context = LocalContext.current
    val doneToast = Toast.makeText(context, "Permission acquired", Toast.LENGTH_LONG)

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = makeLaunchFunction {
            doneToast.show()
        },
    ) {
        Text("Request Storage Access".uppercase())
    }
}

@Composable
fun UncensorButton() {
    val context = LocalContext.current
    val doneToast = Toast.makeText(context, "Uncensoring completed", Toast.LENGTH_LONG)

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            // TODO: Do this asynchronously
            ALL_CENSORSHIP.fix(context)
            doneToast.show()
        },
    ) {
        Text("Uncensor".uppercase())
    }
}

@Composable
fun RecensorButton() {
    val context = LocalContext.current
    val doneToast = Toast.makeText(context, "Recensoring completed", Toast.LENGTH_LONG)

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            // TODO: Do this asynchronously
            ALL_CENSORSHIP.revert(context)
            doneToast.show()
        }
    ) {
        Text("Recensor".uppercase())
    }
}

@Composable
fun LogScreen() {
    Text("Nothing here yet")
}
