package videoeditor.compressor.video.features.compress

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.devs.adloader.AdProvider.loadBannerAd
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import devs.core.AbstractAdapter
import devs.core.BaseObservableFragment
import devs.core.utils.load
import videoeditor.compressor.video.R
import videoeditor.compressor.video.databinding.FragmentCompressOptionsBinding
import videoeditor.compressor.video.databinding.ItemCompressOptionBinding
import videoeditor.compressor.video.databinding.LayoutThumbImageBinding
import videoeditor.compressor.video.features.compress.custom.CustomResQualityFragment
import videoeditor.compressor.video.features.compress.resolution.ConfigurationUpdateLister
import videoeditor.compressor.video.features.compress.resolution.ResolutionSelectionFragment

interface CompressScreenCallbacks {
    fun startProcessing()
}

class CompressOptionsFragment :
    BaseObservableFragment<FragmentCompressOptionsBinding, CompressScreenCallbacks>(
        FragmentCompressOptionsBinding::inflate
    ), ConfigurationUpdateLister {
    companion object {
        fun newInstance(uri: String): CompressOptionsFragment {
            val fragment = CompressOptionsFragment()
            fragment.arguments = bundleOf(IntentKeys.EXTRA_URI.str to uri)
            return fragment
        }
    }

    private val viewModel by viewModels<CompressOptionViewModel>()
    private val adapter by lazy {
        object : FragmentStateAdapter(childFragmentManager, this.lifecycle) {
            override fun getItemCount(): Int {
                return if (viewModel.videoInfo.value == null) 0 else 3
            }

            override fun createFragment(position: Int): Fragment {
                val info = viewModel.videoInfo.value ?: return Fragment()
                return when (position) {
                    0 -> ResolutionSelectionFragment()
                    1 -> ResolutionSelectionFragment()
                    else -> CustomResQualityFragment.newInstance(info)
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
        binding.thumbImage.adapter = pagerAdapter
        binding.viewPager.adapter = adapter
        val mediator = TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "High Quality"
                }
                1 -> {
                    tab.text = "Low quality"
                }
                2 -> {
                    tab.text = "Custom"
                }
            }
        }
        mediator.attach()
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
        viewModel.videoInfo.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }
        viewModel.events.observe(viewLifecycleOwner) {
            if (it.hasBeenHandled) return@observe
            when (it.what) {
                CompressScreenEventType.PROCESS_START_ERROR -> {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        grabListener<CompressScreenCallbacks> { registerObserver(it) }
    }

    override fun onResolutionChange(resolution: Int) {
        Log.d("TEST_LOG", "onChange:resolution $resolution")
    }

    override fun onBitrateChange(bitrate: Int) {
        Log.d("TEST_LOG", "onChange:bitrate $bitrate")
    }
}