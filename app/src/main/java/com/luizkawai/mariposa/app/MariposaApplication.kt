package com.luizkawai.mariposa.app

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath

@HiltAndroidApp
class MariposaApplication : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(IMAGE_CACHE_DIRECTORY).toOkioPath())
                    .maxSizePercent(DISK_CACHE_PERCENT)
                    .build()
            }
            .build()

    private companion object {
        const val IMAGE_CACHE_DIRECTORY = "character_images"
        const val MEMORY_CACHE_PERCENT = 0.25
        const val DISK_CACHE_PERCENT = 0.02
    }
}
