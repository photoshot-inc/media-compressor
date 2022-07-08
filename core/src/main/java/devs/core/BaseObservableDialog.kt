package devs.core

import androidx.viewbinding.ViewBinding


abstract class BaseObservableDialog<V : ViewBinding, ListenerType>(inflater: Inflate<V>) : BaseDialog<V>(inflater) {

    private val listeners = hashSetOf<ListenerType>()

    fun notify(data: (ListenerType) -> Unit) {
        listeners.forEach(data)
    }

    protected fun registerObserver(it: ListenerType) {
        if (listeners.contains(it).not()) listeners.add(it)
    }

    protected fun unRegisterObserver(it: ListenerType) {
        if (listeners.contains(it)) listeners.remove(it)
    }
}