package videoeditor.compressor.video.features.compress.custom

import android.content.Context
import android.util.Log
import android.widget.SeekBar
import androidx.core.os.bundleOf
import devs.core.AbstractAdapter
import devs.core.BaseObservableFragment
import videoeditor.compressor.video.databinding.FragmentCustomBinding
import videoeditor.compressor.video.databinding.FragmentResolutionBinding
import videoeditor.compressor.video.databinding.ItemResolutionListBinding
import videoeditor.compressor.video.features.compress.IntentKeys
import videoeditor.compressor.video.features.compress.resolution.ConfigurationUpdateLister
import videoeditor.compressor.video.models.VideoInfo
import java.io.Serializable
import kotlin.math.min

class CustomResQualityFragment :
    BaseObservableFragment<FragmentCustomBinding, ConfigurationUpdateLister>(FragmentCustomBinding::inflate) {
    companion object {
        fun newInstance(info: VideoInfo): CustomResQualityFragment {
            val fragment = CustomResQualityFragment()
            fragment.arguments = bundleOf(IntentKeys.EXTRA_MODEL.str to info)
            return fragment
        }
    }

    lateinit var videoInfo: VideoInfo
    var resolution: Int = 0
    override fun initView() {
        videoInfo = arguments?.get(IntentKeys.EXTRA_MODEL.str) as VideoInfo
        resolution = min(videoInfo.height, videoInfo.width)
        binding.resolutionSeekbar.setOnSeekBarChangeListener(object : SeekListener() {
            override fun onMove(progress: Float) {
                binding.resolution.text =
                    "${(videoInfo.width * progress).toInt()}x${(videoInfo.height * progress).toInt()}"
            }

            override fun onStop(progress: Float) {
                notify {
                    it.onResolutionChange((resolution * progress).toInt())
                }
            }
        })
        binding.bitrateSeekbar.setOnSeekBarChangeListener(object : SeekListener() {

            override fun onMove(progress: Float) {
                binding.bitrate.text = "${(videoInfo.bitrate * progress).toInt()} kb/s"
            }

            override fun onStop(progress: Float) {
                notify {
                    it.onBitrateChange((videoInfo.bitrate * progress).toInt())
                }
            }
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        grabListener<ConfigurationUpdateLister> { registerObserver(it) }
    }
}

abstract class SeekListener : SeekBar.OnSeekBarChangeListener {
    open fun onStart(progress: Float) {}
    open fun onStop(progress: Float) {}
    open fun onMove(progress: Float) {}
    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        onMove(p1 / 100f)
    }

    override fun onStartTrackingTouch(p0: SeekBar) {
        onStart(p0.progress / 100f)
    }

    override fun onStopTrackingTouch(p0: SeekBar) {
        onStop(p0.progress / 100f)
    }
}