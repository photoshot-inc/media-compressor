package com.inspiration.imagepicker.presentation.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.inspiration.imagepicker.FilePicker
import com.inspiration.imagepicker.R
import com.inspiration.imagepicker.databinding.FragmentImagePickerBinding
import com.inspiration.imagepicker.databinding.ItemBsImageGridBinding
import com.inspiration.imagepicker.databinding.ItemHorizFolderBinding
import com.inspiration.imagepicker.domain.models.FileModel
import com.inspiration.imagepicker.presentation.views.ItemOffsetDecoration
import devs.core.AbstractAdapter
import devs.core.BaseObservableFragment
import devs.core.ResultData
import devs.core.utils.asVisibility
import devs.core.utils.dismissWithAnimation
import devs.core.utils.load
import devs.core.utils.replaceFragment

class ImagePickerFragment :
    BaseObservableFragment<FragmentImagePickerBinding, FilePickerCallback>(
        FragmentImagePickerBinding::inflate
    ) {
    companion object {
        private const val TAG = "ImagePickerFragment"
        private const val KEY_REQUEST_CODE = "req.code.key"
        fun newInstance(type: Int, requestCode: Int, title: String? = null): ImagePickerFragment {
            val args = Bundle()
            args.putInt(KEY_REQUEST_CODE, requestCode)
            args.putInt(FilePicker.KEY_FILE_TYPE, type)
            args.putString(FilePicker.KEY_TITLE, title)
            val fragment = ImagePickerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var requestCode: Int = 0
    private val viewModels by viewModels<FilePickerViewModel> {
        SaveStateImagePickerVmFactory(
            arguments,
            this,
            context = activity?.application!!
        )
    }
    private val adapter =
        object :
            AbstractAdapter<FileModel, ItemBsImageGridBinding>(ItemBsImageGridBinding::inflate) {
            override fun bind(
                itemBinding: ItemBsImageGridBinding,
                item: FileModel,
                position: Int
            ) {
                itemBinding.image.load(item.uri, R.drawable.placeholder_img)
                itemBinding.image.setOnClickListener {
                    dismissWithAnimation(yTranslate = 0.2f)
                    notify { it.onImageClicked(item, requestCode) }
                }
                /* itemBinding.expandBtn.setOnClickListener {
                     binding.fragmentContainer.replaceFragment(
                         ImagePreviewFragment.newInstance(item.uri),
                         "TAG",
                     )
                 }*/
            }
        }

    private val folderListAdapter: AbstractAdapter<FolderModel, ItemHorizFolderBinding> by lazy {
        object :
            AbstractAdapter<FolderModel, ItemHorizFolderBinding>(ItemHorizFolderBinding::inflate) {
            override fun bind(
                itemBinding: ItemHorizFolderBinding,
                item: FolderModel,
                position: Int
            ) {
                itemBinding.title.text = item.title
                itemBinding.title.isSelected = item.selected
                itemBinding.title.setOnClickListener {
                    folderListAdapter.updateSelectionByPredicate {
                        it.selected = it.path == item.path
                        return@updateSelectionByPredicate true
                    }
                    viewModels.loadFiles(item.path)
                }
            }
        }
    }


    override fun initView() {
        requestCode = arguments?.getInt(KEY_REQUEST_CODE) ?: 0
        binding.imagesList.adapter = adapter
        binding.recyclerView.adapter = folderListAdapter
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
        viewModels.folders.observe(viewLifecycleOwner) {
            folderListAdapter.setItems(it)
        }
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        if (arguments?.getString(FilePicker.KEY_TITLE) != null) {
            binding.title.text = arguments?.getString(FilePicker.KEY_TITLE)
        }
        binding.title.setOnClickListener {

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        grabListener<FilePickerCallback> { registerObserver(it) }
    }
}


