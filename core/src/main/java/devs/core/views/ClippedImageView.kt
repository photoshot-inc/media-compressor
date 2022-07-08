package devs.core.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class ClippedImageView(context: Context, attributeSet: AttributeSet?) :
    AppCompatImageView(context, attributeSet) {
    init {
        clipToOutline = true
    }
}