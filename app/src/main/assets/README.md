# Assets Folder - Encrypted Demo Key

## File: secure_config.enc

**Status:** COMMITTED TO GIT (example only)

**Content:** Encrypted demo API key
**Decrypts to:** `sk_secure_9876543210_encrypted_storage_demo` (FAKE KEY)

### Why is this committed?

This file is intentionally committed to demonstrate the encrypted assets security approach. It contains a **non-functional demo key** for educational purposes.

### What's inside?

```
HpQxTebNkS1vKceR3QqYOJWErjhml5aUgZ7u63VmQOziz28P4/LihpljF4Mk4y+q
```

This is an AES-encrypted string that decrypts to a fake API key.

### How it's used

**File:** `AssetEncryptionUtil.kt:37`
```kotlin
val encryptedData = context.assets.open("secure_config.enc").bufferedReader().use { it.readText() }
```

**Decryption:** `AssetEncryptionUtil.kt:53`
```kotlin
private fun decrypt(encryptedData: String, key: String): String {
    // Uses AES/ECB/PKCS5Padding with key from code
}
```

### Security Note

**This approach is still extractable** because:
1. Encrypted data is in APK (this file)
2. Decryption key is in code (`AssetEncryptionUtil.kt:28`)
3. Anyone can decompile APK, find key, decrypt

**Time to extract:** 10-15 minutes

### For Production

**DO NOT commit real encrypted keys.** Instead:

1. Add to `.gitignore`:
```gitignore
# Encrypted secrets
*.enc
app/src/main/assets/*.enc
```

2. Use backend proxy or OAuth instead (Approach 4)

### Documentation

See `docs/security/` for complete security analysis:
- `README.md` - Overview of all approaches
- `3_secure_token_manager_approach.md` - This approach explained
- `VERIFICATION.md` - Proof of implementation
