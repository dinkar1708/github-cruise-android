package com.jetpack.compose.github.github.cruise.data.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing Manager for In-App Subscriptions
 * Supports: Querying, Purchasing, Restoring Purchases, and Canceling Subscriptions
 */
@Singleton
class SubscriptionBillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val PRODUCT_ID_XYS_PROD = "xys_prod"
        private const val PLAY_STORE_SUBSCRIPTION_DEEPLINK = "https://play.google.com/store/account/subscriptions"
    }

    // StateFlow representing whether the user currently has an active subscription
    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    // Listener for purchase flow updates
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.d("User canceled the purchase flow")
        } else {
            Timber.w("Purchase failed: code=${billingResult.responseCode}, debug=${billingResult.debugMessage}")
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    /**
     * Step 1: Connect to Google Play Store
     */
    fun connectToPlayStore(
        onSuccess: () -> Unit = {},
        onError: (BillingResult) -> Unit = {}
    ) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Connected to Google Play Billing successfully")
                    onSuccess()
                } else {
                    Timber.e("Billing setup failed: ${billingResult.debugMessage}")
                    onError(billingResult)
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing service disconnected. Will retry on next request.")
            }
        })
    }

    /**
     * Step 2: Fetch subscription product details for xys_prod
     */
    suspend fun getSubscriptions(
        productIds: List<String> = listOf(PRODUCT_ID_XYS_PROD)
    ): List<ProductDetails> = withContext(Dispatchers.IO) {
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(queryParams)

        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val detailsList = result.productDetailsList ?: emptyList()
            Timber.d("Fetched ${detailsList.size} subscription products successfully")
            detailsList
        } else {
            Timber.e("Failed to query product details: ${result.billingResult.debugMessage}")
            emptyList()
        }
    }

    /**
     * Step 3: Launch Google Play Subscription Purchase Flow
     */
    fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String
    ): BillingResult {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        return billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Step 4: RESTORE PURCHASES
     * Queries active purchases from Google Play Store to restore subscriptions
     * (e.g. after reinstalling app or changing devices).
     */
    suspend fun restorePurchases(): List<Purchase> = withContext(Dispatchers.IO) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val result = billingClient.queryPurchasesAsync(params)

        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val activePurchases = result.purchasesList
            Timber.d("Restored ${activePurchases.size} active subscriptions")

            var hasActiveSubscription = false
            for (purchase in activePurchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (purchase.products.contains(PRODUCT_ID_XYS_PROD)) {
                        hasActiveSubscription = true
                    }
                    // Automatically acknowledge any unacknowledged purchases
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
            _isSubscribed.value = hasActiveSubscription
            activePurchases
        } else {
            Timber.e("Failed to restore purchases: ${result.billingResult.debugMessage}")
            emptyList()
        }
    }

    /**
     * Acknowledge purchase to prevent automatic refunds after 3 days
     */
    suspend fun acknowledgePurchase(purchase: Purchase): BillingResult = withContext(Dispatchers.IO) {
        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val result = billingClient.acknowledgePurchase(acknowledgeParams)
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.d("Purchase acknowledged successfully for token: ${purchase.purchaseToken}")
        } else {
            Timber.e("Failed to acknowledge purchase: ${result.debugMessage}")
        }
        result
    }

    /**
     * Handle and Acknowledge purchase to prevent automatic refunds
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            Timber.d("Valid purchase found for token: ${purchase.purchaseToken}")
            if (purchase.products.contains(PRODUCT_ID_XYS_PROD)) {
                _isSubscribed.value = true
            }

            if (!purchase.isAcknowledged) {
                CoroutineScope(Dispatchers.IO).launch {
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    /**
     * Step 5: CANCEL SUBSCRIPTION (Google Play Store Policy)
     * Google Play does NOT permit apps to cancel subscriptions directly via API.
     * Google Play requires directing the user to the Play Store Subscription Management page.
     *
     * @param context Context to launch the Intent
     * @param productId Subscription product ID (default: xys_prod)
     */
    fun openCancelSubscriptionPage(
        context: Context,
        productId: String = PRODUCT_ID_XYS_PROD
    ) {
        val packageName = context.packageName
        val subscriptionUri = Uri.parse(
            "$PLAY_STORE_SUBSCRIPTION_DEEPLINK?sku=$productId&package=$packageName"
        )

        val intent = Intent(Intent.ACTION_VIEW, subscriptionUri).apply {
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
            Timber.d("Opened Google Play subscription management for $productId")
        } catch (e: Exception) {
            // Fallback to web browser if Play Store app is not installed
            val webIntent = Intent(Intent.ACTION_VIEW, subscriptionUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            Timber.w(e, "Play Store app not found. Opened via browser.")
        }
    }
}
