package com.jetpack.compose.github.github.cruise.domain.usecase

import com.jetpack.compose.github.github.cruise.domain.filter.RepositoryFilter
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryUseCaseTest {

    private val mockRepository: UserRepository = mockk()
    private val mockRepositoryFilter: RepositoryFilter = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepositoryUseCase: UserRepositoryUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepositoryUseCase = UserRepositoryUseCase(mockRepository, mockRepositoryFilter)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test filterUserRepositories() which are not forked on api call success`() = runTest {
        val userRepoList = mutableListOf(
            UserRepo(
                id = 1,
                name = "Repo",
                language = "JAVA",
                stargazersCount = "10",
                description = "Android Repo Desc",
                fork = false
            ),
            UserRepo(
                id = 2,
                name = "Fork Repo",
                language = "Kotlin",
                stargazersCount = "100",
                description = "Fork Repo Desc",
                fork = true
            ),
            UserRepo(
                id = 3,
                name = "Fork Repo2",
                language = "Kotlin",
                stargazersCount = "100",
                description = "Fork Repo Desc2",
                fork = true
            )
        )
        val nonForkedRepos = listOf(userRepoList[0])

        // Given
        coEvery { mockRepository.getUserRepositories("dinkar1708", 1, 20) } returns flowOf(
            userRepoList
        )
        every { mockRepositoryFilter.filterByForkStatus(userRepoList, false) } returns nonForkedRepos

        // When
        val result = userRepositoryUseCase.filterUserRepositories(
            false,
            "dinkar1708", 1, 20,
        )
            .single()
        // Then
        // should filter 1 from given input
        assertEquals(1, result.size)
        // must be not forked ie. false
        assertFalse(result.first().fork)
        assertEquals("Repo", result.first().name)
    }


    @Test
    fun `test filterUserRepositories() which are forked on api call success`() = runTest {
        val userRepoList = mutableListOf(
            UserRepo(
                id = 1,
                name = "Repo",
                language = "JAVA",
                stargazersCount = "10",
                description = "Android Repo Desc",
                fork = false
            ),
            UserRepo(
                id = 2,
                name = "Fork Repo  - test this name",
                language = "Kotlin",
                stargazersCount = "100",
                description = "Fork Repo Desc",
                fork = true
            ),
            UserRepo(
                id = 3,
                name = "Fork Repo2",
                language = "Kotlin",
                stargazersCount = "100",
                description = "Fork Repo Desc2",
                fork = true
            )
        )
        val forkedRepos = listOf(userRepoList[1], userRepoList[2])

        // Given
        coEvery { mockRepository.getUserRepositories("dinkar1708", 1, 20) } returns flowOf(
            userRepoList
        )
        every { mockRepositoryFilter.filterByForkStatus(userRepoList, true) } returns forkedRepos

        // When
        val result = userRepositoryUseCase.filterUserRepositories(
            true,
            "dinkar1708", 1, 20,
        )
            .single()
        // Then
        // should filter 1 from given input
        assertEquals(2, result.size)
        // must be forked
        assertTrue(result.first().fork)
        assertEquals("Fork Repo  - test this name", result.first().name)
    }
}