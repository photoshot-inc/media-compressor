package videoeditor.compressor.video.features.compress.resolution

import android.content.Context
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import devs.core.AbstractAdapter
import devs.core.BaseObservableFragment
import videoeditor.compressor.video.Utils.readableSize
import videoeditor.compressor.video.databinding.FragmentResolutionBinding
import videoeditor.compressor.video.databinding.ItemResolutionListBinding
import videoeditor.compressor.video.features.compress.IntentKeys
import videoeditor.compressor.video.models.VideoInfo
import kotlin.math.min

interface ConfigurationUpdateLister {
    fun onResolutionChange(resolution: Int)
    fun onBitrateChange(bitrate: Int)
    fun onStartCompression(width: Int, height: Int, bitrate: Long)
}

data class ResOptionModel(
    val percentage: Float,
    val resolution: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val standardRes: String? = null
)

class ResolutionSelectionFragment :
    BaseObservableFragment<FragmentResolutionBinding, ConfigurationUpdateLister>(
        FragmentResolutionBinding::inflate
    ) {
    companion object {
        fun newInstance(info: VideoInfo): ResolutionSelectionFragment {
            val fragment = ResolutionSelectionFragment()
            fragment.arguments = bundleOf(IntentKeys.EXTRA_MODEL.str to info)
            return fragment
        }
    }

    private val standardResMap =
        mapOf(
            3840 to "4k",
            2160 to "2k",
            1920 to "1920P",
            1080 to "1080P",
            720 to "720P",
            640 to "640p",
            560 to "560P",
            480 to "480P",
            360 to "360P",
            240 to "240P",
            144 to "144P"
        )
    private val adapter by lazy {
        object :
            AbstractAdapter<ResOptionModel, ItemResolutionListBinding>(ItemResolutionListBinding::inflate) {
            override fun bind(
                itemBinding: ItemResolutionListBinding,
                item: ResOptionModel,
                position: Int
            ) {
                itemBinding.percentage.text = "${(item.percentage * 100).toInt()}%"
                itemBinding.resolution.text = item.resolution
                itemBinding.size.text = item.size.readableSize()
                itemBinding.resMarker.isVisible = item.standardRes != null
                itemBinding.resMarker.text = item.standardRes
                itemBinding.main.setOnClickListener {
                    notify {
                        it.onStartCompression(
                            item.width,
                            item.height,
                            (videoInfo.bitrate * item.percentage).toLong()
                        )
                    }
                }
            }
        }
    }
    lateinit var videoInfo: VideoInfo
    var width: Int = 0

    var height: Int = 0
    override fun initView() {
        videoInfo = arguments?.get(IntentKeys.EXTRA_MODEL.str) as VideoInfo
        width = videoInfo.width
        height = videoInfo.height
        binding.recyclerView.adapter = adapter
        adapter.setItems(getOptions())
    }

    private fun getOptions(): List<ResOptionModel> {
        val items = mutableListOf<ResOptionModel>()
        for (i in 95 downTo 5 step 5) {
            val factor = i / 100f
            val width = (videoInfo.width * factor).toInt()
            val height = (videoInfo.height * factor).toInt()
            val res = min(width, height)
            val option = ResOptionModel(
                factor,
                "${width}x$height", width, height,
                (videoInfo.size * factor).toLong(),
                standardResMap[res]
            );
            items.add(option)
        }

        val videoRes = min(width, height)
        standardResMap.forEach {
            if (it.key > videoRes) return@forEach
            var factor = 0f
            var width = 0
            var height = 0
            if (videoInfo.width < videoInfo.height) {
                width = it.key
                factor = it.key / videoInfo.width.toFloat()
                height = (videoInfo.height * factor).toInt()
            } else {
                height = it.key
                factor = it.key / videoInfo.height.toFloat()
                width = (videoInfo.width * factor).toInt()
            }
            if (factor == 1f) return@forEach
            val option = ResOptionModel(
                factor,
                "${width}x$height", width, height,
                (videoInfo.size * factor).toLong(),
                it.value
            )
            items.add(option)
        }
        return items.filter { it.width >= 144 || it.height >= 144 }
            .sortedByDescending { it.percentage }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        grabListener<ConfigurationUpdateLister> { registerObserver(it) }
    }
}