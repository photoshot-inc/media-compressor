package com.inspiration.imagepicker.presentation.fragments

import android.os.Bundle
import com.inspiration.imagepicker.R
import com.inspiration.imagepicker.databinding.FragmentImagePreviewBinding
import devs.core.BaseFragment
import devs.core.utils.load
import java.lang.Exception

class ImagePreviewFragment :
    BaseFragment<FragmentImagePreviewBinding>(FragmentImagePreviewBinding::inflate) {
    companion object {
        fun newInstance(uri: String): ImagePreviewFragment {
            val args = Bundle()
            args.putString("extra.uri", uri)
            val fragment = ImagePreviewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView() {
        val args = arguments?.getString("extra.uri")
        try {
            binding.imageView.load(args ?: "https::ssdsjafua", R.drawable.placeholder_img)
        } catch (ex: Exception) {
        }
        binding.closeBtn.setOnClickListener {

        }
    }
}