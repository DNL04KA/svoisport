package com.svoysport.tv

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageLoadingPolicyTest {

    @Test
    fun `hardware bitmaps are enabled where Android supports them`() {
        assertFalse(shouldAllowHardwareBitmaps(25))
        assertTrue(shouldAllowHardwareBitmaps(26))
        assertTrue(shouldAllowHardwareBitmaps(35))
    }
}
