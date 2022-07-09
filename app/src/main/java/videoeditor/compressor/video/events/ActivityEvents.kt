package videoeditor.compressor.video.events

import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus

sealed class ActivityEvents {
    object ShowProcessingScreenEvent : ActivityEvents()
    object ShowHomeScreen : ActivityEvents()
    object ShowInterstitial :ActivityEvents()
    data class ShowScreen(val fragment: Fragment, val clearAll: Boolean = false) : ActivityEvents()
    data class PlayVideoEvent(val path: String) : ActivityEvents()
    data class ShareFile(val uri:String):ActivityEvents()

}

private val eventbus = EventBus.getDefault()
fun ActivityEvents.broadcast() {
    eventbus.post(this)
}

fun Any.registerEventBus() {
    if (!eventbus.isRegistered(this)) eventbus.register(this)
}

fun Any.unregisterEventBus() {
    if (eventbus.isRegistered(this)) eventbus.unregister(this)
}