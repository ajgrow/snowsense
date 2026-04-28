package in2000.prosjekt.in2000project

import android.app.Application
import in2000.prosjekt.in2000project.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class SnowSenseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // This is where Koin initializes for the whole app
        startKoin {
            androidContext(this@SnowSenseApplication)
            modules(appModule)
        }
    }
}