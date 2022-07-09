package videoeditor.compressor.video.features.player

import android.net.Uri
import androidx.core.os.bundleOf
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import devs.core.BaseFragment
import devs.core.utils.safeRun
import videoeditor.compressor.video.databinding.FragmentVideoPlayerBinding
import videoeditor.compressor.video.features.compress.IntentKeys

class VideoPlayerFragment :
    BaseFragment<FragmentVideoPlayerBinding>(FragmentVideoPlayerBinding::inflate) {
    companion object {
        fun newInstance(path: String): VideoPlayerFragment {
            val fragment = VideoPlayerFragment()
            fragment.arguments = bundleOf(IntentKeys.EXTRA_URI.str to path)
            return fragment
        }
    }

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(requireContext())
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000).build()
    }

    override fun initView() {
        binding.playerView.player = player
        binding.backBtn.setOnClickListener {
            activity?.onBackPressed()
        }
        safeRun {
            val path = arguments?.getString(IntentKeys.EXTRA_URI.str)
            val mediaItem = MediaItem.fromUri(Uri.parse(path))
            player.playWhenReady = true
            player.setMediaItem(mediaItem)
            player.prepare()
        }
    }

    override fun onDestroy() {
        player.stop()
        player.release()
        binding.playerView.player = null
        super.onDestroy()
    }
}