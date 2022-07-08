package devs.core


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.Exception


abstract class BaseBottomSheetFragment<B : ViewBinding>(private val inflater: Inflate<B>) : BottomSheetDialogFragment() {
    /**
     * holder for the viewBinding instance for current fragment
     */
    lateinit var binding: B

    /**
     * a callback that will invoked when screen is dissmissed
     */
    var onCloseCallback: (() -> Unit)? = null

    /**
     * Method called after onViewCreated lifecycle method
     * proper place for initializing view with your data
     */
    abstract fun initView()
    override fun onCreateView(
        lInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = inflater.invoke(lInflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.bottom_sheet_base_theme
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

    }


    override fun onDestroy() {
        onCloseCallback?.invoke()
        super.onDestroy()
    }

    fun popSelf() {
        try {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitAllowingStateLoss()
        } catch (ex: Exception) {
            Log.d("TAGTAGTAGTAGTAG", "popSelf: " + ex)
        }
    }

    fun popFragment(tag: String) {
        childFragmentManager.popBackStack(
            tag,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    fun popFragment(fragment: Fragment) {
        childFragmentManager.popBackStack(
            fragment.javaClass.simpleName,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

}