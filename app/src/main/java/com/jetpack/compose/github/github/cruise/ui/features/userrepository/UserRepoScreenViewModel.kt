package com.jetpack.compose.github.github.cruise.ui.features.userrepository

import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.di.DefaultDispatcher
import com.jetpack.compose.github.github.cruise.domain.usecase.UserRepositoryUseCase
import com.jetpack.compose.github.github.cruise.ui.base.BaseViewModel
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.state.UserRepoScreenProfileState
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.state.UserRepoViewListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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

    fun loadApiData(login: String) = viewModelScope.launch {
        _uiStateRepository.update { it.copy(login = login) }

        if (_uiStateProfile.value.userProfile == null) {
            loadUserProfile(login)
        }
        if (_uiStateRepository.value.userRepoList.isEmpty()) {
            loadUserRepositories()
        }
    }

    private suspend fun loadUserProfile(login: String) {
        _uiStateProfile.update { it.copy(isLoading = true) }

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

