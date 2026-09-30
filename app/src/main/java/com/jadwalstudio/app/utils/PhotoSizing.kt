package com.jadwalstudio.app.utils

/** BitmapFactory requires a positive power-of-two sampling factor. */
fun photoSampleSize(width: Int, height: Int, limit: Int = 512): Int {
    require(width > 0 && height > 0 && limit > 0) { "Ukuran foto tidak valid." }
    var sample = 1
    while (maxOf(width, height).toLong() > limit.toLong() * sample) sample *= 2
    return sample
}
