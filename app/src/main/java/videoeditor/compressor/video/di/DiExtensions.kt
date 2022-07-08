@file:JvmName("Injector")

package videoeditor.compressor.video.di

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import videoeditor.compressor.video.MApplication

val ViewModel.injector: AppComponent
    get() = MApplication.instance.injector
val Context.injector: AppComponent
    get() = (applicationContext as MApplication).component

val Fragment.injector: AppComponent
    get() = (context!!.applicationContext as MApplication).component

val android.app.Fragment.injector: AppComponent
    get() = (activity!!.applicationContext as MApplication).component

