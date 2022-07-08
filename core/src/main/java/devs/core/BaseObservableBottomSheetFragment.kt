package devs.core


import androidx.viewbinding.ViewBinding


abstract class BaseObservableBottomSheetFragment<B : ViewBinding, ListenerType>(private val inflater: Inflate<B>) : BaseBottomSheetFragment<B>(inflater) {
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
    protected inline fun <reified T> grabListener(noinline onFind: ((it: T) -> Unit)? = null): T? {
        var par = parentFragment
        while (par != null) {
            if (par is T) {
                onFind?.invoke(par)
                return par
            }
            par = par.parentFragment
        }
        if (activity is T){
            onFind?.invoke(activity as T)
            return (activity as T)
        }
        return null
    }

}