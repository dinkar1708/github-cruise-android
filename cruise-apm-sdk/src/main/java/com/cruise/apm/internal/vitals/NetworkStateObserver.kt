package com.cruise.apm.internal.vitals

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Observes real-time Android network connectivity states using ConnectivityManager.NetworkCallback.
 */
internal class NetworkStateObserver(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _networkType = MutableStateFlow(determineCurrentNetwork())
    val networkType: StateFlow<String> = _networkType.asStateFlow()

    private val _isMetered = MutableStateFlow(connectivityManager?.isActiveNetworkMetered ?: false)
    val isMetered: StateFlow<Boolean> = _isMetered.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun start() {
        if (connectivityManager == null) return
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateNetworkState()
                }

                override fun onLost(network: Network) {
                    updateNetworkState()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    updateNetworkState()
                }
            }

            connectivityManager.registerNetworkCallback(request, callback)
            this.networkCallback = callback
        } catch (e: Throwable) {
            // Fallback gracefully on environments with restricted permissions
        }
    }

    fun stop() {
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (ignored: Throwable) {}
        networkCallback = null
    }

    private fun updateNetworkState() {
        _networkType.value = determineCurrentNetwork()
        _isMetered.value = connectivityManager?.isActiveNetworkMetered ?: false
    }

    fun getCurrentNetworkType(): String = determineCurrentNetwork()
    fun isCurrentlyMetered(): Boolean = connectivityManager?.isActiveNetworkMetered ?: false

    private fun determineCurrentNetwork(): String {
        val cm = connectivityManager ?: return "UNKNOWN"
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val activeNetwork = cm.activeNetwork ?: return "OFFLINE"
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return "OFFLINE"

            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                else -> "CONNECTED"
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo ?: return "OFFLINE"
            @Suppress("DEPRECATION")
            when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> "WIFI"
                ConnectivityManager.TYPE_MOBILE -> "CELLULAR"
                ConnectivityManager.TYPE_ETHERNET -> "ETHERNET"
                ConnectivityManager.TYPE_VPN -> "VPN"
                else -> if (networkInfo.isConnected) "CONNECTED" else "OFFLINE"
            }
        }
    }
}
