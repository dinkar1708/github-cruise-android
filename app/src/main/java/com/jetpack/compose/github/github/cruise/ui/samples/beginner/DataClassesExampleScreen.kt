package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber

// Example data classes
data class User(
    val id: Int,
    val name: String,
    val email: String
)

data class Address(
    val street: String,
    val city: String
)

data class UserWithAddress(
    val name: String,
    val address: Address
)

/**
 * Data Classes Example Screen
 * Demonstrates Kotlin data classes features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataClassesExampleScreen(
    onBackClick: () -> Unit
) {
    var user by remember { mutableStateOf(User(1, "John Doe", "john@example.com")) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Classes Examples") },
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
            // Example 1: Auto-generated functions
            ExampleCard(
                title = "1. Auto-Generated Functions",
                description = "equals, hashCode, toString automatically created"
            ) {
                Text("data class User(val id: Int, val name: String)", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Text("Current user:", fontWeight = FontWeight.Bold)
                Text("toString(): $user")

                val user2 = User(1, "John Doe", "john@example.com")
                Text("\nEquals comparison:", fontWeight = FontWeight.Bold)
                Text("user == user2: ${user == user2}")
                Text("user === user2: ${user === user2} (different instances)")

                Timber.d("User toString: $user")
                Timber.d("Equals: ${user == user2}")
            }

            // Example 2: copy() function
            ExampleCard(
                title = "2. copy() Function",
                description = "Create modified copies while keeping original unchanged"
            ) {
                Text("val updated = user.copy(email = \"new@email.com\")", fontWeight = FontWeight.Bold)

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("New Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("New Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val updated = user.copy(
                            name = name.ifEmpty { user.name },
                            email = email.ifEmpty { user.email }
                        )
                        Timber.d("Original: $user")
                        Timber.d("Updated: $updated")
                        user = updated
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply copy()")
                }

                Text("\nOriginal unchanged: ${user.name}", fontSize = 12.sp)
            }

            // Example 3: Destructuring
            ExampleCard(
                title = "3. Destructuring",
                description = "Extract properties into variables"
            ) {
                Text("val (id, name, email) = user", fontWeight = FontWeight.Bold)

                val (id, userName, userEmail) = user

                Text("\nDestructured values:")
                Text("id: $id")
                Text("name: $userName")
                Text("email: $userEmail")

                Timber.d("Destructured - id: $id, name: $userName, email: $userEmail")
            }

            // Example 4: Nested Data Classes
            ExampleCard(
                title = "4. Nested Data Classes",
                description = "Deep copy required for nested objects"
            ) {
                val userWithAddress = UserWithAddress(
                    "Jane",
                    Address("Main St", "NYC")
                )

                Text("Original: $userWithAddress", fontSize = 12.sp)

                Button(onClick = {
                    // Shallow copy
                    val shallow = userWithAddress.copy(name = "Jane Smith")

                    // Deep copy needed for nested
                    val deep = userWithAddress.copy(
                        address = userWithAddress.address.copy(city = "SF")
                    )

                    Timber.d("Shallow copy: $shallow")
                    Timber.d("Deep copy: $deep")
                }) {
                    Text("Show Deep Copy")
                }

                Text(
                    "Check logs for shallow vs deep copy",
                    fontSize = 12.sp
                )
            }

            // Example 5: componentN functions
            ExampleCard(
                title = "5. componentN() Functions",
                description = "Automatically generated for destructuring"
            ) {
                Text("user.component1() = ${user.component1()}")
                Text("user.component2() = ${user.component2()}")
                Text("user.component3() = ${user.component3()}")

                Text("\nUsed internally for destructuring", fontSize = 12.sp)
            }

            // Example 6: When to use
            ExampleCard(
                title = "6. When to Use Data Classes",
                description = "Best practices"
            ) {
                Text("Use for:", fontWeight = FontWeight.Bold)
                Text("• API response models")
                Text("• Database entities")
                Text("• UI state holders")
                Text("• Configuration objects")

                Spacer(modifier = Modifier.height(8.dp))

                Text("Don't use for:", fontWeight = FontWeight.Bold)
                Text("• Classes with behavior/logic")
                Text("• Classes with mutable state")
                Text("• Singleton objects")
            }

            // Console logs
            ExampleCard(
                title = "Console Logs",
                description = "Check Logcat for Timber logs"
            ) {
                Text("Watch logs for:", fontWeight = FontWeight.Bold)
                Text("• toString() output")
                Text("• Equals comparisons")
                Text("• copy() operations")
                Text("• Destructuring values")
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
