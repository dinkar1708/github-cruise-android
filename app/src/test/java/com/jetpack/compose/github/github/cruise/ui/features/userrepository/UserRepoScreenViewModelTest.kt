package com.jetpack.compose.github.github.cruise.ui.features.userrepository

import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.domain.usecase.UserRepositoryUseCase
import com.jetpack.compose.github.github.cruise.network.model.ApiError
import com.jetpack.compose.github.github.cruise.network.model.ApiErrorResponse
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.state.UserRepoViewListState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepoScreenViewModelTest {
    private val mockUserRepositoryUseCase: UserRepositoryUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: UserRepoScreenViewModel

    // mock data
    private val pageNumber = 1
    private val pageSize = 40

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UserRepoScreenViewModel(mockUserRepositoryUseCase, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadApiData() for uiStateProfile & uiStateRepository data on api call success`() =
        runTest {
            // mock data
            val login = "dinkar1708"
            val userProfile = UserProfile(
                id = 1,
                followers = 10,
                following = 20,
                name = "Dinakar Maurya",
                avatarUrl = "url",
                login = "dinkar1708"
            )
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
                    id = 1,
                    name = "1 Repo",
                    language = "JAVA",
                    stargazersCount = "10",
                    description = "2 Android Repo Desc",
                    fork = false
                ),
            )
            // Given
            coEvery {
                mockUserRepositoryUseCase.filterUserRepositories(
                    false,
                    login,
                    pageNumber,
                    pageSize
                )
            } returns flowOf(userRepoList)
            // Given
            coEvery { mockUserRepositoryUseCase.getUserProfile(login) } returns flowOf(userProfile)

            // When
            viewModel.loadApiData(login)

            // Advance time to process the flow
            advanceUntilIdle()

            // Then

            // test user profile
            val stateProfile = viewModel.uiStateProfile.value
            // checking loading cancelled
            Assert.assertEquals(false, stateProfile.isLoading)
            // check that got the user profile in the state
            Assert.assertEquals(userProfile, stateProfile.userProfile)

            // test user repository
            val stateRepository = viewModel.uiStateRepository.value
            // checking loading cancelled
            Assert.assertEquals(false, stateRepository.isLoading)
            // check that got same data in the state
            Assert.assertEquals(userRepoList.size, stateRepository.userRepoList.size)
        }

    @Test
    fun `test loadApiData() for uiStateProfile & uiStateRepository state and message on api call failed`() =
        runTest {
            // Given
            coEvery { mockUserRepositoryUseCase.getUserProfile("dinkar1708") } throws ApiError.ApiException(
                ApiErrorResponse(
                    message = "API rate limit exceeded for 134.180.235.148. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.",
                    documentationUrl = "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                )
            )
            coEvery {
                mockUserRepositoryUseCase.filterUserRepositories(
                    false,
                    "dinkar1708",
                    pageNumber,
                    pageSize
                )
            } throws ApiError.ApiException(
                ApiErrorResponse(
                    message = "API rate limit exceeded for 134.180.235.148. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.",
                    documentationUrl = "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                )
            )

            // When
            // empty text
            viewModel.loadApiData("dinkar1708")

            // Advance time to process the flow
            advanceUntilIdle()

            // Then

            // test profile
            val stateProfile = viewModel.uiStateProfile.value
            // checking loading without mock data
            Assert.assertEquals(false, stateProfile.isLoading)
            // Network error is added from view model so lets test it.
            Assert.assertEquals(
                "API rate limit exceeded for 134.180.235.148. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.",
                stateProfile.errorMessage
            )

            // test repository
            val stateRepository = viewModel.uiStateRepository.value
            // checking loading without mock data
            Assert.assertEquals(false, stateRepository.isLoading)
            // Network error is added from view model so lets test it.
            Assert.assertEquals(
                "API rate limit exceeded for 134.180.235.148. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.",
                stateRepository.errorMessage
            )
        }

    @Test
    fun `test filterRepositories() for uiStateRepository data on api call success`() = runTest {
        // mock data
        val login = "dinkar1708"

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
                id = 1,
                name = "1 Repo",
                language = "JAVA",
                stargazersCount = "10",
                description = "2 Android Repo Desc",
                fork = false
            ),
        )
        // Given
        coEvery {
            mockUserRepositoryUseCase.filterUserRepositories(
                true,
                login,
                pageNumber,
                pageSize
            )
        } returns flowOf(userRepoList)

        // When
        viewModel.filterRepositories(true, login)

        // Advance time to process the flow
        advanceUntilIdle()

        // Then

        // test user repository
        val stateRepository = viewModel.uiStateRepository.value
        // checking loading cancelled
        Assert.assertEquals(false, stateRepository.isLoading)
        // check that got same data in the state
        Assert.assertEquals(userRepoList.size, stateRepository.userRepoList.size)
    }

    @Test
    fun `test filterRepositories() for empty repository data and error message on api call success`() =
        runTest {
            // mock data
            val login = "dinkar1708"

            val userRepoList: List<UserRepo> = emptyList()
            // Given
            // NOTE this time passing true for is showing for repo because our function[filterRepositories] is changing its value
            coEvery {
                mockUserRepositoryUseCase.filterUserRepositories(
                    true,
                    login,
                    pageNumber,
                    pageSize
                )
            } returns flowOf(userRepoList)

            // When
            viewModel.filterRepositories(true, login)

            // Advance time to process the flow
            advanceUntilIdle()

            // Then
            // test state expected value and correct message
            val expectedState = UserRepoViewListState(
                userRepoList = emptyList(),
                isLoading = false,
                errorMessage = "0 results for repositories."
            )
            val stateRepository = viewModel.uiStateRepository.value
            // checking loading cancelled
            Assert.assertEquals(false, stateRepository.isLoading)
            // confirm error message
            Assert.assertEquals(expectedState.errorMessage, stateRepository.errorMessage)
            // check that got empty data
            Assert.assertEquals(expectedState.userRepoList, stateRepository.userRepoList)
        }

    @Test
    fun `test filterRepositories() for error message on api call failure`() = runTest {
        // Given
        val login = "dinkar1708"
        coEvery {
            mockUserRepositoryUseCase.filterUserRepositories(
                false,
                login,
                pageNumber,
                pageSize
            )
        } throws ApiError.NetworkError("Connection timeout")

        // When
        viewModel.filterRepositories(false, login)

        // Advance time to process the flow
        advanceUntilIdle()

        // Then
        val stateRepository = viewModel.uiStateRepository.value
        Assert.assertEquals(false, stateRepository.isLoading)
        Assert.assertEquals("Connection timeout", stateRepository.errorMessage)
        Assert.assertTrue(stateRepository.userRepoList.isEmpty())
    }

    @Test
    fun `test loadApiData() only loads profile when repository data already exists`() = runTest {
        // Given
        val login = "dinkar1708"
        val userProfile = UserProfile(
            id = 1,
            followers = 10,
            following = 20,
            name = "Dinakar Maurya",
            avatarUrl = "url",
            login = "dinkar1708"
        )
        val userRepoList = mutableListOf(
            UserRepo(
                id = 1,
                name = "Repo",
                language = "Kotlin",
                stargazersCount = "10",
                description = "Test Repo",
                fork = false
            )
        )

        coEvery { mockUserRepositoryUseCase.getUserProfile(login) } returns flowOf(userProfile)
        coEvery {
            mockUserRepositoryUseCase.filterUserRepositories(
                false,
                login,
                pageNumber,
                pageSize
            )
        } returns flowOf(userRepoList)

        // Load data first time
        viewModel.loadApiData(login)
        advanceUntilIdle()

        // When - Load data again (should not reload repos)
        viewModel.loadApiData(login)
        advanceUntilIdle()

        // Then
        val stateRepository = viewModel.uiStateRepository.value
        Assert.assertEquals(1, stateRepository.userRepoList.size)
        Assert.assertEquals(false, stateRepository.isLoading)
    }

    // Note: Network error testing for getUserProfile is covered in the existing test:
    // "test loadApiData() for uiStateProfile & uiStateRepository state and message on api call failed"

    @Test
    fun `test filterRepositories() updates isShowingForkRepo state`() = runTest {
        // Given
        val login = "dinkar1708"
        val userRepoList = mutableListOf(
            UserRepo(
                id = 1,
                name = "Repo",
                language = "Kotlin",
                stargazersCount = "10",
                description = "Test",
                fork = true
            )
        )

        coEvery {
            mockUserRepositoryUseCase.filterUserRepositories(
                true,
                login,
                pageNumber,
                pageSize
            )
        } returns flowOf(userRepoList)

        // When
        viewModel.filterRepositories(true, login)
        advanceUntilIdle()

        // Then
        val stateRepository = viewModel.uiStateRepository.value
        Assert.assertEquals(true, stateRepository.isShowingForkRepo)
        Assert.assertEquals(login, stateRepository.login)
    }

    @Test
    fun `test loadApiData() does not crash when user profile flow completes without data`() = runTest {
        // Given
        val login = "testuser"
        coEvery { mockUserRepositoryUseCase.getUserProfile(login) } returns flowOf()
        coEvery {
            mockUserRepositoryUseCase.filterUserRepositories(false, login, pageNumber, pageSize)
        } returns flowOf(emptyList())

        // When
        viewModel.loadApiData(login)
        advanceUntilIdle()

        // Then - Should not crash, profile should remain null
        val stateProfile = viewModel.uiStateProfile.value
        Assert.assertNull(stateProfile.userProfile)
    }

    @Test
    fun `test loadApiData() handles null repository response`() = runTest {
        // Given
        val login = "testuser"
        val userProfile = UserProfile(
            id = 1,
            followers = 5,
            following = 10,
            name = "Test User",
            avatarUrl = "url",
            login = login
        )

        coEvery { mockUserRepositoryUseCase.getUserProfile(login) } returns flowOf(userProfile)
        coEvery {
            mockUserRepositoryUseCase.filterUserRepositories(
                false,
                login,
                pageNumber,
                pageSize
            )
        } returns flowOf()

        // When
        viewModel.loadApiData(login)
        advanceUntilIdle()

        // Then
        val stateRepository = viewModel.uiStateRepository.value
        Assert.assertTrue(stateRepository.userRepoList.isEmpty())
    }

    @Test
    fun `test filtering repositories excludes forks correctly`() = runTest {
        // Given
        val login = "dinkar1708"
        val nonForkRepos = mutableListOf(
            UserRepo(
                id = 1,
                name = "Original Repo",
                language = "Kotlin",
                stargazersCount = "10",
                description = "Not a fork",
                fork = false
            )
        )

        coEvery {
            mockUserRepositoryUseCase.filterUserRepositories(
                false,
                login,
                pageNumber,
                pageSize
            )
        } returns flowOf(nonForkRepos)

        // When
        viewModel.filterRepositories(false, login)
        advanceUntilIdle()

        // Then
        val stateRepository = viewModel.uiStateRepository.value
        Assert.assertEquals(1, stateRepository.userRepoList.size)
        Assert.assertEquals(false, stateRepository.userRepoList[0].fork)
        Assert.assertEquals(false, stateRepository.isShowingForkRepo)
    }
}