package net.retiolus.osm2gmaps

import android.app.Application
import com.google.android.material.color.DynamicColors

class Osm2gmapsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}