package com.jetpack.compose.github.github.cruise.data.repository.search

import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.model.User
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.data.network.model.ApiError
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by Dinakar Maurya on 2024/05/13
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryImplTest {
    private val mockNetworkDataSource: NetworkDataSource = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: SearchRepositoryImpl

    // mock data
    private val user = User(
        id = 1,
        login = "dinkar1708",
        avatarUrl = "https://avatars.githubusercontent.com/u/14831652?v=4",
    )
    private val searchUser = SearchUser(2, true, mutableListOf(user))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = SearchRepositoryImpl(mockNetworkDataSource, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test searchUsers() API call success with result`() {
        runTest {
            val searchUser = SearchUser(2, false, mutableListOf(user))
            val userName = "dinkar1708"
            // Given
            // set mock data for user name
            coEvery {
                mockNetworkDataSource.searchUser(
                    userName = userName,
                    page = 1,
                    pageSize = 10
                )
            } returns searchUser

            // When
            // now call mock api
            val resultFlow = repository.searchUsers(userName = userName, page = 1, pageSize = 10)
            val result = resultFlow.single()

            // Then
            // test
            // for same user mock response and api response must be same
            assertEquals(searchUser, result)
        }
    }

    @Test
    fun `test searchUsers() API call success with incomplete result true`() {
        runTest {
            val userName = "dinkar1708"
            // Given
            // set mock data for user name
            coEvery {
                mockNetworkDataSource.searchUser(
                    userName = userName,
                    page = 1,
                    pageSize = 10
                )
            } returns searchUser

            // When
            // now call mock api
            val resultFlow = repository.searchUsers(userName = userName, page = 1, pageSize = 10)
            val result = resultFlow.single()

            // Then
            // test
            // incomplete result
            assertEquals(result.incompleteResults, searchUser.incompleteResults)
        }
    }

    @Test
    fun `test searchUsers() API call unknown host host error`() {
        runTest {
            // Given
            coEvery {
                mockNetworkDataSource.searchUser(
                    userName = "dinkar1708",
                    page = 1,
                    pageSize = 10
                )
            } throws ApiError.NetworkError("Unknown host error")
            // When
            val resultFlow: Flow<SearchUser> =
                repository.searchUsers(userName = "dinkar1708", page = 1, pageSize = 10)
            resultFlow.catch { e ->
                // Then
                assertTrue(e is ApiError)
                assertTrue(e.message == "Unknown host error")
            }
        }
    }
}