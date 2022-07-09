package videoeditor.compressor.video.features.compress

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.devs.adloader.AdProvider.loadBannerAd
import com.google.android.material.tabs.TabLayoutMediator
import com.inspiration.imagepicker.domain.models.FileModel
import devs.core.AbstractAdapter
import devs.core.BaseObservableFragment
import devs.core.utils.load
import videoeditor.compressor.video.R
import videoeditor.compressor.video.Utils.readableSize
import videoeditor.compressor.video.Utils.toFormattedDuration
import videoeditor.compressor.video.databinding.FragmentCompressOptionsBinding
import videoeditor.compressor.video.databinding.LayoutThumbImageBinding
import videoeditor.compressor.video.events.ActivityEvents
import videoeditor.compressor.video.events.broadcast
import videoeditor.compressor.video.features.compress.custom.CustomResQualityFragment
import videoeditor.compressor.video.features.compress.resolution.ConfigurationUpdateLister
import videoeditor.compressor.video.features.compress.resolution.ResolutionSelectionFragment
import videoeditor.compressor.video.models.VideoInfo

interface CompressScreenCallbacks {
    fun startProcessing()
}

class CompressOptionsFragment :
    BaseObservableFragment<FragmentCompressOptionsBinding, CompressScreenCallbacks>(
        FragmentCompressOptionsBinding::inflate
    ), ConfigurationUpdateLister {
    companion object {
        fun newInstance(model: FileModel): CompressOptionsFragment {
            val fragment = CompressOptionsFragment()
            fragment.arguments = bundleOf(IntentKeys.EXTRA_MODEL.str to model)
            return fragment
        }
    }

    private val viewModel by viewModels<CompressOptionViewModel>()
    private lateinit var adapter: FragmentStateAdapter

    private lateinit var pagerAdapter: AbstractAdapter<VideoInfo, LayoutThumbImageBinding>

    override fun initView() {
        pagerAdapter = object :
            AbstractAdapter<VideoInfo, LayoutThumbImageBinding>(LayoutThumbImageBinding::inflate) {
            override fun bind(
                itemBinding: LayoutThumbImageBinding,
                item: VideoInfo,
                position: Int
            ) {
                itemBinding.thumb.load(item.uri)
                itemBinding.size.text = item.size.readableSize()
                itemBinding.duration.text = item.duration.toFormattedDuration()
                itemBinding.playBtn.setOnClickListener {
                    ActivityEvents.PlayVideoEvent(item.uri).broadcast()
                }
            }
        }
        adapter = object : FragmentStateAdapter(this@CompressOptionsFragment) {
            override fun getItemCount(): Int {
                return if (viewModel.videoInfo.value == null) 0 else 3
            }

            override fun createFragment(position: Int): Fragment {
                val info = viewModel.videoInfo.value ?: return Fragment()
                Log.d("TAGTAGTAGTAG", "createFragment: $position")
                return when (position) {
                    0 -> ResolutionSelectionFragment.newInstance(info)
                    1 -> ResolutionSelectionFragment.newInstance(info)
                    else -> CustomResQualityFragment.newInstance(info)
                }
            }

        }

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
            pagerAdapter.setItems(it)
        }
        viewModel.videoInfo.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
            binding.toolbar.title = it?.title
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

    override fun onStartCompression(width: Int, height: Int, bitrate: Long) {
        viewModel.compressVideo(width, height, bitrate)
    }
}