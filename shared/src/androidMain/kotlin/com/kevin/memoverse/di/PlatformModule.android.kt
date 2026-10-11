package com.kevin.memoverse.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.data.notification.LocalNotifier
import com.kevin.memoverse.data.notification.WorkManagerNotifier
import com.kevin.memoverse.data.speech.AndroidSpeechRecognitionService
import com.kevin.memoverse.domain.speech.SpeechRecognitionService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(MemoverseDatabase.Schema, androidContext(), DATABASE_NAME) }
    single<LocalNotifier> { WorkManagerNotifier(androidContext()) }
    single<SpeechRecognitionService> { AndroidSpeechRecognitionService(androidContext()) }
}
