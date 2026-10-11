package com.kevin.memoverse.data.local

interface VerseBankDataSource {
    suspend fun readVerseBankJson(): String
}
