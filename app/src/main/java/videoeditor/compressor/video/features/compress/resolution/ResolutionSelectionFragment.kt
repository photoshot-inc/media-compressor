package videoeditor.compressor.video.features.compress.resolution

import android.content.Context
import devs.core.AbstractAdapter
import devs.core.BaseObservableFragment
import videoeditor.compressor.video.databinding.FragmentResolutionBinding
import videoeditor.compressor.video.databinding.ItemResolutionListBinding

interface ConfigurationUpdateLister {
    fun onResolutionChange(resolution: Int)
    fun onBitrateChange(bitrate: Int)
    fun onStartCompression(width: Int, height: Int, bitrate: Int)
}

class ResolutionSelectionFragment :
    BaseObservableFragment<FragmentResolutionBinding, ConfigurationUpdateLister>(
        FragmentResolutionBinding::inflate
    ) {

    private val adapter by lazy {
        object :
            AbstractAdapter<Int, ItemResolutionListBinding>(ItemResolutionListBinding::inflate) {
            override fun bind(itemBinding: ItemResolutionListBinding, item: Int, position: Int) {
                itemBinding.percentage.text = "$item%"
            }
        }
    }

    override fun initView() {
        binding.recyclerView.adapter = adapter
        adapter.setItems((1..95).filter { it % 5 == 0 }.map { 100 - it })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        grabListener<ConfigurationUpdateLister> { registerObserver(it) }
    }
}