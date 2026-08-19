package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import timber.log.Timber

/**
 * Passing Data Between Screens - Main Screen
 * Demonstrates different methods of passing data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassingDataExampleScreen(
    onBackClick: () -> Unit,
    onNavigateWithId: (String) -> Unit,
    onNavigateWithMultiple: (String, String) -> Unit,
    onNavigateWithSharedViewModel: () -> Unit,
    viewModel: PassingDataExampleViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passing Data Examples") },
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
            // Header
            Text(
                "Choose a method to pass data:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Method 1: Route Arguments (Simple ID)
            ExampleCard(
                title = "Method 1: Route Arguments",
                description = "Pass simple ID via route"
            ) {
                Text(
                    """
                    Route: "details/{itemId}"
                    Navigate: navigate("details/123")
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Button(
                    onClick = {
                        val itemId = items.firstOrNull()?.id ?: "1"
                        Timber.d("Navigating with ID: $itemId")
                        onNavigateWithId(itemId)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Navigate with ID")
                }

                Text("Best for: Simple primitives (String, Int, Boolean)", fontSize = 12.sp)
            }

            // Method 2: Multiple Arguments
            ExampleCard(
                title = "Method 2: Multiple Arguments",
                description = "Pass multiple parameters"
            ) {
                Text(
                    """
                    Route: "profile/{userId}/{userName}"
                    Navigate: navigate("profile/123/John")
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Button(
                    onClick = {
                        Timber.d("Navigating with multiple args")
                        onNavigateWithMultiple("123", "JohnDoe")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Navigate with Multiple Args")
                }

                Text("Best for: 2-3 simple parameters", fontSize = 12.sp)
            }

            // Method 3: SharedViewModel
            ExampleCard(
                title = "Method 3: SharedViewModel",
                description = "Share complex data via ViewModel"
            ) {
                Text("Select an item from list:", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                items.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectItem(item)
                                Timber.d("Item selected via SharedViewModel: ${item.title}")
                                onNavigateWithSharedViewModel()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Text(item.description, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    "Best for: Complex objects, shared state",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Type-Safe Navigation explanation
            ExampleCard(
                title = "Method 4: Type-Safe Navigation",
                description = "Modern approach (Navigation 2.8.0+)"
            ) {
                Text(
                    """
                    @Serializable
                    data class Profile(val userId: String)

                    navController.navigate(Profile("123"))
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )

                Text(
                    "✓ Compile-time safety\n✓ No string manipulation\n✓ IDE autocomplete",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Comparison
            ExampleCard(
                title = "When to Use What",
                description = "Quick reference guide"
            ) {
                Text("Route Args: Simple IDs, primitives")
                Text("Multiple Args: 2-3 parameters")
                Text("SharedViewModel: Complex objects")
                Text("Type-Safe: Modern apps (Nav 2.8.0+)")
                Text("JSON: Legacy complex objects")
            }

            // Notes
            Text(
                "Note: Click a method above to see it in action!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Details Screen - Receives data via route argument
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassingDataDetailsScreen(
    itemId: String?,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Method 1: Route Argument") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExampleCard(
                title = "Received Data",
                description = "Data passed via route argument"
            ) {
                Text("Item ID: ${itemId ?: "null"}", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Text("Code used:", fontWeight = FontWeight.Bold)
                Text(
                    """
                    // In navigation graph:
                    composable("details/{itemId}") { entry ->
                        val id = entry.arguments?.getString("itemId")
                        DetailsScreen(id)
                    }

                    // Navigate:
                    navController.navigate("details/$itemId")
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )

                Timber.d("Details screen displayed with itemId: $itemId")
            }

            ExampleCard(
                title = "Pros & Cons",
                description = "Method 1 analysis"
            ) {
                Text("✓ Simple and straightforward", color = MaterialTheme.colorScheme.primary)
                Text("✓ URL-friendly", color = MaterialTheme.colorScheme.primary)
                Text("✓ Works with deep links", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("✗ Only primitives", color = MaterialTheme.colorScheme.error)
                Text("✗ Manual parsing", color = MaterialTheme.colorScheme.error)
                Text("✗ No type safety", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * Profile Screen - Receives multiple arguments
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassingDataProfileScreen(
    userId: String?,
    userName: String?,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Method 2: Multiple Args") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExampleCard(
                title = "Received Data",
                description = "Multiple arguments passed"
            ) {
                Text("User ID: ${userId ?: "null"}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("User Name: ${userName ?: "null"}", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    """
                    Route: "profile/{userId}/{userName}"
                    Navigate: navigate("profile/123/John")
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )

                Timber.d("Profile screen: userId=$userId, userName=$userName")
            }

            ExampleCard(
                title = "Best Practices",
                description = "Tips for multiple arguments"
            ) {
                Text("• Keep to 2-3 parameters max")
                Text("• Use meaningful parameter names")
                Text("• Consider SharedViewModel for more data")
                Text("• Validate all arguments for null")
            }
        }
    }
}

/**
 * Shared Data Screen - Receives data via SharedViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassingDataSharedScreen(
    onBackClick: () -> Unit,
    viewModel: PassingDataExampleViewModel = hiltViewModel()
) {
    val selectedItem by viewModel.selectedItem.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Method 3: SharedViewModel") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedItem?.let { item ->
                ExampleCard(
                    title = "Received Data",
                    description = "Complex object from SharedViewModel"
                ) {
                    Text("ID: ${item.id}", fontSize = 16.sp)
                    Text("Title: ${item.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Description: ${item.description}", fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Full object available:", fontWeight = FontWeight.Bold)
                    Text("• All properties accessible")
                    Text("• No serialization needed")
                    Text("• Survives rotation")

                    Timber.d("Shared screen displaying: ${item.title}")
                }
            } ?: run {
                Text("No item selected", fontSize = 18.sp)
            }

            ExampleCard(
                title = "How It Works",
                description = "SharedViewModel pattern"
            ) {
                Text(
                    """
                    // In ViewModel:
                    private val _selectedItem = MutableStateFlow<Item?>(null)
                    val selectedItem = _selectedItem.asStateFlow()

                    fun selectItem(item: Item) {
                        _selectedItem.value = item
                    }

                    // In Screen A:
                    viewModel.selectItem(item)
                    navController.navigate("details")

                    // In Screen B:
                    val item by viewModel.selectedItem.collectAsState()
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            ExampleCard(
                title = "Advantages",
                description = "Why use SharedViewModel"
            ) {
                Text("✓ Complex objects", color = MaterialTheme.colorScheme.primary)
                Text("✓ Survives configuration changes", color = MaterialTheme.colorScheme.primary)
                Text("✓ No serialization", color = MaterialTheme.colorScheme.primary)
                Text("✓ Shared between multiple screens", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
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
