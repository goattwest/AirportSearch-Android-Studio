package com.example.airportsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.airportsearch.data.FlightDatabase
import com.example.airportsearch.data.UserPreferencesRepository
import com.example.airportsearch.pantalla.FlightAppScreen
import com.example.airportsearch.viewmodel.FlightViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instanciar BD y DataStore
        val database = FlightDatabase.getDatabase(this)
        val preferencesRepo = UserPreferencesRepository(this)

        // Factory para el ViewModel
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FlightViewModel(database.flightDao(), preferencesRepo) as T
            }
        }
        val viewModel = ViewModelProvider(this, factory)[FlightViewModel::class.java]

        setContent {
            MaterialTheme { // Usa el tema de Material 3 por defecto
                Surface(color = MaterialTheme.colorScheme.background) {
                    FlightAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}