package com.jetpack.compose.github.github.cruise.data.repository.search

import com.jetpack.compose.github.github.cruise.data.local.dao.SearchUserDao
import com.jetpack.compose.github.github.cruise.data.local.entity.SearchUserEntity
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Tests for offline-first caching behavior in SearchRepository
 *
 * Tests verify:
 * 1. Cache hits (returns cached search results immediately)
 * 2. Cache + Network (shows cache first, then updates from network)
 * 3. Network failure fallback (uses cache when network fails)
 * 4. Cache miss (no cache, fetches from network)
 * 5. Query normalization (lowercase matching)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryOfflineCacheTest {

    private val mockNetworkDataSource: NetworkDataSource = mockk()
    private val mockSearchUserDao: SearchUserDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: SearchRepositoryImpl

    // Mock data
    private val cachedUserEntities = listOf(
        SearchUserEntity(
            id = 1,
            login = "cached-user",
            avatarUrl = "https://cached-avatar.com",
            score = 90.0,
            query = "dinkar",
            cachedAt = System.currentTimeMillis()
        ),
        SearchUserEntity(
            id = 2,
            login = "cached-user-2",
            avatarUrl = "https://cached-avatar-2.com",
            score = 85.0,
            query = "dinkar",
            cachedAt = System.currentTimeMillis()
        )
    )

    private val networkSearchResult = SearchUser(
        totalCount = 3,
        incompleteResults = false,
        users = listOf(
            User(
                id = 3,
                login = "fresh-user",
                avatarUrl = "https://fresh-avatar.com",
                score = 95.0
            ),
            User(
                id = 4,
                login = "fresh-user-2",
                avatarUrl = "https://fresh-avatar-2.com",
                score = 88.0
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = SearchRepositoryImpl(mockNetworkDataSource, mockSearchUserDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // Search Users Cache Tests
    // ========================================

    @Test
    fun `searchUsers page 1 - cache hit then network update`() = runTest {
        val query = "dinkar"

        // Given: Cache has data
        coEvery { mockSearchUserDao.getSearchResultsOneShot(query) } returns cachedUserEntities
        coEvery { mockNetworkDataSource.searchUser(query, 1, 30) } returns networkSearchResult

        // When: Search users
        val results = repository.searchUsers(query, 1, 30).toList()

        // Then: Should emit cached data first, then network data
        assertEquals(2, results.size)

        // First emission: cached data
        assertEquals(2, results[0].totalCount)
        assertEquals("cached-user", results[0].users[0].login)
        assertEquals("cached-user-2", results[0].users[1].login)

        // Second emission: fresh network data
        assertEquals(3, results[1].totalCount)
        assertEquals("fresh-user", results[1].users[0].login)
        assertEquals("fresh-user-2", results[1].users[1].login)

        // Verify cache was cleared and updated
        coVerify { mockSearchUserDao.deleteSearchResults(query) }
        coVerify { mockSearchUserDao.insertSearchResults(any()) }
    }

    @Test
    fun `searchUsers - query normalization to lowercase`() = runTest {
        val query = "DINKAR" // uppercase

        // Given: Cache uses lowercase
        coEvery { mockSearchUserDao.getSearchResultsOneShot("dinkar") } returns cachedUserEntities
        coEvery { mockNetworkDataSource.searchUser("DINKAR", 1, 30) } returns networkSearchResult

        // When: Search with uppercase query
        repository.searchUsers(query, 1, 30).first()

        // Then: Should normalize to lowercase for cache lookup
        coVerify { mockSearchUserDao.getSearchResultsOneShot("dinkar") }
        coVerify { mockSearchUserDao.deleteSearchResults("dinkar") }
    }

    @Test
    fun `searchUsers - cache miss fetches from network only`() = runTest {
        val query = "newquery"

        // Given: No cache
        coEvery { mockSearchUserDao.getSearchResultsOneShot(query) } returns emptyList()
        coEvery { mockNetworkDataSource.searchUser(query, 1, 30) } returns networkSearchResult

        // When: Search users
        val results = repository.searchUsers(query, 1, 30).toList()

        // Then: Should only emit network data (no cache)
        assertEquals(1, results.size)
        assertEquals(3, results[0].totalCount)
        assertEquals("fresh-user", results[0].users[0].login)

        // Verify cache was updated
        coVerify { mockSearchUserDao.insertSearchResults(any()) }
    }

    @Test
    fun `searchUsers - network failure with cache fallback`() = runTest {
        val query = "dinkar"

        // Given: Cache has data, network fails
        coEvery { mockSearchUserDao.getSearchResultsOneShot(query) } returns cachedUserEntities
        coEvery { mockNetworkDataSource.searchUser(query, 1, 30) } throws IOException("Network error")

        // When: Search users
        val results = repository.searchUsers(query, 1, 30).toList()

        // Then: Should only emit cached data (network failed)
        assertEquals(1, results.size)
        assertEquals(2, results[0].totalCount)
        assertEquals("cached-user", results[0].users[0].login)

        // Verify cache was NOT deleted (network failed)
        coVerify(exactly = 0) { mockSearchUserDao.deleteSearchResults(any()) }
    }

    @Test
    fun `searchUsers - network failure without cache throws error`() = runTest {
        val query = "newquery"

        // Given: No cache, network fails
        coEvery { mockSearchUserDao.getSearchResultsOneShot(query) } returns emptyList()
        coEvery { mockNetworkDataSource.searchUser(query, 1, 30) } throws IOException("Network error")

        // When/Then: Should throw error (no cache, network failed)
        try {
            repository.searchUsers(query, 1, 30).first()
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertTrue("Should be IOException", e is IOException)
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `searchUsers page 2+ - no cache, network only`() = runTest {
        val query = "dinkar"

        // Given: Network has data for page 2
        coEvery { mockNetworkDataSource.searchUser(query, 2, 30) } returns networkSearchResult

        // When: Search users page 2
        val results = repository.searchUsers(query, 2, 30).toList()

        // Then: Should only emit network data (pagination doesn't use cache)
        assertEquals(1, results.size)
        assertEquals("fresh-user", results[0].users[0].login)

        // Verify cache was not checked (page > 1)
        coVerify(exactly = 0) { mockSearchUserDao.getSearchResultsOneShot(any()) }

        // Verify cache was updated but not deleted (page > 1)
        coVerify(exactly = 0) { mockSearchUserDao.deleteSearchResults(any()) }
    }

    @Test
    fun `searchUsers - empty cache returns empty list not error`() = runTest {
        val query = "dinkar"

        // Given: Empty cache, network succeeds
        coEvery { mockSearchUserDao.getSearchResultsOneShot(query) } returns emptyList()
        coEvery { mockNetworkDataSource.searchUser(query, 1, 30) } returns networkSearchResult

        // When: Search users
        val results = repository.searchUsers(query, 1, 30).toList()

        // Then: Should get network data (cache was empty but didn't error)
        assertEquals(1, results.size)
        assertEquals(3, results[0].totalCount)
    }

    @Test
    fun `searchUsers - different queries have separate caches`() = runTest {
        val query1 = "dinkar"
        val query2 = "android"

        // Given: Different cache for different queries
        coEvery { mockSearchUserDao.getSearchResultsOneShot(query1) } returns cachedUserEntities
        coEvery { mockSearchUserDao.getSearchResultsOneShot(query2) } returns emptyList()
        coEvery { mockNetworkDataSource.searchUser(any(), any(), any()) } returns networkSearchResult

        // When: Search with query1
        repository.searchUsers(query1, 1, 30).first()

        // Then: Should use query1 cache
        coVerify { mockSearchUserDao.getSearchResultsOneShot(query1) }

        // When: Search with query2
        repository.searchUsers(query2, 1, 30).first()

        // Then: Should use query2 cache (separate)
        coVerify { mockSearchUserDao.getSearchResultsOneShot(query2) }
    }
}
