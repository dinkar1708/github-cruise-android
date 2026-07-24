package com.jetpack.compose.github.github.cruise.domain.validation

import javax.inject.Inject

/**
 * Validator for search input following GitHub username rules
 *
 * GitHub username rules:
 * - Cannot be empty
 * - Must be at least 3 characters for search
 * - Cannot exceed 39 characters (GitHub limit)
 * - Can only contain alphanumeric characters and hyphens
 * - Cannot start or end with a hyphen
 */
class SearchInputValidator @Inject constructor() {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(true, null)
            fun error(message: String) = ValidationResult(false, message)
        }
    }

    /**
     * Validate search query for GitHub user search
     */
    fun validateUserSearchQuery(query: String): ValidationResult {
        return when {
            query.isBlank() -> ValidationResult.error("Search query cannot be empty")

            query.length < MIN_SEARCH_LENGTH -> ValidationResult.error(
                "Search query must be at least $MIN_SEARCH_LENGTH characters"
            )

            query.length > MAX_USERNAME_LENGTH -> ValidationResult.error(
                "Username cannot exceed $MAX_USERNAME_LENGTH characters"
            )

            !query.matches(VALID_USERNAME_REGEX) -> ValidationResult.error(
                "Username can only contain letters, numbers, and hyphens"
            )

            query.startsWith("-") || query.endsWith("-") -> ValidationResult.error(
                "Username cannot start or end with a hyphen"
            )

            else -> ValidationResult.success()
        }
    }

    /**
     * Validate repository search query
     */
    fun validateRepositorySearchQuery(query: String): ValidationResult {
        return when {
            query.isBlank() -> ValidationResult.error("Search query cannot be empty")

            query.length < MIN_SEARCH_LENGTH -> ValidationResult.error(
                "Search query must be at least $MIN_SEARCH_LENGTH characters"
            )

            query.length > MAX_QUERY_LENGTH -> ValidationResult.error(
                "Query cannot exceed $MAX_QUERY_LENGTH characters"
            )

            else -> ValidationResult.success()
        }
    }

    /**
     * Normalize search input (trim, lowercase)
     */
    fun normalizeSearchInput(query: String): String {
        return query.trim().lowercase()
    }

    companion object {
        private const val MIN_SEARCH_LENGTH = 3
        private const val MAX_USERNAME_LENGTH = 39  // GitHub limit
        private const val MAX_QUERY_LENGTH = 256
        private val VALID_USERNAME_REGEX = Regex("^[a-zA-Z0-9-]+$")
    }
}
