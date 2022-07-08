package videoeditor.compressor.video.events

import org.greenrobot.eventbus.EventBus

sealed class ActivityEvents {
    object ShowProcessingScreenEvent : ActivityEvents()
    data class PlayVideoEvent(val path: String) : ActivityEvents()
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