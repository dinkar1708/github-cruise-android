# Approach 1: BuildConfig

## What
Hardcodes API keys directly in `build.gradle.kts`, compiled into APK as plain text constants.

## How It Works
1. Define in `build.gradle.kts`: `buildConfigField("String", "API_KEY", "\"key\"")`
2. Gradle generates `BuildConfig.java` with constant
3. Use in code: `val key = BuildConfig.API_KEY`

## Limitations

| Aspect | Status |
|--------|--------|
| In git? | Yes (visible in code) |
| In APK? | Yes (plain text) |
| Extractable? | Yes (2 minutes with jadx) |
| Compliance | No (PCI-DSS, GDPR) |

**Decompiled APK shows:**
```java
public final class BuildConfig {
    public static final String API_KEY = "sk_live_your_secret_key";  // Visible!
}
```

## Implementation Example

```kotlin
// build.gradle.kts
buildTypes {
    debug {
        buildConfigField("String", "API_KEY", "\"fake_key_123\"")
    }
}

// Usage
val key = BuildConfig.API_KEY
```

## Common Mistakes

- Using for payment keys (Stripe, etc.) - extractable
- Thinking ProGuard protects string values - it doesn't
- Storing real secrets - anyone can decompile APK

## References
- [Android Build Config](https://developer.android.com/build/gradle-tips)
- [Security Best Practices](https://developer.android.com/privacy-and-security/security-best-practices)
