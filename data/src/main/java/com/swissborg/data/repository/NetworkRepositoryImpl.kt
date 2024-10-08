/*
 * © Hugo 2024 for SwissBorg technical challenge
 */

package com.swissborg.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.swissborg.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkRepositoryImpl (
    private val context: Context
) : NetworkRepository {

    //This implementation is used to check if the device is connected to the internet, so the app can react to the network status
    private val _networkStatus = MutableStateFlow(true)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {

        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _networkStatus.value = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkStatus.value = true
            }

            override fun onLost(network: Network) {
                _networkStatus.value = false
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }


    override fun observeNetworkStatus(): Flow<Boolean> = _networkStatus.asStateFlow()
}
