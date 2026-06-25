package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.db.AppDatabase
import com.example.db.ScoreRepository
import com.example.ui.FretboardTrainerApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FretboardViewModel
import com.example.viewmodel.FretboardViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Room Database & Score Repository
    val database = AppDatabase.getDatabase(this)
    val repository = ScoreRepository(database.scoreDao())
    
    // Instantiate FretboardViewModel with the Factory
    val viewModelFactory = FretboardViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, viewModelFactory)[FretboardViewModel::class.java]

    setContent {
      MyApplicationTheme {
        FretboardTrainerApp(viewModel)
      }
    }
  }
}
