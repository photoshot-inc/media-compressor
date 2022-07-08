package videoeditor.compressor.video.features.home

import android.content.Context
import com.devs.adloader.AdProvider.loadBannerAd
import devs.core.BaseObservableFragment
import videoeditor.compressor.video.databinding.FragmentHomeBinding

class HomeFragment : BaseObservableFragment<FragmentHomeBinding,HomeFragment.Listener>(FragmentHomeBinding::inflate) {
    interface Listener{
        fun onCompressClicked()
        fun onOutputClicked()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        grabListener<Listener> {
            registerObserver(it)
        }
    }
    override fun initView() {
        binding.openPhoto.setOnClickListener {
            notify { listener ->  listener.onCompressClicked() }
        }
        loadBannerAd(binding.adContainer)
    }
}