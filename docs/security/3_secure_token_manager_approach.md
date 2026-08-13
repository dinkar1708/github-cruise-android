# Approach 3: SecureTokenManager

## What
Encrypts secrets on device using Android Keystore. Secret NOT in APK.

## How It Works
1. User enters secret at runtime (e.g., OAuth login)
2. Encrypted with AES256-GCM using MasterKey from Android Keystore
3. Stored in `/data/data/.../shared_prefs/github_cruise_secure_prefs.xml`
4. MasterKey in hardware (Keystore), unique per device

## Limitations

| Protects Against | Doesn't Protect Against |
|------------------|-------------------------|
| APK decompilation | Memory dumps |
| Reverse engineering | Rooted device attacks |
| File copying to another device | Malware with root |

## Encrypted Assets vs SecureTokenManager

| Aspect | Encrypted Assets | SecureTokenManager |
|--------|------------------|-------------------|
| Secret in APK? | Yes (encrypted) | No |
| Decryption key in APK? | Yes (in code) | No (in Keystore) |
| Extractable? | Yes (15 min) | No |
| Compliance | Partial | OWASP MASVS |

## Implementation

**Files:** `SecureTokenManager.kt`, `ApiInterceptor.kt`

```kotlin
// Save
tokenManager.saveSecureApiKey("user_token")

// Retrieve
val token = tokenManager.getSecureApiKey()
```

## Example Keys in Project

| Key | Location | Purpose | Extractable? |
|-----|----------|---------|--------------|
| `API_KEY` | BuildConfig | Demo - insecure approach | Yes (2 min) |
| `API_KEY_EXTRA_SECURE` | assets/secure_config.enc | Demo - encryption | Yes (15 min) |
| `github_token` | SecureTokenManager | Real - user OAuth | No |

**All demo keys are fake/non-functional.**

## References
- [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
- [Android Keystore](https://developer.android.com/privacy-and-security/keystore)
- [OWASP MASVS](https://github.com/OWASP/owasp-masvs)
