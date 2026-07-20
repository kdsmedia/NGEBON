package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.utils.BackgroundMusicManager

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var showSplash by remember { mutableStateOf(true) }

        if (showSplash) {
          SplashScreen(
            onFinished = { showSplash = false },
            modifier = Modifier.fillMaxSize()
          )
        } else {
          MainScreen(modifier = Modifier.fillMaxSize())
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    BackgroundMusicManager.start(this)
  }

  override fun onStop() {
    super.onStop()
    BackgroundMusicManager.pause()
  }

  override fun onDestroy() {
    super.onDestroy()
    BackgroundMusicManager.release()
  }
}
