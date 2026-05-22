package com.petal

import android.app.Application
import com.petal.di.AppContainer

class PetalApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
