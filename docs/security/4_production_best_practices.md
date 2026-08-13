# Approach 4: Backend Proxy & OAuth

## What
Keeps secrets OFF device entirely. Backend holds API keys, or user provides their own token (OAuth).

## How It Works

### Option 1: Backend Proxy
```
Android App → Your Backend → Third-party API
              ↑ Has API key
              ↑ Validates requests
```

### Option 2: OAuth (User's Token)
```
User logs in with GitHub/Google → App gets user's token → Stored in SecureTokenManager
```

## Limitations

| Protects Against | Doesn't Protect Against |
|------------------|-------------------------|
| APK decompilation | Server compromise |
| Reverse engineering | User token theft (phishing) |
| Secret extraction | Man-in-the-middle (use HTTPS) |

**Best for:** Payment APIs, cloud services, production systems
**Compliance:** PCI-DSS, GDPR, OWASP MASVS

## Implementation Examples

### Backend Proxy (Node.js)

```javascript
// Backend holds secret
const STRIPE_KEY = process.env.STRIPE_KEY;

app.post('/api/payment', async (req, res) => {
    // Android app calls this, not Stripe directly
    const stripe = require('stripe')(STRIPE_KEY);
    const payment = await stripe.paymentIntents.create({...});
    res.json(payment);
});
```

```kotlin
// Android app - NO secret
suspend fun createPayment(amount: Int) {
    api.createPayment(amount)  // Calls YOUR backend
}
```

### OAuth (GitHub example)

```kotlin
// User logs in, app gets token
fun onGitHubLoginSuccess(token: String) {
    secureTokenManager.saveToken(token)  // User's personal token
}

// Use token
val token = secureTokenManager.getToken()
api.searchUsers(query, token)
```

## Comparison with Other Approaches

| Aspect | BuildConfig | Encrypted Assets | Backend/OAuth |
|--------|-------------|------------------|---------------|
| Secret in APK? | Yes | Yes (encrypted) | No |
| Extractable? | Yes (2 min) | Yes (15 min) | No |
| Production-ready? | No | Partial | Yes |
| Cost if leaked? | High | High | Low/None |

## When to Use

| API Type | Use This |
|----------|----------|
| Stripe, PayPal | Backend proxy |
| AWS, Firebase | Backend proxy |
| GitHub API | OAuth (user's token) |
| Google Sign-In | OAuth (user's token) |
| Analytics (non-sensitive) | BuildConfig acceptable |

## References
- [OAuth 2.0](https://oauth.net/2/)
- [OWASP API Security](https://owasp.org/www-project-api-security/)
- [PCI-DSS Mobile Guidelines](https://www.pcisecuritystandards.org/)
