package com.example.subpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TwoScreenApp()
        }
    }
}

@Composable
fun TwoScreenApp() {
    var isFirstScreen by remember { mutableStateOf(true) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFirstScreen) {
                Text("Первый экран", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { isFirstScreen = false }) {
                    Text("Перейти на второй экран")
                }
            } else {
                Text("Второй экран", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { isFirstScreen = true }) {
                    Text("Назад")
                }
            }
        }
    }
}
