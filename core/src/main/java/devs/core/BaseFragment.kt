package devs.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<V : ViewBinding>(private val inflater: Inflate<V>) : Fragment() {
    private var _binding: V? = null
    val binding: V get() = _binding!!
    var idTag: String = ""

    open fun onBackPressed(params: OnBackPressedCallback) {
        params.isEnabled = false
        activity?.onBackPressedDispatcher?.onBackPressed()
        params.isEnabled = true

    }

    override fun onCreateView(inflaterp: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflater.invoke(inflaterp, container, false)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}