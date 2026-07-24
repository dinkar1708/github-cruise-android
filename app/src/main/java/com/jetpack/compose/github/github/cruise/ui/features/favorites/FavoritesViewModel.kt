package com.jetpack.compose.github.github.cruise.ui.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.domain.repository.FavoritesRepository
import com.jetpack.compose.github.github.cruise.di.DefaultDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Favorites screen
 *
 * Threading:
 * - viewModelScope runs on Dispatchers.Main by default (UI thread)
 * - Repository layer handles threading for SharedPreferences (uses IoDispatcher)
 * - Flow collection happens on Main thread (safe for UI updates)
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesState())
    val uiState: StateFlow<FavoritesState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    /**
     * Observe favorites from repository
     * Collects on Main thread (viewModelScope)
     */
    private fun observeFavorites() {
        favoritesRepository.favorites
            .onEach { favorites ->
                _uiState.update { it.copy(favorites = favorites, isLoading = false) }
            }
            .catch { e ->
                Timber.e(e, "Error observing favorites")
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope) // Collects on Main dispatcher
    }

    /**
     * Add item to favorites
     * Repository handles IO thread switching
     */
    fun addFavorite(item: FavoriteItem) {
        viewModelScope.launch {
            try {
                favoritesRepository.addFavorite(item)
            } catch (e: Exception) {
                Timber.e(e, "Error adding favorite")
                _uiState.update { it.copy(error = "Failed to add favorite") }
            }
        }
    }

    /**
     * Remove item from favorites
     * Repository handles IO thread switching
     */
    fun removeFavorite(itemId: String, type: FavoriteType) {
        viewModelScope.launch {
            try {
                favoritesRepository.removeFavorite(itemId, type)
            } catch (e: Exception) {
                Timber.e(e, "Error removing favorite")
                _uiState.update { it.copy(error = "Failed to remove favorite") }
            }
        }
    }

    /**
     * Clear all favorites
     * Repository handles IO thread switching
     */
    fun clearAllFavorites() {
        viewModelScope.launch {
            try {
                favoritesRepository.clearFavorites()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing favorites")
                _uiState.update { it.copy(error = "Failed to clear favorites") }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
