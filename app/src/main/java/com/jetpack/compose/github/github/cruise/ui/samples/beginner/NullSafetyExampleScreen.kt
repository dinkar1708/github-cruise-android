package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber

/**
 * Null Safety Example Screen
 * Demonstrates Kotlin's null safety features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NullSafetyExampleScreen(
    onBackClick: () -> Unit
) {
    var nullableText by remember { mutableStateOf<String?>(null) }
    var userInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Null Safety Examples") },
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
            // Example 1: Nullable Type
            ExampleCard(
                title = "1. Nullable Type (?)",
                description = "Variables can be null with ? suffix"
            ) {
                Text("var name: String? = null", fontWeight = FontWeight.Bold)
                Text("Current value: ${nullableText ?: "null"}")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            nullableText = "Hello"
                            Timber.d("Set to: Hello")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set Value")
                    }
                    Button(
                        onClick = {
                            nullableText = null
                            Timber.d("Set to: null")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set Null")
                    }
                }
            }

            // Example 2: Safe Call (?.)
            ExampleCard(
                title = "2. Safe Call (?.)",
                description = "Safely access properties/methods on nullable types"
            ) {
                Text("nullableText?.length", fontWeight = FontWeight.Bold)
                Text("Result: ${nullableText?.length ?: "null"}")
                Text("If null, returns null instead of crash", fontSize = 12.sp)

                Button(onClick = {
                    val length = nullableText?.length
                    result = "Length: ${length ?: "null"}"
                    Timber.d("Safe call result: $length")
                }) {
                    Text("Get Length (Safe)")
                }
                if (result.isNotEmpty()) {
                    Text(result, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Example 3: Elvis Operator (?:)
            ExampleCard(
                title = "3. Elvis Operator (?:)",
                description = "Provide default value if null"
            ) {
                Text("val display = name ?: \"Guest\"", fontWeight = FontWeight.Bold)
                val displayName = nullableText ?: "Guest"
                Text("Result: $displayName", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("If null, uses 'Guest' instead", fontSize = 12.sp)
            }

            // Example 4: Not-Null Assertion (!!)
            ExampleCard(
                title = "4. Not-Null Assertion (!!)",
                description = "Forces unwrap - crashes if null!",
                backgroundColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Text("nullableText!!", fontWeight = FontWeight.Bold)
                Text("WARNING: Throws NullPointerException if null", color = MaterialTheme.colorScheme.error)

                Button(onClick = {
                    try {
                        val value = nullableText!!
                        result = "Success: $value"
                        Timber.d("!! assertion succeeded: $value")
                    } catch (e: NullPointerException) {
                        result = "ERROR: NullPointerException!"
                        Timber.e(e, "!! assertion failed")
                    }
                }) {
                    Text("Try !!")
                }
                if (result.isNotEmpty()) {
                    Text(
                        result,
                        color = if (result.startsWith("ERROR")) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Example 5: let Function
            ExampleCard(
                title = "5. let Function",
                description = "Execute code only if not null"
            ) {
                Text("name?.let { println(it) }", fontWeight = FontWeight.Bold)
                var letResult by remember { mutableStateOf("") }

                Button(onClick = {
                    nullableText?.let {
                        letResult = "Executed with: $it (length: ${it.length})"
                        Timber.d("let block executed: $it")
                    } ?: run {
                        letResult = "Skipped (null)"
                        Timber.d("let block skipped")
                    }
                }) {
                    Text("Run let")
                }
                if (letResult.isNotEmpty()) {
                    Text(letResult, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Example 6: lateinit
            ExampleCard(
                title = "6. lateinit",
                description = "Promise to initialize before use"
            ) {
                Text("lateinit var binding: ViewBinding", fontWeight = FontWeight.Bold)
                Text("For non-null variables initialized later", fontSize = 12.sp)
                Text("Check with ::binding.isInitialized", fontSize = 12.sp)

                var initStatus by remember { mutableStateOf("Not initialized") }
                var lateInitValue by remember { mutableStateOf<String?>(null) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            lateInitValue = "Initialized!"
                            initStatus = "Initialized"
                            Timber.d("lateinit initialized")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Initialize")
                    }
                    Button(
                        onClick = {
                            initStatus = if (lateInitValue != null) {
                                "Value: $lateInitValue"
                            } else {
                                "Not initialized yet"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Check")
                    }
                }
                Text(initStatus, color = MaterialTheme.colorScheme.primary)
            }

            // Interactive Test
            ExampleCard(
                title = "Interactive Test",
                description = "Try entering text"
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { Text("Enter text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val inputOrNull = userInput.ifEmpty { null }
                Text("Safe call: ${inputOrNull?.length}")
                Text("Elvis: ${inputOrNull ?: "Empty"}")
                Text("let: ${inputOrNull?.let { "Length is ${it.length}" } ?: "No text"}")
            }

            // Console Log Example
            ExampleCard(
                title = "Console Logs",
                description = "Check Logcat for Timber logs"
            ) {
                Text("All actions log to Timber:", fontWeight = FontWeight.Bold)
                Text("• Set Value/Null")
                Text("• Safe call operations")
                Text("• !! assertion attempts")
                Text("• let block execution")
            }
        }
    }
}

@Composable
private fun ExampleCard(
    title: String,
    description: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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
