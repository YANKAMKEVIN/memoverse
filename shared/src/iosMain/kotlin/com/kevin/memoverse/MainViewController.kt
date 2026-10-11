package com.kevin.memoverse

import androidx.compose.ui.window.ComposeUIViewController
import com.kevin.memoverse.di.initKoin
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    if (KoinPlatform.getKoinOrNull() == null) initKoin()
    return ComposeUIViewController { App() }
}
