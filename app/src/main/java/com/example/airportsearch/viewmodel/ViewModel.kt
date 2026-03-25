package com.example.airportsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airportsearch.data.Airport
import com.example.airportsearch.data.Favorite
import com.example.airportsearch.data.FlightDao
import com.example.airportsearch.data.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlightViewModel(
    private val flightDao: FlightDao,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedAirport = MutableStateFlow<Airport?>(null)
    val selectedAirport = _selectedAirport.asStateFlow()

    val searchResults: StateFlow<List<Airport>> = _searchQuery
        .debounce(300) // Espera a que el usuario termine de escribir
        .flatMapLatest { query ->
            if (query.isNotBlank()) flightDao.searchAirports(query)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flightDestinations: StateFlow<List<Airport>> = _selectedAirport
        .flatMapLatest { airport ->
            if (airport != null) flightDao.getAllDestinations(airport.iataCode)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Favorite>> = flightDao.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Cargar búsqueda guardada al iniciar
        viewModelScope.launch {
            preferencesRepository.searchQuery.collect { savedQuery ->
                _searchQuery.value = savedQuery
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _selectedAirport.value = null // Resetea la selección si el usuario vuelve a escribir
        viewModelScope.launch {
            preferencesRepository.saveSearchQuery(query)
        }
    }

    fun onAirportSelected(airport: Airport) {
        _selectedAirport.value = airport
        // Opcional: limpiar la barra de búsqueda o poner el código IATA
        _searchQuery.value = airport.iataCode
    }

    fun toggleFavorite(departure: String, destination: String, isFavorite: Boolean, favoriteItem: Favorite?) {
        viewModelScope.launch {
            if (isFavorite && favoriteItem != null) {
                flightDao.deleteFavorite(favoriteItem)
            } else {
                flightDao.insertFavorite(Favorite(departureCode = departure, destinationCode = destination))
            }
        }
    }
}