import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.kotlin.compose)
    jacoco
}

hilt {
    enableAggregatingTask = false
}

// Read properties from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    FileInputStream(localPropertiesFile).use { stream ->
        localProperties.load(stream)
    }
}

// Helper function to get property with fallback
fun getLocalProperty(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key, defaultValue)
}

android {
    namespace = "com.jetpack.compose.github.github.cruise"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jetpack.compose.github.github.cruise"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        multiDexEnabled = true
    }

    buildTypes {
        release {
            manifestPlaceholders += mapOf("app_name" to "GithubCruise")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "DEBUG", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://api.github.com\"")
            buildConfigField("String", "API_VERSION", "\"2022-11-28\"")
            // Example API Key from local.properties (NOT committed to git)
            // NOTE: This WILL be compiled into the APK and is extractable via decompilation
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("API_KEY", "your_api_key_here")}\"")
            // un comment it to run release build to test only using android studio
//            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            manifestPlaceholders += mapOf("app_name" to "DebugGithubCruise")
            buildConfigField("boolean", "DEBUG", "true")
            buildConfigField("String", "API_BASE_URL", "\"https://api.github.com\"")
            buildConfigField("String", "API_VERSION", "\"2022-11-28\"")
            // Example API Key from local.properties (NOT committed to git)
            // NOTE: This WILL be compiled into the APK and is extractable via decompilation
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("API_KEY", "your_api_key_here")}\"")
            // NOTE: API_KEY_EXTRA_SECURE is NOT in BuildConfig
            // It's encrypted and stored in assets/secure_config.enc instead
            applicationIdSuffix = ".debug"
            versionNameSuffix = "debug"
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/gradle/incremental.annotation.processors"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

    testOptions {
        unitTests.all {
            it.configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }
}

dependencies {

    // App dependencies
    implementation(project(path = ":cruise-apm-sdk", configuration = "debugRuntimeElements"))
    implementation(libs.timber)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.play.billing.ktx)

    // Media3 & Live Streaming (ExoPlayer & RTMP)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.datasource.rtmp)

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)



    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Architecture Components
    implementation(libs.androidx.material3.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    implementation(libs.mockk)
    implementation(libs.coil.compose)

    // Hilt
    implementation(libs.hilt.android.core)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Jetpack Compose
    val composeBom = platform(libs.androidx.compose.bom)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.compiler)
    implementation(composeBom)
    implementation(libs.androidx.compose.foundation.core)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)

    debugImplementation(composeBom)
    // preview etc.
    debugImplementation(libs.androidx.compose.ui.tooling.core)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Dependencies for local unit tests
    testImplementation(composeBom)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)

    // Dependencies for Android unit tests
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*Fragment.*",
        "**/*Activity.*",
        "**/*_Factory.*",
        "**/*_HiltModules*.*",
        "**/Hilt_*.*",
        "**/*ComposableSingletons*.*",
        "**/*_Impl.*",
        "**/*Module_*.*",
        "**/di/**"
    )

    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(listOf(mainSrc)))
    classDirectories.setFrom(files(listOf(debugTree)))
    executionData.setFrom(fileTree(buildDir) {
        include("**/*.exec", "**/*.ec")
    })
}