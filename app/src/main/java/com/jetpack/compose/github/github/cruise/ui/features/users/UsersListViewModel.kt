package com.jetpack.compose.github.github.cruise.ui.features.users

import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.di.DefaultDispatcher
import com.jetpack.compose.github.github.cruise.domain.pagination.PaginationManager
import com.jetpack.compose.github.github.cruise.domain.usecase.SearchRepositoryUseCase
import com.jetpack.compose.github.github.cruise.ui.base.BaseViewModel
import com.jetpack.compose.github.github.cruise.ui.features.users.state.UsersListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/12.
 *
 * Threading:
 * - viewModelScope runs on Dispatchers.Main by default (UI thread)
 * - Repository layer handles switching to IO thread via flowOn()
 * - Flow collection happens on Main thread (safe for UI updates)
 */
@HiltViewModel
class UsersListViewModel @Inject constructor(
    private val searchRepositoryUseCase: SearchRepositoryUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(UsersListState())
    val uiState: StateFlow<UsersListState> = _uiState.asStateFlow()

    // input search text
    private var userName: String = ""

    // pagination manager
    private val paginationManager = PaginationManager(initialPageSize = 30)

    // Job for tracking ongoing API call (for cancellation)
    private var searchJob: Job? = null

    init {
        _uiState.update {
            // update UI
            UsersListState(
                errorMessage = "Input user name to search",
                isLoading = false
            )
        }
    }

    fun updateLastVisibleIndex(index: Int) {
        _uiState.update { _uiState.value.copy(lastVisibleItemIndex = index) }
    }

    override val TAG = "UsersListViewModel"

    fun searchUsers(inputString: String) {
        logDebug("searchUsers() called...")
        userName = inputString

        // Cancel previous search job when search query changes
        // This prevents stale results from previous searches
        searchJob?.cancel()
        logDebug("searchUsers() - Cancelled previous search (new search query)")

        // Reset pagination when input string changes
        paginationManager.reset()
        paginationManager.startLoading()
        _uiState.update {
            // reset all things
            UsersListState()
        }
        loadUsers()
    }

    /**
     * public function to trigger next page data
     */
    fun loadNextPage() {
        val currentItemCount = _uiState.value.userList.size
        // Check if should load next page
        if (paginationManager.shouldLoadNextPage(currentItemCount)) {
            val nextPage = paginationManager.loadNextPage()
            logDebug("loadNextPage() called... loading data for page $nextPage now")
            loadUsers()
        } else {
            logDebug("loadNextPage() No more data to load or already loading")
        }
    }

    private fun loadUsers() {
        // Launch new search job
        // Note: Cancellation is handled in searchUsers() for new queries
        // For pagination (loadNextPage), we don't cancel - we want to load all pages
        searchJob = viewModelScope.launch {
            // keep original list
            _uiState.update {
                _uiState.value.copy(
                    isLoading = true,
                    userList = _uiState.value.userList
                )
            }
            try {
            if (userName.isEmpty()) {
                _uiState.update {
                    // update UI
                    UsersListState(
                        errorMessage = "Input user name to search",
                        isLoading = false
                    )
                }
                return@launch
            }

            val userList = searchRepositoryUseCase.searchUsers(userName, paginationManager.page, paginationManager.pageSize)
                .catch { exception ->
                    val errorMessage = handleError(exception, "searchUsers")
                    _uiState.update {
                        // update UI
                        UsersListState(
                            errorMessage = errorMessage,
                            isLoading = false
                        )
                    }
                    paginationManager.finishLoading()
                }
            userList.collect { searchUser ->
                if (searchUser.totalCount == 0) {
                    _uiState.update {
                        UsersListState(
                            errorMessage = "Your search did not match any user!",
                            isLoading = false
                        )
                    }
                    paginationManager.finishLoading()
                    return@collect
                }

                // update total result found
                paginationManager.updateTotalResults(searchUser.totalCount)
                // current user list data to do processing
                val currentUserList = _uiState.value.userList
                // update user list
                val newUserList = if (searchUser.users.isNotEmpty()) {
                    if (paginationManager.page == 1) searchUser.users else currentUserList.plus(searchUser.users)
                } else {
                    currentUserList
                }
                // update UI
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userList = newUserList,
                    totalCount = searchUser.totalCount
                )
                logDebug("UI has been updated after api data is received... ${paginationManager.getInfo()}")
            }
        } catch (exception: Exception) {
            val errorMessage = handleError(exception, "loadUsers")
            paginationManager.finishLoading()
            // update UI
            _uiState.update {
                UsersListState(
                    errorMessage = errorMessage,
                    isLoading = false
                )
            }
        }
        } // end of viewModelScope.launch
    }
}


