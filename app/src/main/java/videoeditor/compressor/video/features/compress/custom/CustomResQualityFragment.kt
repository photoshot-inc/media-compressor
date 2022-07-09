package videoeditor.compressor.video.features.compress.custom

import android.content.Context
import androidx.core.os.bundleOf
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import devs.core.BaseObservableFragment
import videoeditor.compressor.video.databinding.FragmentCustomBinding
import videoeditor.compressor.video.features.compress.IntentKeys
import videoeditor.compressor.video.features.compress.resolution.ConfigurationUpdateLister
import videoeditor.compressor.video.models.VideoInfo
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

    private fun updateResolutionTxt(progress: Float) {
        binding.resolution.text =
            "${(videoInfo.width * progress).toInt()}x${(videoInfo.height * progress).toInt()}"
    }

    private fun updateBitrateTxt(progress: Float) {
        binding.bitrate.text = "${(videoInfo.bitrate * progress).toInt()} kb/s"
    }

    lateinit var videoInfo: VideoInfo
    var resolution: Int = 0
    override fun initView() {
        videoInfo = arguments?.get(IntentKeys.EXTRA_MODEL.str) as VideoInfo
        resolution = min(videoInfo.height, videoInfo.width)
        updateResolutionTxt(0.75f)
        updateBitrateTxt(0.75f)
        binding.resolutionSeekbar.setProgress(0.75f)
        binding.bitrateSeekbar.setProgress(0.75f)
        binding.resolutionSeekbar.setOnRangeChangedListener(object : SeekListener() {
            override fun onMove(progress: Float) {
                updateResolutionTxt(progress)
            }

            override fun onStop(progress: Float) {
                notify {
                    it.onResolutionChange((resolution * progress).toInt())
                }
            }
        })
        binding.bitrateSeekbar.setOnRangeChangedListener(object : SeekListener() {
            override fun onMove(progress: Float) {
                updateBitrateTxt(progress)
            }

            override fun onStop(progress: Float) {
                notify {
                    it.onBitrateChange((videoInfo.bitrate * progress).toInt())
                }
            }
        })
        binding.compressBtn.setOnClickListener {
            val resProgress = binding.resolutionSeekbar.leftSeekBar.progress
            val bitProgress = binding.resolutionSeekbar.leftSeekBar.progress
            notify {
                it.onStartCompression(
                    (videoInfo.width * resProgress).toInt(),
                    (videoInfo.height * resProgress).toInt(),
                    (videoInfo.bitrate * bitProgress).toInt()
                )
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        grabListener<ConfigurationUpdateLister> { registerObserver(it) }
    }
}

abstract class SeekListener : OnRangeChangedListener {
    open fun onStart(progress: Float) {}
    open fun onStop(progress: Float) {}
    open fun onMove(progress: Float) {}
    override fun onStartTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
        onStart(view.leftSeekBar.progress)
    }

    override fun onRangeChanged(
        view: RangeSeekBar?,
        leftValue: Float,
        rightValue: Float,
        isFromUser: Boolean
    ) {
        onMove(leftValue)
    }

    override fun onStopTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
        onStop(view.leftSeekBar.progress)
    }

}