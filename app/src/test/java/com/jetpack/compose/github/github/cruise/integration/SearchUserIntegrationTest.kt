package com.jetpack.compose.github.github.cruise.integration

import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.model.User
import com.jetpack.compose.github.github.cruise.domain.usecase.SearchRepositoryUseCase
import com.jetpack.compose.github.github.cruise.ui.features.users.UsersListViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/**
 * Integration test for Search User flow
 * Tests ViewModel + UseCase integration with mocked repository
 * This is a simplified integration test that verifies the flow between layers
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchUserIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockUseCase: SearchRepositoryUseCase
    private lateinit var viewModel: UsersListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockUseCase = mockk()
        viewModel = UsersListViewModel(mockUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `integration test - search user flow from ViewModel to UseCase`() = runTest {
        // Given - Mock UseCase response
        val user = User(
            id = 1,
            login = "dinkar1708",
            avatarUrl = "https://avatars.githubusercontent.com/u/14831652?v=4"
        )
        val searchResult = SearchUser(
            totalCount = 1,
            incompleteResults = false,
            users = mutableListOf(user)
        )

        coEvery {
            mockUseCase.searchUsers(
                userName = "dinkar1708",
                page = 1,
                pageSize = 30
            )
        } returns flowOf(searchResult)

        // When - User searches
        viewModel.searchUsers("dinkar1708")
        advanceUntilIdle()

        // Then - Verify complete flow
        val state = viewModel.uiState.value

        // Verify loading completed
        Assert.assertEquals(false, state.isLoading)

        // Verify user list has data
        Assert.assertEquals(1, state.userList.size)

        // Verify data is correct
        val resultUser = state.userList.first()
        Assert.assertEquals(1, resultUser.id)
        Assert.assertEquals("dinkar1708", resultUser.login)
        Assert.assertEquals("https://avatars.githubusercontent.com/u/14831652?v=4", resultUser.avatarUrl)

        // Verify no error
        Assert.assertEquals("", state.errorMessage)
    }

    @Test
    fun `integration test - search user handles empty results`() = runTest {
        // Given - UseCase returns empty results
        val searchResult = SearchUser(
            totalCount = 0,
            incompleteResults = false,
            users = mutableListOf()
        )

        coEvery {
            mockUseCase.searchUsers(
                userName = "nonexistentuser123456",
                page = 1,
                pageSize = 30
            )
        } returns flowOf(searchResult)

        // When
        viewModel.searchUsers("nonexistentuser123456")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        Assert.assertEquals(false, state.isLoading)
        Assert.assertEquals(0, state.userList.size)
        Assert.assertEquals("Your search did not match any user!", state.errorMessage)
    }

    @Test
    fun `integration test - search user pagination loads multiple pages`() = runTest {
        // Given - First page
        val firstPageUser = User(id = 1, login = "user1", avatarUrl = "url1")
        val firstPageResult = SearchUser(
            totalCount = 100,
            incompleteResults = false,
            users = mutableListOf(firstPageUser)
        )

        // Second page
        val secondPageUser = User(id = 2, login = "user2", avatarUrl = "url2")
        val secondPageResult = SearchUser(
            totalCount = 100,
            incompleteResults = false,
            users = mutableListOf(secondPageUser)
        )

        coEvery {
            mockUseCase.searchUsers(userName = "test", page = 1, pageSize = 30)
        } returns flowOf(firstPageResult)

        coEvery {
            mockUseCase.searchUsers(userName = "test", page = 2, pageSize = 30)
        } returns flowOf(secondPageResult)

        // When - Load first page
        viewModel.searchUsers("test")
        advanceUntilIdle()

        // Then - Verify first page
        var state = viewModel.uiState.value
        Assert.assertEquals(1, state.userList.size)
        Assert.assertEquals("user1", state.userList[0].login)

        // When - Load next page
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then - Verify both pages combined
        state = viewModel.uiState.value
        Assert.assertEquals(2, state.userList.size)
        Assert.assertEquals("user1", state.userList[0].login)
        Assert.assertEquals("user2", state.userList[1].login)
    }

    @Test
    fun `integration test - complete user search journey`() = runTest {
        // Given - Complete search scenario
        val users = mutableListOf(
            User(id = 100, login = "testuser", avatarUrl = "https://example.com/avatar.png")
        )
        val searchResult = SearchUser(
            totalCount = 1,
            incompleteResults = false,
            users = users
        )

        coEvery {
            mockUseCase.searchUsers(any(), any(), any())
        } returns flowOf(searchResult)

        // When - Complete search flow
        viewModel.searchUsers("testuser")
        advanceUntilIdle()

        // Then - Verify end-to-end flow
        val state = viewModel.uiState.value
        val domainUser = state.userList.first()

        // Verify data is correct
        Assert.assertEquals(100, domainUser.id)
        Assert.assertEquals("testuser", domainUser.login)
        Assert.assertEquals("https://example.com/avatar.png", domainUser.avatarUrl)

        // Verify state is ready for UI
        Assert.assertEquals(false, state.isLoading)
        Assert.assertEquals("", state.errorMessage)
    }
}
