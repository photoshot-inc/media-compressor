package devs.core

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel

open class BaseAndroidVm(private val application: Application) : ViewModel() {
    val context: Context get() = application.applicationContext
}