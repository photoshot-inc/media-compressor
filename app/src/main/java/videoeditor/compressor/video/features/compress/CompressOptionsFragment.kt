package videoeditor.compressor.video.features.compress

import android.content.Context
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.devs.adloader.AdProvider.loadBannerAd
import devs.core.AbstractAdapter
import devs.core.BaseObservableFragment
import devs.core.utils.load
import videoeditor.compressor.video.R
import videoeditor.compressor.video.databinding.FragmentCompressOptionsBinding
import videoeditor.compressor.video.databinding.ItemCompressOptionBinding
import videoeditor.compressor.video.databinding.LayoutThumbImageBinding

interface CompressScreenCallbacks {
    fun startProcessing()
}

class CompressOptionsFragment :
    BaseObservableFragment<FragmentCompressOptionsBinding, CompressScreenCallbacks>(
        FragmentCompressOptionsBinding::inflate
    ) {
    companion object {
        fun newInstance(uri: String): CompressOptionsFragment {
            val fragment = CompressOptionsFragment()
            fragment.arguments = bundleOf(IntentKeys.EXTRA_URI.str to uri)
            return fragment
        }
    }

    private val viewModel by viewModels<CompressOptionViewModel>()
    private val adapter: AbstractAdapter<CompressProfileModel, ItemCompressOptionBinding> by lazy {
        object :
            AbstractAdapter<CompressProfileModel, ItemCompressOptionBinding>(
                ItemCompressOptionBinding::inflate
            ) {
            override fun bind(
                itemBinding: ItemCompressOptionBinding,
                item: CompressProfileModel,
                position: Int
            ) {
                itemBinding.container.isSelected = item.selected
                itemBinding.title.text = item.title
                itemBinding.message.text = item.message
                itemBinding.container.setOnClickListener {
                    adapter.updateSelectionByPredicate {
                        it.selected = item.title == it.title
                        return@updateSelectionByPredicate true
                    }
                    binding.customProfile.isSelected = false
                    viewModel.onProfileUpdated(item)
                }
            }
        }
    }

    private val pagerAdapter by lazy {
        object :
            AbstractAdapter<String, LayoutThumbImageBinding>(LayoutThumbImageBinding::inflate) {
            override fun bind(itemBinding: LayoutThumbImageBinding, item: String, position: Int) {
                itemBinding.thumb.load(item)
            }
        }
    }

    override fun initView() {
        binding.profilesList.adapter = adapter
        binding.profilesList.itemAnimator = null
        binding.thumbImage.adapter = pagerAdapter
        binding.compressBtn.setOnClickListener {
            viewModel.compressVideo()
        }
        binding.customProfile.setOnClickListener {
            it.isSelected = true
            adapter.updateSelectionByPredicate { model ->
                model.selected = false
                return@updateSelectionByPredicate true
            }
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding.toolbar.title = "Configure"
        initObservers()
        loadBannerAd(binding.adContainer)
    }

    private fun initObservers() {
        viewModel.selectedFiles.observe(viewLifecycleOwner) { it ->
            pagerAdapter.setItems(it.map { uri -> uri.toString() })
        }

        viewModel.events.observe(viewLifecycleOwner) {
            if (it.hasBeenHandled) return@observe
            when (it.what) {
                CompressScreenEventType.PROCESS_START_ERROR -> {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.profiles.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        grabListener<CompressScreenCallbacks> { registerObserver(it) }
    }
}