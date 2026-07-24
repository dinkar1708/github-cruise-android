package com.jetpack.compose.github.github.cruise.ui.features.repositorysearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.data.network.model.ApiError
import com.jetpack.compose.github.github.cruise.di.DefaultDispatcher
import com.jetpack.compose.github.github.cruise.domain.pagination.PaginationManager
import com.jetpack.compose.github.github.cruise.domain.usecase.RepositorySearchUseCase
import com.jetpack.compose.github.github.cruise.ui.features.repositorysearch.state.RepositorySearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Repository Search Screen
 * Feature 2.1: Repository Search Screen
 *
 * Threading:
 * - viewModelScope runs on Dispatchers.Main by default (UI thread)
 * - Repository layer handles switching to IO thread via flowOn()
 * - Flow collection happens on Main thread (safe for UI updates)
 */
@HiltViewModel
class RepositorySearchViewModel @Inject constructor(
    private val repositorySearchUseCase: RepositorySearchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepositorySearchState())
    val uiState: StateFlow<RepositorySearchState> = _uiState.asStateFlow()

    private var searchQuery: String = ""
    private val paginationManager = PaginationManager(initialPageSize = 40)

    init {
        _uiState.update {
            RepositorySearchState(
                errorMessage = "Enter repository name to search",
                isLoading = false
            )
        }
    }

    fun searchRepositories(query: String) {
        Timber.d("searchRepositories() called with query: $query")
        searchQuery = query
        paginationManager.reset()
        paginationManager.startLoading()
        _uiState.update { RepositorySearchState(searchQuery = query) }
        loadRepositories()
    }

    fun loadNextPage() {
        val currentItemCount = _uiState.value.repositories.size
        if (paginationManager.shouldLoadNextPage(currentItemCount)) {
            val nextPage = paginationManager.loadNextPage()
            Timber.d("loadNextPage() loading page $nextPage")
            loadRepositories()
        }
    }

    fun updateLastVisibleIndex(index: Int) {
        _uiState.update { it.copy(lastVisibleItemIndex = index) }
    }

    private fun loadRepositories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            repositorySearchUseCase.searchRepositories(searchQuery, paginationManager.page, paginationManager.pageSize)
                .catch { e ->
                    Timber.e(e, "Error searching repositories")
                    paginationManager.finishLoading()
                    val errorMsg = when (e) {
                        is ApiError -> e.message
                        else -> e.message ?: "Unknown error occurred"
                    }
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = errorMsg)
                    }
                }
                .collect { searchResult ->
                    paginationManager.updateTotalResults(searchResult.totalCount)

                    val currentList = _uiState.value.repositories
                    val newList = if (paginationManager.page == 1) {
                        searchResult.repositories
                    } else {
                        currentList + searchResult.repositories
                    }

                    val hasMore = paginationManager.hasMorePages(newList.size)
                    _uiState.update {
                        it.copy(
                            repositories = newList,
                            totalCount = searchResult.totalCount,
                            isLoading = false,
                            hasMorePages = hasMore
                        )
                    }
                    Timber.d("Loaded repositories: ${paginationManager.getInfo()}")
                }
        }
    }

    companion object {
        private const val TAG = "RepositorySearchVM"
    }
}
