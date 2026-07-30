package com.jetpack.compose.github.github.cruise.data.repository.user

import com.jetpack.compose.github.github.cruise.data.local.dao.RepositoryDao
import com.jetpack.compose.github.github.cruise.data.local.dao.UserDao
import com.jetpack.compose.github.github.cruise.data.local.entity.RepositoryEntity
import com.jetpack.compose.github.github.cruise.data.local.entity.UserEntity
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.data.network.model.ApiError
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
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
 * Tests for offline-first caching behavior in UserRepository
 *
 * Tests verify:
 * 1. Cache hits (returns cached data immediately)
 * 2. Cache + Network (shows cache first, then updates from network)
 * 3. Network failure fallback (uses cache when network fails)
 * 4. Cache miss (no cache, fetches from network)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryOfflineCacheTest {

    private val mockNetworkDataSource: NetworkDataSource = mockk()
    private val mockUserDao: UserDao = mockk(relaxed = true)
    private val mockRepositoryDao: RepositoryDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: UserRepositoryImpl

    // Mock data
    private val cachedUserEntity = UserEntity(
        login = "dinkar1708",
        id = 1,
        avatarUrl = "https://cached-avatar.com",
        name = "Cached User",
        company = null,
        blog = null,
        location = null,
        email = null,
        bio = null,
        publicRepos = 50,
        publicGists = 0,
        followers = 100,
        following = 50,
        createdAt = null,
        updatedAt = null,
        htmlUrl = null,
        cachedAt = System.currentTimeMillis()
    )

    private val networkUserProfile = UserProfile(
        id = 1,
        login = "dinkar1708",
        avatarUrl = "https://fresh-avatar.com",
        name = "Fresh User",
        followers = 200,
        following = 100,
        publicRepos = 75
    )

    private val cachedRepoEntities = listOf(
        RepositoryEntity(
            id = 1,
            name = "cached-repo",
            fullName = "dinkar1708/cached-repo",
            ownerLogin = "dinkar1708",
            ownerAvatarUrl = null,
            description = "Cached repository",
            htmlUrl = "https://github.com/dinkar1708/cached-repo",
            language = "Kotlin",
            stargazersCount = 10,
            forksCount = 5,
            watchersCount = 3,
            openIssuesCount = 2,
            createdAt = null,
            updatedAt = null
        )
    )

    private val networkRepos = listOf(
        UserRepo(
            id = 2,
            name = "fresh-repo",
            fullName = "dinkar1708/fresh-repo",
            description = "Fresh repository",
            htmlUrl = "https://github.com/dinkar1708/fresh-repo",
            language = "Kotlin",
            stargazersCount = "20",
            fork = false
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = UserRepositoryImpl(mockNetworkDataSource, mockUserDao, mockRepositoryDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // User Profile Cache Tests
    // ========================================

    @Test
    fun `getUserProfile - cache hit then network update`() = runTest {
        val userName = "dinkar1708"

        // Given: Cache has data
        coEvery { mockUserDao.getUserByLoginOneShot(userName) } returns cachedUserEntity
        coEvery { mockNetworkDataSource.getUserProfile(userName) } returns networkUserProfile

        // When: Get user profile
        val results = repository.getUserProfile(userName).toList()

        // Then: Should emit cached data first, then network data
        assertEquals(2, results.size)

        // First emission: cached data
        assertEquals("Cached User", results[0].name)
        assertEquals(100, results[0].followers)

        // Second emission: fresh network data
        assertEquals("Fresh User", results[1].name)
        assertEquals(200, results[1].followers)

        // Verify cache was inserted with fresh data
        coVerify { mockUserDao.insertUser(any()) }
    }

    @Test
    fun `getUserProfile - cache miss fetches from network only`() = runTest {
        val userName = "dinkar1708"

        // Given: No cache
        coEvery { mockUserDao.getUserByLoginOneShot(userName) } returns null
        coEvery { mockNetworkDataSource.getUserProfile(userName) } returns networkUserProfile

        // When: Get user profile
        val results = repository.getUserProfile(userName).toList()

        // Then: Should emit network data only (no cache)
        assertEquals(1, results.size)
        assertEquals("Fresh User", results[0].name)

        // Verify cache was updated
        coVerify { mockUserDao.insertUser(any()) }
    }

    @Test
    fun `getUserProfile - network failure with cache fallback`() = runTest {
        val userName = "dinkar1708"

        // Given: Cache has data, network fails
        coEvery { mockUserDao.getUserByLoginOneShot(userName) } returns cachedUserEntity
        coEvery { mockNetworkDataSource.getUserProfile(userName) } throws IOException("Network error")

        // When: Get user profile
        val results = repository.getUserProfile(userName).toList()

        // Then: Should only emit cached data (network failed but cache available)
        assertEquals(1, results.size)
        assertEquals("Cached User", results[0].name)
        assertEquals(100, results[0].followers)

        // Verify no cache insert (network failed)
        coVerify(exactly = 0) { mockUserDao.insertUser(any()) }
    }

    @Test
    fun `getUserProfile - network failure without cache throws error`() = runTest {
        val userName = "dinkar1708"

        // Given: No cache, network fails
        coEvery { mockUserDao.getUserByLoginOneShot(userName) } returns null
        coEvery { mockNetworkDataSource.getUserProfile(userName) } throws IOException("Network error")

        // When/Then: Should throw error (no cache, network failed)
        try {
            repository.getUserProfile(userName).first()
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertTrue("Should be IOException", e is IOException)
            assertEquals("Network error", e.message)
        }
    }

    // ========================================
    // User Repositories Cache Tests
    // ========================================

    @Test
    fun `getUserRepositories page 1 - cache hit then network update`() = runTest {
        val userName = "dinkar1708"

        // Given: Cache has data for page 1
        coEvery { mockRepositoryDao.getRepositoriesByOwnerOneShot(userName) } returns cachedRepoEntities
        coEvery { mockNetworkDataSource.getUserRepositories(userName, 1, 100) } returns networkRepos

        // When: Get repositories page 1
        val results = repository.getUserRepositories(userName, 1, 100).toList()

        // Then: Should emit cached data first, then network data
        assertEquals(2, results.size)

        // First emission: cached data
        assertEquals(1, results[0].size)
        assertEquals("cached-repo", results[0][0].name)

        // Second emission: fresh network data
        assertEquals(1, results[1].size)
        assertEquals("fresh-repo", results[1][0].name)

        // Verify cache was cleared and updated
        coVerify { mockRepositoryDao.deleteRepositoriesByOwner(userName) }
        coVerify { mockRepositoryDao.insertRepositories(any()) }
    }

    @Test
    fun `getUserRepositories page 2+ - no cache, network only`() = runTest {
        val userName = "dinkar1708"

        // Given: Network has data for page 2
        coEvery { mockNetworkDataSource.getUserRepositories(userName, 2, 100) } returns networkRepos

        // When: Get repositories page 2
        val results = repository.getUserRepositories(userName, 2, 100).toList()

        // Then: Should only emit network data (pagination doesn't use cache)
        assertEquals(1, results.size)
        assertEquals("fresh-repo", results[0][0].name)

        // Verify cache was updated but not deleted (page > 1)
        coVerify(exactly = 0) { mockRepositoryDao.deleteRepositoriesByOwner(any()) }
        coVerify { mockRepositoryDao.insertRepositories(any()) }
    }

    @Test
    fun `getUserRepositories - network failure with cache fallback`() = runTest {
        val userName = "dinkar1708"

        // Given: Cache has data, network fails
        coEvery { mockRepositoryDao.getRepositoriesByOwnerOneShot(userName) } returns cachedRepoEntities
        coEvery { mockNetworkDataSource.getUserRepositories(userName, 1, 100) } throws IOException("Network error")

        // When: Get repositories
        val results = repository.getUserRepositories(userName, 1, 100).toList()

        // Then: Should only emit cached data
        assertEquals(1, results.size)
        assertEquals("cached-repo", results[0][0].name)

        // Verify cache was NOT deleted (network failed)
        coVerify(exactly = 0) { mockRepositoryDao.deleteRepositoriesByOwner(any()) }
    }

    @Test
    fun `getUserRepositories - network failure without cache throws error`() = runTest {
        val userName = "dinkar1708"

        // Given: No cache, network fails
        coEvery { mockRepositoryDao.getRepositoriesByOwnerOneShot(userName) } returns emptyList()
        coEvery { mockNetworkDataSource.getUserRepositories(userName, 1, 100) } throws IOException("Network error")

        // When/Then: Should throw error
        try {
            repository.getUserRepositories(userName, 1, 100).first()
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            assertTrue("Should be IOException", e is IOException)
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `getUserRepositories - cache only used for page 1`() = runTest {
        val userName = "dinkar1708"

        // Given: Network data available
        coEvery { mockNetworkDataSource.getUserRepositories(userName, 3, 100) } returns networkRepos

        // When: Get repositories page 3
        val results = repository.getUserRepositories(userName, 3, 100).toList()

        // Then: Should not check cache (page > 1)
        coVerify(exactly = 0) { mockRepositoryDao.getRepositoriesByOwnerOneShot(any()) }

        // Should get network data
        assertEquals(1, results.size)
        assertEquals("fresh-repo", results[0][0].name)
    }
}
