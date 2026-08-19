package com.jetpack.compose.github.github.cruise.ui.features.userrepository

import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.domain.usecase.UserRepositoryUseCase
import com.jetpack.compose.github.github.cruise.ui.base.BaseViewModel
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.state.UserRepoScreenProfileState
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.state.UserRepoViewListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/14.
 *
 * Threading:
 * - viewModelScope runs on Dispatchers.Main by default (UI thread)
 * - Repository layer handles switching to IO thread via flowOn()
 * - Flow collection happens on Main thread (safe for UI updates)
 */
@HiltViewModel
class UserRepoScreenViewModel @Inject constructor(
    private val userRepositoryUseCase: UserRepositoryUseCase
) : BaseViewModel() {
    private val _uiStateRepository = MutableStateFlow(UserRepoViewListState())
    val uiStateRepository: StateFlow<UserRepoViewListState> = _uiStateRepository.asStateFlow()

    private val _uiStateProfile = MutableStateFlow(UserRepoScreenProfileState())
    val uiStateProfile: StateFlow<UserRepoScreenProfileState> = _uiStateProfile.asStateFlow()

    override val TAG = "UserRepoScreenViewModel"

    /**
     * PATTERN 1: SERIAL / SEQUENTIAL API CALLS
     *
     * Single coroutine: Loads profile first, then repositories (one after another).
     * Use case: When API #2 depends on data from API #1.
     * See docs/technical/API_CALL_PATTERNS.md for comparison.
     */
    fun loadApiDataSerial(login: String) = viewModelScope.launch {
        _uiStateRepository.update { it.copy(login = login) }

        // Sequential API calls - second waits for first to complete
        if (_uiStateProfile.value.userProfile == null) {
            loadUserProfile(login)
        }
        if (_uiStateRepository.value.userRepoList.isEmpty()) {
            loadUserRepositories()
        }
    }

    /**
     * PATTERN 2: PARALLEL VIA ASYNC / AWAIT (Coordinated Execution)
     *
     * Single coroutine with async: Dispatches both calls concurrently and awaits completion.
     * Use case: When you need data from both APIs before proceeding to the next step.
     */
    fun loadApiDataParallelAsync(login: String) = viewModelScope.launch {
        _uiStateRepository.update { it.copy(login = login) }

        // Parallel API calls using async/await
        if (_uiStateProfile.value.userProfile == null ||
            _uiStateRepository.value.userRepoList.isEmpty()
        ) {
            val profileDeferred = async {
                if (_uiStateProfile.value.userProfile == null) {
                    loadUserProfile(login)
                }
            }

            val reposDeferred = async {
                if (_uiStateRepository.value.userRepoList.isEmpty()) {
                    loadUserRepositories()
                }
            }

            // Wait for both to complete
            profileDeferred.await()
            reposDeferred.await()
        }
    }

    /**
     * PATTERN 3: PARALLEL VIA SEPARATE LAUNCH (Independent / Progressive Loading)
     *
     * Two separate coroutines: Profile and Repositories run completely independently.
     * Best UX: Profile renders immediately when ready without waiting for repositories.
     * Error Isolation: Failure in one API call does not cancel or block the other.
     */
    fun loadApiDataParallelSeparateLaunch(login: String) {
        _uiStateRepository.update { it.copy(login = login) }

        // 🚀 Coroutine 1: Load profile independently
        viewModelScope.launch {
            if (_uiStateProfile.value.userProfile == null) {
                loadUserProfile(login)
            }
        }

        // 🚀 Coroutine 2: Load repositories in parallel
        viewModelScope.launch {
            if (_uiStateRepository.value.userRepoList.isEmpty()) {
                loadUserRepositories()
            }
        }
    }

    /**
     * Default convenience methods
     */
    fun loadApiData(login: String) = loadApiDataSerial(login)
    fun loadApiDataParallel(login: String) = loadApiDataParallelSeparateLaunch(login)



    private suspend fun loadUserProfile(login: String) {
        _uiStateProfile.update { it.copy(isLoading = true) }
        logDebug("loadUserProfile - START")
        val startTime = System.currentTimeMillis()

        try {
            val userProfile = userRepositoryUseCase.getUserProfile(login = login)
                .catch { exception ->
                    val errorMessage = handleError(exception, "loadUserProfile")
                    _uiStateProfile.update {
                        it.copy(
                            errorMessage = errorMessage,
                            isLoading = false
                        )
                    }
                }
                .singleOrNull() ?: return
            _uiStateProfile.update {
                it.copy(
                    userProfile = userProfile,
                    isLoading = false,
                    errorMessage = ""
                )
            }
            val endTime = System.currentTimeMillis()
            logDebug("loadUserProfile - END (${endTime - startTime}ms)")
            // Update repository state with total count from user profile
            _uiStateRepository.update {
                it.copy(totalRepos = userProfile.publicRepos)
            }
        } catch (exception: Exception) {
            val errorMessage = handleError(exception, "loadUserProfile unexpected")
            _uiStateProfile.update {
                it.copy(
                    errorMessage = errorMessage,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadUserRepositories() {
        _uiStateRepository.update { it.copy(isLoading = true) }
        logDebug("loadUserRepositories - START")
        val startTime = System.currentTimeMillis()

        try {
            val repositories =
                userRepositoryUseCase.filterUserRepositories(
                    _uiStateRepository.value.isShowingForkRepo,
                    login = _uiStateRepository.value.login,
                    1,
                    40
                )
                    .catch { exception ->
                        val errorMessage = handleError(exception, "loadUserRepositories")
                        _uiStateRepository.update {
                            it.copy(
                                errorMessage = errorMessage,
                                isLoading = false
                            )
                        }
                    }
                    .singleOrNull() ?: return

            val endTime = System.currentTimeMillis()
            logDebug("loadUserRepositories - END (${endTime - startTime}ms)")

            if (repositories.isEmpty()) {
                _uiStateRepository.update {
                    it.copy(
                        userRepoList = emptyList(),
                        isLoading = false,
                        errorMessage = "0 results for repositories."
                    )
                }
            } else {
                _uiStateRepository.update {
                    it.copy(
                        userRepoList = repositories,
                        isLoading = false,
                        errorMessage = ""
                    )
                }
            }
        } catch (exception: Exception) {
            val errorMessage = handleError(exception, "loadUserRepositories unexpected")

            _uiStateRepository.update {
                it.copy(
                    errorMessage = errorMessage,
                    isLoading = false
                )
            }
        }
    }

    fun filterRepositories(isShowingForkRepo: Boolean, login: String) =
        viewModelScope.launch {
            _uiStateRepository.update {
                it.copy(isShowingForkRepo = isShowingForkRepo, login = login)
            }
            loadUserRepositories()
        }
}

