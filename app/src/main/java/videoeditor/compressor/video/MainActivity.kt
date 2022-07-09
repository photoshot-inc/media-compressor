package videoeditor.compressor.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.inspiration.imagepicker.domain.models.FileModel
import com.inspiration.imagepicker.presentation.fragments.FilePicker
import com.inspiration.imagepicker.presentation.fragments.FilePickerCallback
import devs.core.BaseActivity
import devs.core.utils.replaceFragment
import devs.core.utils.withStoragePermission
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import videoeditor.compressor.video.databinding.ActivityMainBinding
import videoeditor.compressor.video.events.ActivityEvents
import videoeditor.compressor.video.events.registerEventBus
import videoeditor.compressor.video.events.unregisterEventBus
import videoeditor.compressor.video.features.compress.CompressOptionsFragment
import videoeditor.compressor.video.features.home.HomeFragment
import videoeditor.compressor.video.features.player.VideoPlayerFragment
import videoeditor.compressor.video.features.progress.ProgressFragment


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    HomeFragment.Listener, FilePickerCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            showScreen(HomeFragment())
        }
    }

    override fun onCompressClicked() {
        withStoragePermission {
            showScreen(FilePicker.getVideoPicker(120))
        }
    }

    override fun onOutputClicked() {
        TODO("Not yet implemented")
    }

    override fun onImageClicked(image: FileModel, requestCode: Int) {
        showScreen(CompressOptionsFragment.newInstance(image))
    }

    private fun showScreen(fragment: Fragment) {
        replaceFragment(fragment, binding.frameContainer.id)
    }

    override fun onStart() {
        super.onStart()
        registerEventBus()
    }

    override fun onStop() {
        super.onStop()
        unregisterEventBus()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            finish()
            return
        }
        super.onBackPressed()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityEvents) {
        when (event) {
            ActivityEvents.ShowProcessingScreenEvent -> showScreen(ProgressFragment())
            is ActivityEvents.PlayVideoEvent -> showScreen(VideoPlayerFragment.newInstance(event.path))
            is ActivityEvents.ShareFile -> TODO()
            is ActivityEvents.ShowInterstitial -> TODO()
            ActivityEvents.ShowHomeScreen -> {
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                showScreen(HomeFragment())
            }
            is ActivityEvents.ShowScreen -> {
                showScreen(event.fragment)
            }
        }
    }
}