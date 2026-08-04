package com.svoysport.tv.ui.screens.profile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DevicesPresentationTest {
    @Test
    fun `exit all is only shown at the three device limit`() {
        assertFalse(shouldShowExitAllDevices(1))
        assertFalse(shouldShowExitAllDevices(2))
        assertTrue(shouldShowExitAllDevices(3))
        assertTrue(shouldShowExitAllDevices(4))
    }
}
