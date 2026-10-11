package com.kevin.memoverse

import android.app.Application
import com.kevin.memoverse.di.initKoin
import org.koin.android.ext.koin.androidContext

class MemoverseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@MemoverseApplication) }
    }
}
