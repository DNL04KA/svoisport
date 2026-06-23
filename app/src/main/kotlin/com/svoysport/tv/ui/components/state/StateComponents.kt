package com.svoysport.tv.ui.components.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

@Composable
fun HomeLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, start = 80.dp)
    ) {
        // Skeleton for Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 80.dp)
                .height(400.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Skeleton for Row
        repeat(2) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(30.dp)
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .height(158.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.2f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HomeErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Ошибка загрузки", style = MaterialTheme.typography.headlineLarge)
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Попробовать снова")
            }
        }
    }
}
