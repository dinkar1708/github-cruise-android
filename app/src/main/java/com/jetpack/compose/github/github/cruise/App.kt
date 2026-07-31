package com.jetpack.compose.github.github.cruise

import android.app.Application
import android.content.Context
import com.jetpack.compose.github.github.cruise.BuildConfig.DEBUG
import com.jetpack.compose.github.github.cruise.data.datastore.LocaleDataStore
import com.jetpack.compose.github.github.cruise.utils.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/12.
 */
@HiltAndroidApp
class GithubCruiseApplication : Application() {

    @Inject
    lateinit var localeDataStore: LocaleDataStore

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun attachBaseContext(base: Context) {
        // Apply saved locale before app is created
        val locale = base.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
            .getString("locale", LocaleDataStore.LOCALE_SYSTEM_DEFAULT)
            ?: LocaleDataStore.LOCALE_SYSTEM_DEFAULT

        val context = LocaleManager.setLocale(base, locale)
        super.attachBaseContext(context)
    }
}