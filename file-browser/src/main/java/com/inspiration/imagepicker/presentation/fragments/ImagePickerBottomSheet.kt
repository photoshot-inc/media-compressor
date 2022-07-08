package com.inspiration.imagepicker.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.inspiration.imagepicker.R
import com.inspiration.imagepicker.databinding.FragmentImagePickerBsBinding
import com.inspiration.imagepicker.databinding.ItemBsImageGridBinding
import com.inspiration.imagepicker.domain.models.FileModel
import com.inspiration.imagepicker.presentation.views.ItemOffsetDecoration
import devs.core.*
import devs.core.utils.asVisibility
import devs.core.utils.load

class ImagePickerBottomSheet :
    BaseObservableBottomSheetFragment<FragmentImagePickerBsBinding,FilePickerCallback>(FragmentImagePickerBsBinding::inflate) {
    companion object {
        private const val TAG = "ImagePickerActivityLog"
        private const val KEY_REQUEST_CODE = "req.code.key"
        fun newInstance(type:Int,requestCode: Int): ImagePickerBottomSheet {
            val args = Bundle()
            args.putInt(KEY_REQUEST_CODE, requestCode)
            args.putInt(FilePicker.KEY_FILE_TYPE, type)
            val fragment = ImagePickerBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    var requestCode: Int = 0
    private val viewModels by viewModels<FilePickerViewModel>{SaveStateImagePickerVmFactory(arguments,this, context = activity?.application!!)}
    private val adapter =
        object :
            AbstractAdapter<FileModel, ItemBsImageGridBinding>(ItemBsImageGridBinding::inflate) {
            override fun bind(
                itemBinding: ItemBsImageGridBinding,
                item: FileModel,
                position: Int,
            ) {
                itemBinding.image.load(item.uri, R.drawable.placeholder_img)
                itemBinding.image.setOnClickListener {
                    notify{ it.onImageClicked(item, requestCode) }
                    dismissAllowingStateLoss()
                }
                /*itemBinding.expandBtn.setOnClickListener {
                    binding.fragmentContainer.replaceFragment(
                        ImagePreviewFragment.newInstance(item.uri),
                        "TAG",
                    )
                }*/
            }
        }


    var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialog
    }

    override fun initView() {
        requestCode = arguments?.getInt(KEY_REQUEST_CODE) ?: 0
        binding.imagesList.adapter = adapter
        binding.imagesList.addItemDecoration(ItemOffsetDecoration(3))
        viewModels.files.observe(this) {
            binding.loadingView.visibility = (it is ResultData.Loading).asVisibility()
            when (it) {
                is ResultData.Success -> adapter.setItems(it.data)
                is ResultData.Error -> {
                }
                ResultData.Loading -> {
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
      grabListener<FilePickerCallback> {registerObserver(it)}
    }
}


