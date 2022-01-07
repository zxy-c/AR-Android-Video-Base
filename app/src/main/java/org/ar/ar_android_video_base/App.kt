package org.ar.ar_android_video_base

import android.app.Application
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import kotlin.properties.Delegates

class App:Application() {

    val userId :String =((Math.random()*9+1)*100000L).toInt().toString()

    companion object{
        var app: App by Delegates.notNull()
    }

    override fun onCreate() {
        Fuel.trace = true
        FuelManager.instance.basePath = BuildConfig.SERVER_URL
        super.onCreate()
        app = this
    }
}