package com.svoysport.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.svoysport.tv.navigation.AppNavGraph
import com.svoysport.tv.navigation.Screen
import com.svoysport.tv.ui.components.GlobalReminderBanner
import com.svoysport.tv.ui.theme.SvoySportTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SvoySportTheme {
                val navController = rememberNavController()
                Box(Modifier.fillMaxSize()) {
                    AppNavGraph(navController = navController)
                    GlobalReminderBanner(
                        onWatch = { matchId ->
                            navController.navigate(Screen.Player.createRoute(matchId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
