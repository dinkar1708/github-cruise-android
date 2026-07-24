package com.jetpack.compose.github.github.cruise.domain.filter

import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import javax.inject.Inject

/**
 * Service for filtering repositories based on various criteria
 *
 * Encapsulates filtering logic following Single Responsibility Principle
 * and makes filtering logic testable and reusable
 */
class RepositoryFilter @Inject constructor() {

    /**
     * Filter repositories by fork status
     *
     * @param repositories List of repositories to filter
     * @param showForked true to show only forked repos, false to show only non-forked
     * @return Filtered list of repositories
     */
    fun filterByForkStatus(
        repositories: List<UserRepo>,
        showForked: Boolean
    ): List<UserRepo> {
        return repositories.filter { it.fork == showForked }
    }

    /**
     * Filter repositories by programming language
     *
     * @param repositories List of repositories to filter
     * @param language Language to filter by (null for all)
     * @return Filtered list of repositories
     */
    fun filterByLanguage(
        repositories: List<UserRepo>,
        language: String?
    ): List<UserRepo> {
        if (language.isNullOrBlank()) return repositories
        return repositories.filter {
            it.language?.equals(language, ignoreCase = true) == true
        }
    }

    /**
     * Filter repositories by minimum star count
     *
     * @param repositories List of repositories to filter
     * @param minStars Minimum number of stars (inclusive)
     * @return Filtered list of repositories
     */
    fun filterByMinStars(
        repositories: List<UserRepo>,
        minStars: Int
    ): List<UserRepo> {
        if (minStars <= 0) return repositories
        return repositories.filter { repo ->
            repo.stargazersCount.toIntOrNull()?.let { it >= minStars } ?: false
        }
    }

    /**
     * Filter repositories by search query (searches name and description)
     *
     * @param repositories List of repositories to filter
     * @param query Search query (null or empty for all)
     * @return Filtered list of repositories
     */
    fun filterBySearchQuery(
        repositories: List<UserRepo>,
        query: String?
    ): List<UserRepo> {
        if (query.isNullOrBlank()) return repositories
        val lowerQuery = query.lowercase()
        return repositories.filter { repo ->
            repo.name.lowercase().contains(lowerQuery) ||
            repo.description?.lowercase()?.contains(lowerQuery) == true
        }
    }

    /**
     * Apply multiple filters in sequence
     *
     * @param repositories List of repositories to filter
     * @param criteria Filter criteria to apply
     * @return Filtered list of repositories
     */
    fun applyFilters(
        repositories: List<UserRepo>,
        criteria: FilterCriteria
    ): List<UserRepo> {
        var result = repositories

        criteria.showForked?.let { showForked ->
            result = filterByForkStatus(result, showForked)
        }

        criteria.language?.let { language ->
            result = filterByLanguage(result, language)
        }

        criteria.minStars?.let { minStars ->
            result = filterByMinStars(result, minStars)
        }

        criteria.searchQuery?.let { query ->
            result = filterBySearchQuery(result, query)
        }

        return result
    }
}

/**
 * Data class representing filter criteria
 */
data class FilterCriteria(
    val showForked: Boolean? = null,
    val language: String? = null,
    val minStars: Int? = null,
    val searchQuery: String? = null
) {
    companion object {
        val NONE = FilterCriteria()
    }
}
