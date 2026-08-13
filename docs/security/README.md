# API Key Security - 4 Approaches

## Quick Comparison

| Approach | What | Extractable? | When to Use |
|----------|------|--------------|-------------|
| **1. BuildConfig** | Hardcoded in build.gradle to APK | Yes (2 min) | Demo only |
| **2. local.properties** | Read from file to APK | Yes (2 min) | Demo only |
| **3. Encrypted Assets** | Encrypted to APK (key in code) | Yes (15 min) | Slows attackers |
| **4. Backend/OAuth** | Not in APK at all | No | Production |

## Limitations

```
Approach 1 & 2: Decompile APK → Get secret immediately
Approach 3:     Decompile APK → Find key in code → Decrypt → Get secret
Approach 4:     Decompile APK → Nothing found (secret on server/user)
```

**Reality:** Only Approach 4 prevents extraction from APK.

## Current Project (Demo)

- `API_KEY` (BuildConfig) - **Fake key**, shows insecure approach
- `API_KEY_EXTRA_SECURE` (Encrypted) - **Fake key**, shows encryption (still extractable)
- `github_token` (Runtime) - **Real**, user's OAuth token (secure)

## Detailed Docs

- `1_buildconfig_approach.md` - BuildConfig (insecure)
- `2_local_properties_approach.md` - local.properties (insecure)
- `3_secure_token_manager_approach.md` - Runtime encryption
- `4_production_best_practices.md` - Backend proxy
- `VERIFICATION.md` - Proof of implementation (runtime logs + code references)

## Example Files (Committed for Demo)

The following files are **intentionally committed** to show working examples:

| File | Purpose | Safe to Commit? |
|------|---------|-----------------|
| `local.properties.example` | Template with fake keys | Yes - example only |
| `app/src/main/assets/secure_config.enc` | Encrypted demo key | Yes - fake key |
| `app/src/main/assets/README.md` | Explains encrypted file | Yes - documentation |

**All keys in these files are FAKE/non-functional.**

**Production Warning:**
- For real secrets, add to `.gitignore`:
  ```gitignore
  # Encrypted secrets
  *.enc
  app/src/main/assets/*.enc

  # Real local.properties (already gitignored)
  local.properties
  ```

## References
- [Android Security](https://developer.android.com/privacy-and-security/security-best-practices)
- [OWASP Mobile](https://mas.owasp.org/MASTG/)
