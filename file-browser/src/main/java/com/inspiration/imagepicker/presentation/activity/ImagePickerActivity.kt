package com.inspiration.imagepicker.presentation.activity

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
 import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.inspiration.imagepicker.databinding.ActivityImagePickerBinding
import com.inspiration.imagepicker.presentation.fragments.ImagePickerBottomSheet
import devs.core.BaseActivity

class ImagePickerActivity :
    BaseActivity<ActivityImagePickerBinding>(ActivityImagePickerBinding::inflate) {
    companion object {
        private const val TAG = "ImagePickerActivityLog"
    }

    override fun initView(savedInstanceState: Bundle?) {
        initViewPager()
        initTabs()
        //qbinding.fragmentContainer.replaceFragment(ImagePickerFragment(), "TAG")
    }

    private fun initTabs() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Media"
                1 -> "Folders"
                else -> "Browse"
            }
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { setStyleForTab(it, Typeface.BOLD) }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let { setStyleForTab(it, Typeface.NORMAL) }
            }

            fun setStyleForTab(tab: TabLayout.Tab, style: Int) {
                tab.view.children.find { it is TextView }?.let { tv ->
                    (tv as TextView).post {
                        tv.setTypeface(null, style)
                    }
                }
            }
        })

    }
    private fun initViewPager() {
        binding.viewPager.run {
            adapter = object : FragmentStateAdapter(this@ImagePickerActivity) {
                override fun getItemCount(): Int = 3
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> ImagePickerBottomSheet()
                        1 -> ImagePickerBottomSheet()
                        else -> ImagePickerBottomSheet()
                    }
                }
            }
            offscreenPageLimit = 3
        }

    }
}
