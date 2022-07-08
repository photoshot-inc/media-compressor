package devs.core.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

private val scaleShow = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
    duration = 500
    fillAfter = true
}
private val scaleHide = ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
    duration = 500
    fillAfter = true
}

fun View.scaleUpAndShow() {
    startAnimation(scaleShow)
}

fun View.scaleDownAndHide() {
    startAnimation(scaleHide)
}
fun Boolean.asVisibility(): Int {
    return if(this) View.VISIBLE
    else View.GONE
}