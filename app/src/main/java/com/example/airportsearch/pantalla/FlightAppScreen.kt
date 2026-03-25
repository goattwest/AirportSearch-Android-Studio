package com.example.airportsearch.pantalla

import com.example.airportsearch.data.Airport
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.airportsearch.viewmodel.FlightViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightAppScreen(viewModel: FlightViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedAirport by viewModel.selectedAirport.collectAsState()
    val destinations by viewModel.flightDestinations.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra de búsqueda bonita
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            placeholder = { Text("Busca un aeropuerto (Ej: MAD)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lógica de visualización
        if (searchQuery.isBlank() && selectedAirport == null) {
            // Mostrar Favoritos si no hay búsqueda
            Text("Tus Rutas Favoritas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(favorites) { fav ->
                    RouteCard(
                        departure = fav.departureCode,
                        destination = fav.destinationCode,
                        isFavorite = true,
                        onFavoriteClick = { viewModel.toggleFavorite(fav.departureCode, fav.destinationCode, true, fav) }
                    )
                }
            }
        } else if (selectedAirport == null) {
            // Mostrar sugerencias (Autocompletar)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchResults) { airport ->
                    AirportSuggestionItem(airport = airport) {
                        viewModel.onAirportSelected(airport)
                    }
                }
            }
        } else {
            // Mostrar Vuelos Disponibles desde el aeropuerto seleccionado
            Text("Vuelos desde ${selectedAirport!!.iataCode}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(destinations) { dest ->
                    val isFav = favorites.any { it.departureCode == selectedAirport!!.iataCode && it.destinationCode == dest.iataCode }
                    val favItem = favorites.find { it.departureCode == selectedAirport!!.iataCode && it.destinationCode == dest.iataCode }

                    RouteCard(
                        departure = selectedAirport!!.iataCode,
                        destination = dest.iataCode,
                        destinationName = dest.name,
                        isFavorite = isFav,
                        onFavoriteClick = { viewModel.toggleFavorite(selectedAirport!!.iataCode, dest.iataCode, isFav, favItem) }
                    )
                }
            }
        }
    }
}

@Composable
fun AirportSuggestionItem(airport: Airport, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = airport.iataCode, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = airport.name, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RouteCard(
    departure: String,
    destination: String,
    destinationName: String = "",
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "$departure  ➔  $destination", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (destinationName.isNotEmpty()) {
                    Text(text = destinationName, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Favorito",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}