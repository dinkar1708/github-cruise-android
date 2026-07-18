package com.jetpack.compose.github.github.cruise.ui.features.users

import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.model.User
import com.jetpack.compose.github.github.cruise.domain.usecase.SearchRepositoryUseCase
import com.jetpack.compose.github.github.cruise.data.network.model.ApiError
import com.jetpack.compose.github.github.cruise.data.network.model.ApiErrorResponse
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
 * Created by Dinakar Maurya on 2024/05/13
 */

@OptIn(ExperimentalCoroutinesApi::class)
class UsersListViewModelTest {
    private val mockSearchRepositoryUseCase: SearchRepositoryUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: UsersListViewModel

    private var page = 1
    private val pageSize = 30

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UsersListViewModel(mockSearchRepositoryUseCase, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test searchUsers() for user valid list on search api call success`() =
        runTest {
            // mock data
            // mock data
            val user = User(
                id = 1,
                login = "dinkar1708",
                avatarUrl = "https://avatars.githubusercontent.com/u/14831652?v=4",
            )
            val searchUser = SearchUser(2, true, mutableListOf(user))
            val inputText = "dinkar1708"
            // Given
            coEvery {
                mockSearchRepositoryUseCase.searchUsers(
                    inputText,
                    page,
                    pageSize
                )
            } returns flowOf(searchUser)

            // When
            viewModel.searchUsers(inputText)

            // Advance time to process the flow
            advanceUntilIdle()

            // Then
            // test state expected value and correct message
            // checking loading cancelled
            val stateUserList = viewModel.uiState.value
            // test
            Assert.assertEquals(false, stateUserList.isLoading)
            // check that got data
            Assert.assertEquals(searchUser.users, stateUserList.userList)
        }

    @Test
    fun `test searchUsers() for user no matching user found on api call success`() =
        runTest {
            // mock data
            val inputText = "invalid-user-name-invalid-username-invalid"
            // Given
            val searchUser = SearchUser(0, false, emptyList())
            coEvery {
                mockSearchRepositoryUseCase.searchUsers(
                    inputText,
                    page,
                    pageSize
                )
            } returns flowOf(searchUser)

            // When
            viewModel.searchUsers(inputText)

            // Advance time to process the flow
            advanceUntilIdle()

            // Then
            // test state expected value and correct message
            // checking loading cancelled
            val stateUserList = viewModel.uiState.value
            // test
            // test loading cancelled
            Assert.assertEquals(false, stateUserList.isLoading)
            // test error message
            Assert.assertEquals("Your search did not match any user!", stateUserList.errorMessage)
            // check that 0 results
            Assert.assertTrue(stateUserList.userList.isEmpty())
        }

    @Test
    fun `test searchUsers() for loading end and error message on input text search`() =
        runTest {
            // Given
            val inputText = ""
            // When
            viewModel.searchUsers(inputText)

            // Advance time to process the flow
            advanceUntilIdle()

            // Then

            // test
            // test state expected value and correct message
            val stateUserList = viewModel.uiState.value
            // test loading
            Assert.assertEquals(false, stateUserList.isLoading)
            // check that got correct error message
            Assert.assertEquals("Input user name to search", stateUserList.errorMessage)
        }

    @Test
    fun `test searchUsers() for loading error and error message on api call rate limit error`() =
        runTest {
            // mock data
            val inputText = "dinkar1708"
            // Given
            coEvery {
                mockSearchRepositoryUseCase.searchUsers(
                    inputText,
                    page,
                    pageSize
                )
            } throws ApiError.ApiException(
                ApiErrorResponse(
                    message = "API rate limit exceeded for 134.180.235.148. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.",
                    documentationUrl = "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                )
            )

            // When
            viewModel.searchUsers(inputText)

            // Advance time to process the flow
            advanceUntilIdle()

            // Then
            // test state expected value and correct message
            // checking loading cancelled
            val stateUserList = viewModel.uiState.value
            // test
            // test loading cancelled
            Assert.assertEquals(false, stateUserList.isLoading)
            // test error message
            Assert.assertEquals(
                "API rate limit exceeded for 134.180.235.148. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.",
                stateUserList.errorMessage
            )
        }

    @Test
    fun `test searchUsers() for loading error and error message on api call network error error`() =
        runTest {
            // mock data
            val inputText = "dinkar1708"
            // Given
            coEvery {
                mockSearchRepositoryUseCase.searchUsers(
                    inputText,
                    page,
                    pageSize
                )
            } throws ApiError.NetworkError("Unknown host error!")

            // When
            viewModel.searchUsers(inputText)

            // Advance time to process the flow
            advanceUntilIdle()

            // Then
            // test state expected value and correct message
            // checking loading cancelled
            val stateUserList = viewModel.uiState.value
            // test
            // test loading cancelled
            Assert.assertEquals(false, stateUserList.isLoading)
            // test error message
            Assert.assertEquals("Unknown host error!", stateUserList.errorMessage)
        }

    @Test
    fun `test updateLastVisibleIndex() updates the last visible item index`() = runTest {
        // Given
        val newIndex = 10

        // When
        viewModel.updateLastVisibleIndex(newIndex)

        // Advance time to process state updates
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        Assert.assertEquals(newIndex, state.lastVisibleItemIndex)
    }

    @Test
    fun `test loadNextPage() loads second page when more data available`() = runTest {
        // Given - Setup first page data
        val inputText = "dinkar1708"
        val user1 = User(id = 1, login = "user1", avatarUrl = "url1")
        val user2 = User(id = 2, login = "user2", avatarUrl = "url2")
        val user3 = User(id = 3, login = "user3", avatarUrl = "url3")

        val firstPageResult = SearchUser(totalCount = 100, incompleteResults = false, users = mutableListOf(user1, user2))
        val secondPageResult = SearchUser(totalCount = 100, incompleteResults = false, users = mutableListOf(user3))

        coEvery {
            mockSearchRepositoryUseCase.searchUsers(inputText, 1, pageSize)
        } returns flowOf(firstPageResult)

        coEvery {
            mockSearchRepositoryUseCase.searchUsers(inputText, 2, pageSize)
        } returns flowOf(secondPageResult)

        // Load first page
        viewModel.searchUsers(inputText)
        advanceUntilIdle()

        // When - Load next page
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        Assert.assertEquals(false, state.isLoading)
        Assert.assertEquals(3, state.userList.size) // Should have all 3 users
        Assert.assertEquals("user1", state.userList[0].login)
        Assert.assertEquals("user3", state.userList[2].login)
    }

    @Test
    fun `test loadNextPage() does not load when no more data available`() = runTest {
        // Given - Setup data where all results are loaded
        val inputText = "dinkar1708"
        val user1 = User(id = 1, login = "user1", avatarUrl = "url1")

        // Total count is 1, page size is 30, so only 1 page exists
        val searchResult = SearchUser(totalCount = 1, incompleteResults = false, users = mutableListOf(user1))

        coEvery {
            mockSearchRepositoryUseCase.searchUsers(inputText, 1, pageSize)
        } returns flowOf(searchResult)

        // Load first page
        viewModel.searchUsers(inputText)
        advanceUntilIdle()

        val firstState = viewModel.uiState.value
        val userCountAfterFirstLoad = firstState.userList.size

        // When - Try to load next page (should not trigger API call)
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then - Should still have same data, no new API call
        val secondState = viewModel.uiState.value
        Assert.assertEquals(userCountAfterFirstLoad, secondState.userList.size)
        Assert.assertEquals(1, secondState.userList.size)
    }

    @Test
    fun `test loadNextPage() does not load when already loading`() = runTest {
        // Given
        val inputText = "dinkar1708"
        val user = User(id = 1, login = "user1", avatarUrl = "url1")
        val searchResult = SearchUser(totalCount = 100, incompleteResults = false, users = mutableListOf(user))

        // Simulate slow API call
        coEvery {
            mockSearchRepositoryUseCase.searchUsers(inputText, any(), pageSize)
        } returns flowOf(searchResult)

        // When - Start search
        viewModel.searchUsers(inputText)

        // Try to load next page immediately without waiting (while still loading)
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then - Should only have first page data
        val state = viewModel.uiState.value
        Assert.assertEquals(1, state.userList.size)
    }

    @Test
    fun `test pagination with multiple pages loads all data correctly`() = runTest {
        // Given
        val inputText = "test"
        val page1Users = mutableListOf(
            User(id = 1, login = "user1", avatarUrl = "url1"),
            User(id = 2, login = "user2", avatarUrl = "url2")
        )
        val page2Users = mutableListOf(
            User(id = 3, login = "user3", avatarUrl = "url3"),
            User(id = 4, login = "user4", avatarUrl = "url4")
        )
        val page3Users = mutableListOf(
            User(id = 5, login = "user5", avatarUrl = "url5")
        )

        coEvery {
            mockSearchRepositoryUseCase.searchUsers(inputText, 1, pageSize)
        } returns flowOf(SearchUser(150, false, page1Users))

        coEvery {
            mockSearchRepositoryUseCase.searchUsers(inputText, 2, pageSize)
        } returns flowOf(SearchUser(150, false, page2Users))

        coEvery {
            mockSearchRepositoryUseCase.searchUsers(inputText, 3, pageSize)
        } returns flowOf(SearchUser(150, false, page3Users))

        // When - Load all pages
        viewModel.searchUsers(inputText)
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        Assert.assertEquals(5, state.userList.size)
        Assert.assertEquals("user1", state.userList[0].login)
        Assert.assertEquals("user5", state.userList[4].login)
    }

    @Test
    fun `test searchUsers() resets pagination when new search is performed`() = runTest {
        // Given - Initial search
        val firstSearch = "dinkar"
        val secondSearch = "john"

        val firstUser = User(id = 1, login = "dinkar1708", avatarUrl = "url1")
        val secondUser = User(id = 2, login = "john_doe", avatarUrl = "url2")

        coEvery {
            mockSearchRepositoryUseCase.searchUsers(firstSearch, 1, pageSize)
        } returns flowOf(SearchUser(50, false, mutableListOf(firstUser)))

        coEvery {
            mockSearchRepositoryUseCase.searchUsers(secondSearch, 1, pageSize)
        } returns flowOf(SearchUser(30, false, mutableListOf(secondUser)))

        // When - First search
        viewModel.searchUsers(firstSearch)
        advanceUntilIdle()

        val firstState = viewModel.uiState.value
        Assert.assertEquals(1, firstState.userList.size)
        Assert.assertEquals("dinkar1708", firstState.userList[0].login)

        // When - New search (should reset)
        viewModel.searchUsers(secondSearch)
        advanceUntilIdle()

        // Then - Should have only new search results
        val secondState = viewModel.uiState.value
        Assert.assertEquals(1, secondState.userList.size)
        Assert.assertEquals("john_doe", secondState.userList[0].login)
    }
}