package videoeditor.compressor.video.features.progress

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.view.isVisible
import com.devs.adloader.AdProvider.loadNativeAd
import devs.core.BaseObservableFragment
import devs.core.utils.load
import devs.core.utils.safeRun
import videoeditor.compressor.video.Utils
import videoeditor.compressor.video.Utils.readableSize
import videoeditor.compressor.video.databinding.FragmentProgressBinding
import videoeditor.compressor.video.events.ActivityEvents
import videoeditor.compressor.video.events.broadcast
import videoeditor.compressor.video.service.CompressionService
import videoeditor.compressor.video.service.ServiceState
import java.io.File

class ProgressFragment : BaseObservableFragment<FragmentProgressBinding,
        ProgressFragment.Listener>(FragmentProgressBinding::inflate), ServiceConnection {
    interface Listener {

    }

    var mService: CompressionService? = null
    override fun initView() {
        context?.let { CompressionService.startService(it) }
        loadNativeAd(binding.adContainer)
        binding.cancelBtn.setOnClickListener {
            mService?.cancel()
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(context, CompressionService::class.java).also { intent ->
            activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.unbindService(this)
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        safeRun {
            val binder = p1 as? CompressionService.MBinder
            mService = binder?.getService()
            observeService(mService)
        }
    }

    private fun observeService(service: CompressionService?) {
        if (service == null) return
        service.progress.observe(viewLifecycleOwner) {
            binding.progress.isIndeterminate = it < 0
            binding.progress.progress = it
        }
        service.state.observe(viewLifecycleOwner) { state ->
            binding.successView.isVisible = state is ServiceState.Success
            binding.progressView.isVisible =
                state is ServiceState.Processing || state is ServiceState.Started
            when (state) {
                is ServiceState.Success -> {
                    binding.outputVideoThumb.load(state.data.outputPath)
                    binding.outputVideoThumb.setOnClickListener {
                        ActivityEvents.PlayVideoEvent(state.data.outputPath).broadcast()
                    }
                    val outDimen = "${state.data.outputWidth}x${state.data.outputHeight}"
                    val outSize =
                        "${(File(state.data.outputPath).length()).readableSize()}"
                    val inDimen =
                        "${state.data.inputVideoInfo.width}x${state.data.inputVideoInfo.height}"
                    val inSize = "${state.data.inputVideoInfo.size.readableSize()}"
                    binding.inputSize.text = "$inDimen\n$inSize"
                    binding.outputSize.text = "$outDimen\n$outSize"
                }
                else -> {}
            }
            updateTitle(state)
        }
    }

    private fun updateTitle(state: ServiceState) {
        val status = when (state) {
            is ServiceState.Failed -> "Failed"
            ServiceState.Idle -> "Not Started"
            ServiceState.Processing -> "Processing"
            ServiceState.Started -> "Started"
            is ServiceState.Success -> "Successful"
            ServiceState.Cancelled -> "Cancelled"
        }
        binding.status.text = status
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        mService = null
    }
}