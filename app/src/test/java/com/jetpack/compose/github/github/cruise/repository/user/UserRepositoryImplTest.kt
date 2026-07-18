package com.jetpack.compose.github.github.cruise.data.repository.user

import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.data.network.model.ApiError
import com.jetpack.compose.github.github.cruise.data.network.model.ApiErrorResponse
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase
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
 * Created by Dinakar Maurya on 2024/05/14
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private val mockNetworkDataSource: NetworkDataSource = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: UserRepositoryImpl

    // mock data
    private val userProfile = UserProfile(
        id = 1,
        followers = 10,
        following = 20,
        name = "Dinakar Maurya",
        avatarUrl = "https://avatars.githubusercontent.com/u/14831652?v=4",
        login = "dinkar1708"
    )

    private val userRepoList = mutableListOf(
        UserRepo(
            id = 1,
            name = "Repo",
            language = "JAVA",
            stargazersCount = "10",
            description = "Android Repo Desc", fork = false
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = UserRepositoryImpl(mockNetworkDataSource, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test getUserProfile() Users API call success`() {
        runTest {
            val userName = "dinkar1708"
            // Given
            // set mock data for user name
            coEvery { mockNetworkDataSource.getUserProfile(userName = userName) } returns userProfile
            // When
            // now call mock api
            val resultFlow = repository.getUserProfile(userName = userName)
            val result = resultFlow.single()
            // for same user mock response and api response must be same
            TestCase.assertEquals(userProfile, result)
            // Then
            TestCase.assertTrue(userProfile.login == userName)
        }
    }

    @Test
    fun `test getUserProfile() API call rate limit exceeded, api failed`() {
        runTest {
            // Given
            coEvery { mockNetworkDataSource.getUserProfile(userName = "dinkar1708") } throws ApiError.ApiException(
                ApiErrorResponse(
                    message = "API rate limit exceeded for 134.180.235.148. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.",
                    documentationUrl = "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                )
            )
            // When
            val resultFlow: Flow<UserProfile> = repository.getUserProfile(userName = "dinkar1708")
            resultFlow.catch { e ->
                // Then
                TestCase.assertTrue(e is ApiError.ApiException)
                TestCase.assertTrue(e.message == "API rate limit exceeded for 134.180.235.148. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.")
            }
        }
    }

    @Test
    fun `test getUserRepositories() user repository list API call success`() {
        runTest {
            val userName = "dinkar1708"
            // Given
            // set mock data for user name
            coEvery {
                mockNetworkDataSource.getUserRepositories(
                    userName = userName,
                    page = 1,
                    pageSize = 10
                )
            } returns userRepoList

            // When
            // now call mock api
            val resultFlow =
                repository.getUserRepositories(userName = userName, page = 1, pageSize = 10)
            val result = resultFlow.single()
            // Then
            // for same user mock response and api response must be same
            TestCase.assertEquals(userRepoList, result)
        }
    }
}