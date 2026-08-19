package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Hilt DI Example Screen
 * Explains Hilt concepts with project examples
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiltDIExampleScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hilt DI Examples") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Application Setup
            ExampleCard(
                title = "1. Application Setup",
                description = "Entry point for Hilt"
            ) {
                CodeBlock(
                    """
                    @HiltAndroidApp
                    class GithubCruiseApplication : Application() {
                        @Inject
                        lateinit var localeDataStore: LocaleDataStore
                    }
                    """.trimIndent()
                )
                Text("See: App.kt:18", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }

            // ViewModel
            ExampleCard(
                title = "2. ViewModel with Hilt",
                description = "Automatic injection"
            ) {
                CodeBlock(
                    """
                    @HiltViewModel
                    class RepositorySearchViewModel @Inject constructor(
                        private val useCase: RepositorySearchUseCase
                    ) : ViewModel()
                    """.trimIndent()
                )
                Text("See: RepositorySearchViewModel.kt:31", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }

            // Module - Provides
            ExampleCard(
                title = "3. Module - @Provides",
                description = "For classes you don't own"
            ) {
                CodeBlock(
                    """
                    @Module
                    @InstallIn(SingletonComponent::class)
                    object DatabaseModule {
                        @Provides
                        @Singleton
                        fun provideDatabase(
                            @ApplicationContext context: Context
                        ): GithubCruiseDatabase
                    }
                    """.trimIndent()
                )
                Text("See: di/DatabaseModule.kt:25", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }

            // Module - Binds
            ExampleCard(
                title = "4. Module - @Binds",
                description = "For interface implementations"
            ) {
                CodeBlock(
                    """
                    @Module
                    @InstallIn(SingletonComponent::class)
                    abstract class RepositoryModule {
                        @Binds
                        abstract fun bindSearchRepository(
                            impl: SearchRepositoryImpl
                        ): SearchRepository
                    }
                    """.trimIndent()
                )
                Text("See: di/RepositoryModule.kt:31", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }

            // Qualifiers
            ExampleCard(
                title = "5. Qualifiers",
                description = "Multiple instances of same type"
            ) {
                CodeBlock(
                    """
                    @Qualifier
                    annotation class IoDispatcher

                    @Module
                    object CoroutinesModule {
                        @Provides
                        @IoDispatcher
                        fun provideIoDispatcher() = Dispatchers.IO
                    }

                    class Repository @Inject constructor(
                        @IoDispatcher private val dispatcher
                    )
                    """.trimIndent()
                )
                Text("See: di/CoroutinesModule.kt:11", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }

            // Repository Pattern
            ExampleCard(
                title = "6. Repository Pattern",
                description = "Domain interface, Data implementation"
            ) {
                Text("Domain (Interface):", fontWeight = FontWeight.Bold)
                CodeBlock(
                    """
                    interface SearchRepository {
                        fun searchUsers(): Flow<SearchUser>
                    }
                    """.trimIndent()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Data (Implementation):", fontWeight = FontWeight.Bold)
                CodeBlock(
                    """
                    class SearchRepositoryImpl @Inject constructor(
                        private val networkDataSource: NetworkDataSource,
                        @IoDispatcher private val dispatcher
                    ) : SearchRepository
                    """.trimIndent()
                )

                Text("See: domain/repository/ and data/repository/", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }

            // Component Scopes
            ExampleCard(
                title = "7. Component Scopes",
                description = "Control lifetime"
            ) {
                Text("SingletonComponent - App lifetime")
                Text("ActivityComponent - Activity lifetime")
                Text("ViewModelComponent - ViewModel lifetime")

                Spacer(modifier = Modifier.height(8.dp))

                Text("Use @Singleton for app-wide instances", fontSize = 12.sp)
            }

            // Benefits
            ExampleCard(
                title = "8. Benefits",
                description = "Why use Hilt?"
            ) {
                Text("• Automatic dependency creation")
                Text("• Easy to test (swap implementations)")
                Text("• Less boilerplate")
                Text("• Compile-time safety")
                Text("• Lifecycle-aware")
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        code,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Composable
private fun ExampleCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}
