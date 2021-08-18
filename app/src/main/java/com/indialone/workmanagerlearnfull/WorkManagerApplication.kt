package com.indialone.workmanagerlearnfull

import android.app.Application
import android.util.Log.*
import androidx.viewbinding.BuildConfig
import androidx.work.Configuration
import timber.log.Timber

class WorkManagerApplication : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration {
        return if (BuildConfig.DEBUG) {
            Configuration.Builder()
                .setMinimumLoggingLevel(DEBUG)
                .build()
        } else {
            Configuration.Builder()
                .setMinimumLoggingLevel(ERROR)
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }


}