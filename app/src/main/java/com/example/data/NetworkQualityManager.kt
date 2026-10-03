package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkQualityManager(private val context: Context) {
    private val _networkQuality = MutableStateFlow(NetworkQuality.GOOD)
    val networkQuality: StateFlow<NetworkQuality> = _networkQuality.asStateFlow()

    fun checkNetworkStatus(): NetworkQuality {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork ?: run {
            _networkQuality.value = NetworkQuality.DISCONNECTED
            return NetworkQuality.DISCONNECTED
        }
        val caps = cm.getNetworkCapabilities(network) ?: run {
            _networkQuality.value = NetworkQuality.DISCONNECTED
            return NetworkQuality.DISCONNECTED
        }

        val quality = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkQuality.GOOD
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val downSpeed = caps.linkDownstreamBandwidthKbps
                when {
                    downSpeed > 3000 -> NetworkQuality.GOOD
                    downSpeed > 500 -> NetworkQuality.NORMAL
                    downSpeed > 100 -> NetworkQuality.WEAK
                    else -> NetworkQuality.VERY_WEAK
                }
            }
            else -> NetworkQuality.NORMAL
        }
        _networkQuality.value = quality
        return quality
    }
}
