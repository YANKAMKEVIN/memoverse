package com.kevin.memoverse.data.local

import com.kevin.memoverse.resources.Res

class ResourceVerseBankDataSource : VerseBankDataSource {
    override suspend fun readVerseBankJson(): String =
        Res.readBytes(VERSE_BANK_PATH).decodeToString()

    private companion object {
        const val VERSE_BANK_PATH = "files/verses.json"
    }
}
