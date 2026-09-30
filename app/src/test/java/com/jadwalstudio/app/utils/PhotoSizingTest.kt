package com.jadwalstudio.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoSizingTest {
    @Test fun smallPhotoNeverUsesZeroDivisor() {
        assertEquals(1, photoSampleSize(100, 100))
        assertEquals(1, photoSampleSize(512, 512))
    }
    @Test fun cameraPhotoIsReducedWithinLimit() {
        assertEquals(8, photoSampleSize(4032, 3024))
        assertEquals(8, photoSampleSize(3024, 4032))
        assertEquals(2, photoSampleSize(513, 512))
    }
    @Test(expected = IllegalArgumentException::class) fun invalidPhotoIsRejected() {
        photoSampleSize(-1, 100)
    }
}
