package devs.core

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding


typealias ActivityInflater<T> = (LayoutInflater) -> T

abstract class BaseActivity<V : ViewBinding>(private val inflate: ActivityInflater<V>) :
    AppCompatActivity() {
    private var _binding: V? = null
    val binding: V get() = _binding!!
    var idTag: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflate.invoke(layoutInflater)
        setContentView(binding.root)
        initView(savedInstanceState)
    }


    abstract fun initView(savedInstanceState: Bundle?)


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}