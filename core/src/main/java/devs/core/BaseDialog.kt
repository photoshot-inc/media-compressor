package devs.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding


abstract class BaseDialog<V : ViewBinding>(private val inflater: Inflate<V>) : DialogFragment() {
    protected lateinit var binding: V
    var idTag: String = ""

    open fun onBackPressed(params: OnBackPressedCallback) {
        params.isEnabled = false
        activity?.onBackPressedDispatcher?.onBackPressed()
        params.isEnabled = true

    }

    override fun onCreateView(inflaterp: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = inflater.invoke(inflaterp, container, false)
        return binding.root
    }

    abstract fun initView()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed(this)
        }

    }

    fun registerBackPress() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressCallback)
    }

    protected inline fun <reified T> grabListener(): T? {
        var par = parentFragment
        while (par != null) {
            if (par is T) {
                return par
            }
            par = par.parentFragment
        }
        if (activity is T) return (activity as T)
        return null
    }
}