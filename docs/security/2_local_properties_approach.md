# Approach 2: local.properties

## What
Stores API keys in `local.properties` file (gitignored), reads in `build.gradle.kts`, generates BuildConfig.

## How It Works
1. Add to `local.properties`: `API_KEY=your_key`
2. Read in `build.gradle.kts`: `buildConfigField("String", "API_KEY", "\"${getLocalProperty("API_KEY")}\")`
3. Use in code: `val key = BuildConfig.API_KEY`

## Limitations

| Aspect | Status |
|--------|--------|
| In git? | No (gitignored) |
| In APK? | Yes (plain text) |
| Extractable? | Yes (2 minutes with jadx) |
| Compliance | No (PCI-DSS, GDPR) |

**Improvement over Approach 1:** Keeps secrets out of git
**Still vulnerable:** Values still compiled into APK as plain text

**Decompiled APK shows:**
```java
public final class BuildConfig {
    public static final String API_KEY = "sk_live_your_secret_key";  // Still visible!
}
```

## Implementation Example

```kotlin
// local.properties (gitignored)
API_KEY=fake_key_123

// build.gradle.kts
val localProps = Properties().apply {
    rootProject.file("local.properties").inputStream().use { load(it) }
}

buildTypes {
    debug {
        buildConfigField("String", "API_KEY", "\"${localProps["API_KEY"]}\"")
    }
}

// Usage
val key = BuildConfig.API_KEY
```

## Common Mistakes

- Thinking this is secure - APK still has plain text
- Sharing local.properties with team - defeats gitignore purpose
- Not having fallback values - build fails on CI

## References
- [Android Build Config](https://developer.android.com/build/gradle-tips)
- [Gradle Properties](https://docs.gradle.org/current/userguide/build_environment.html)
