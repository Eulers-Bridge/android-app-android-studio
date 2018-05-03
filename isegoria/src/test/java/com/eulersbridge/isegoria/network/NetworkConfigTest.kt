package com.eulersbridge.isegoria.network


import org.junit.Before
import org.junit.Test

class NetworkConfigTest {

    private lateinit var networkConfig: NetworkConfig

    @Before
    fun setUp() {
        networkConfig = NetworkConfig()
    }

    @Test
    fun `resetConfig resets the baseUrl of the network config`() {
        val newBaseUrl = "https://isegoria.com.au"

        networkConfig.baseUrl = newBaseUrl
        networkConfig.resetBaseUrl()

        assert(NetworkConfig.DEFAULT_BASE_URL == networkConfig.baseUrl)
    }

}