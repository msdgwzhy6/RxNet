package com.dhy.retrofitrxtest

import android.app.Application
import android.content.Context
import com.dhy.retrofitrxutil.ObserverX
import com.dhy.retrofitrxutil.sample.SampleErrorHandler

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ObserverX.setProgressGenerator(ProgressGenerator())
        ObserverX.setErrorHandler(SampleErrorHandler())
    }

    companion object {
        fun getInstance(context: Context): App {
            return context.applicationContext as App
        }
    }
}