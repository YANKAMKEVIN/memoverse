package com.kevin.memoverse.di

import com.kevin.memoverse.data.local.ResourceVerseBankDataSource
import com.kevin.memoverse.data.local.VerseBankDataSource
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.data.notification.ReviewNotificationScheduler
import com.kevin.memoverse.data.repository.LearningRepositoryImpl
import com.kevin.memoverse.data.repository.VerseBankRepositoryImpl
import com.kevin.memoverse.data.system.SystemTimeProvider
import com.kevin.memoverse.domain.repository.LearningRepository
import com.kevin.memoverse.domain.repository.NotificationScheduler
import com.kevin.memoverse.domain.repository.TimeProvider
import com.kevin.memoverse.domain.repository.VerseBankRepository
import org.koin.dsl.module

internal const val DATABASE_NAME = "memoverse.db"

val dataModule = module {
    single<VerseBankDataSource> { ResourceVerseBankDataSource() }
    single<VerseBankRepository> { VerseBankRepositoryImpl(get()) }
    single { MemoverseDatabase(get()) }
    single<LearningRepository> { LearningRepositoryImpl(get()) }
    single<TimeProvider> { SystemTimeProvider() }
    single<NotificationScheduler> { ReviewNotificationScheduler(get()) }
}
