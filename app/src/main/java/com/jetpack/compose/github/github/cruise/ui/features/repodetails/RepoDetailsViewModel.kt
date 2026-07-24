package com.jetpack.compose.github.github.cruise.ui.features.repodetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.di.DefaultDispatcher
import com.jetpack.compose.github.github.cruise.domain.usecase.RepositoryDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Repository Details screen
 *
 * Threading:
 * - viewModelScope runs on Dispatchers.Main by default (UI thread)
 * - Repository layer handles switching to IO thread via flowOn()
 * - Flow collection happens on Main thread (safe for UI updates)
 */
@HiltViewModel
class RepoDetailsViewModel @Inject constructor(
    private val repositoryDetailsUseCase: RepositoryDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepoDetailsState())
    val uiState: StateFlow<RepoDetailsState> = _uiState.asStateFlow()

    /**
     * Load repository details from URL
     * Parses GitHub URL to extract owner and repo name
     * Example: https://github.com/owner/repo -> owner, repo
     */
    fun loadRepositoryDetails(htmlUrl: String) {
        val (owner, repo) = parseGitHubUrl(htmlUrl)
        if (owner.isEmpty() || repo.isEmpty()) {
            _uiState.update { it.copy(error = "Invalid GitHub URL", isLoading = false) }
            return
        }

        viewModelScope.launch {
            repositoryDetailsUseCase(owner, repo)
                .onStart {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .catch { e ->
                    Timber.e(e, "Error loading repository details")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load repository details"
                        )
                    }
                }
                .collect { details ->
                    _uiState.update {
                        it.copy(
                            repositoryDetails = details,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Parse GitHub URL to extract owner and repo
     * Supports formats:
     * - https://github.com/owner/repo
     * - github.com/owner/repo
     */
    private fun parseGitHubUrl(url: String): Pair<String, String> {
        return try {
            val cleanUrl = url.replace("https://", "").replace("http://", "")
            val parts = cleanUrl.split("/")
            if (parts.size >= 3 && parts[0].contains("github.com")) {
                Pair(parts[1], parts[2])
            } else {
                Pair("", "")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing GitHub URL: $url")
            Pair("", "")
        }
    }
}
