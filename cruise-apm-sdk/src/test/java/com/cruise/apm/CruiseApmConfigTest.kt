package com.cruise.apm

import com.cruise.apm.model.SdkEnvironment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CruiseApmConfigTest {

    @Test
    fun `builder creates expected configuration`() {
        val config = CruiseApmConfig.Builder("test_api_key_xyz")
            .setEnvironment(SdkEnvironment.PRODUCTION)
            .setNetworkMonitoringEnabled(true)
            .setAnrWatchdogEnabled(false)
            .setAnrTimeoutMs(3000L)
            .setAutoLifecycleTrackingEnabled(true)
            .setBatchFlushSize(50)
            .setLoggingEnabled(true)
            .build()

        assertEquals("test_api_key_xyz", config.apiKey)
        assertEquals(SdkEnvironment.PRODUCTION, config.environment)
        assertTrue(config.enableNetworkMonitoring)
        assertFalse(config.enableAnrWatchdog)
        assertEquals(3000L, config.anrTimeoutMs)
        assertTrue(config.enableAutoLifecycleTracking)
        assertEquals(50, config.batchFlushSize)
        assertTrue(config.enableLogging)
    }

    @Test
    fun `default builder uses sandbox and default timeouts`() {
        val config = CruiseApmConfig.Builder("key_123").build()

        assertEquals(SdkEnvironment.SANDBOX, config.environment)
        assertTrue(config.enableNetworkMonitoring)
        assertTrue(config.enableAnrWatchdog)
        assertEquals(5000L, config.anrTimeoutMs)
        assertEquals(20, config.batchFlushSize)
        assertFalse(config.enableLogging)
    }
}
