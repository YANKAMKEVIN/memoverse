package com.kevin.memoverse.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.data.notification.LocalNotifier
import com.kevin.memoverse.data.notification.UserNotificationsNotifier
import com.kevin.memoverse.data.speech.IosSpeechRecognitionService
import com.kevin.memoverse.domain.speech.SpeechRecognitionService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(MemoverseDatabase.Schema, DATABASE_NAME) }
    single<LocalNotifier> { UserNotificationsNotifier() }
    single<SpeechRecognitionService> { IosSpeechRecognitionService() }
}
