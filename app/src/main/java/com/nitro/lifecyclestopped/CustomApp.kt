package com.nitro.lifecyclestopped

import android.app.Application
import logcat.AndroidLogcatLogger

/**
 * Created by t.coulange on 27/09/2021.
 */
class CustomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidLogcatLogger.installOnDebuggableApp(this)
    }
}