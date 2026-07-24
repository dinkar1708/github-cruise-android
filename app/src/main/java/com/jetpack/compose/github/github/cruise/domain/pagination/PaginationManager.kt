package com.jetpack.compose.github.github.cruise.domain.pagination

import androidx.compose.runtime.Immutable

/**
 * Manages pagination state and logic for paginated API calls
 *
 * This class encapsulates pagination logic following the Single Responsibility Principle
 * and reduces code duplication across ViewModels
 *
 * @param initialPageSize The number of items per page (default: 30)
 */
class PaginationManager(
    private val initialPageSize: Int = DEFAULT_PAGE_SIZE
) {
    private var currentPage = 1
    private var totalResults = 0
    private var isLoading = false

    val pageSize: Int get() = initialPageSize
    val page: Int get() = currentPage
    val isLoadingData: Boolean get() = isLoading

    /**
     * Reset pagination to initial state (call when search query changes)
     */
    fun reset() {
        currentPage = 1
        totalResults = 0
        isLoading = false
    }

    /**
     * Check if next page should be loaded
     *
     * @param currentItemCount Current number of items loaded
     * @return true if more pages available and not currently loading
     */
    fun shouldLoadNextPage(currentItemCount: Int): Boolean {
        return !isLoading && currentItemCount < totalResults
    }

    /**
     * Increment page number and set loading state
     * Call this before making the API request
     *
     * @return The new page number
     */
    fun loadNextPage(): Int {
        if (!isLoading) {
            isLoading = true
            currentPage++
        }
        return currentPage
    }

    /**
     * Set loading state for first page
     * Call this before making the initial API request
     */
    fun startLoading() {
        isLoading = true
    }

    /**
     * Update total results from API response and clear loading state
     *
     * @param total Total number of results from API
     */
    fun updateTotalResults(total: Int) {
        totalResults = total
        isLoading = false
    }

    /**
     * Mark loading as complete (call when API fails or completes)
     */
    fun finishLoading() {
        isLoading = false
    }

    /**
     * Check if there are more pages available
     *
     * @param currentItemCount Current number of items loaded
     * @return true if more pages exist
     */
    fun hasMorePages(currentItemCount: Int): Boolean {
        return currentItemCount < totalResults
    }

    /**
     * Get pagination info for debugging/logging
     */
    fun getInfo(): PaginationInfo {
        return PaginationInfo(
            currentPage = currentPage,
            pageSize = initialPageSize,
            totalResults = totalResults,
            isLoading = isLoading
        )
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 30
    }
}

/**
 * Immutable data class representing current pagination state
 */
@Immutable
data class PaginationInfo(
    val currentPage: Int,
    val pageSize: Int,
    val totalResults: Int,
    val isLoading: Boolean
) {
    val totalPages: Int
        get() = if (pageSize > 0) (totalResults + pageSize - 1) / pageSize else 0

    val hasMorePages: Boolean
        get() = currentPage < totalPages
}
