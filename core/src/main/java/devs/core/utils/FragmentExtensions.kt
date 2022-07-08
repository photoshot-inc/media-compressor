package devs.core.utils

import android.animation.Animator
import android.view.View
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import devs.core.R

fun Fragment.replaceFragment(
    fragment: Fragment,
    containerId: Int,
    addToBackStack: Boolean = true,
    @AnimRes enterAnim: Int = R.anim.slide_up_enter,
    @AnimRes exitAnim: Int = R.anim.slide_down_enter,
    @AnimRes popEnter: Int = R.anim.slide_up_enter,
    @AnimRes popExit: Int = R.anim.slide_down_enter
) = safeRun {
    childFragmentManager.beginTransaction()
        .setCustomAnimations(enterAnim, exitAnim, popEnter, popExit)
        .replace(containerId, fragment)
//        .setReorderingAllowed(true)
        .let {
            if (addToBackStack) it.addToBackStack(fragment.javaClass.simpleName)
            it.commit()
        }
}

fun Fragment.dismiss() = safeRun {
    parentFragmentManager.beginTransaction()
        .remove(this)
        .commit()
}

fun Fragment.dismissWithAnimation(xTranslate: Float = 0f, yTranslate: Float = 0f, endAlpha: Float = 0f, duration: Long = 300) = safeRun {
    val listener = object : Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator?) {

        }

        override fun onAnimationEnd(p0: Animator?) {
            safeRun {
                parentFragmentManager.fragments.firstOrNull { it == this@dismissWithAnimation }?.let {
                    parentFragmentManager.popBackStack()
                } ?: parentFragmentManager.beginTransaction()
                    .remove(this@dismissWithAnimation)
                    .commit()
            }
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationRepeat(p0: Animator?) {

        }

    }
    if (view == null) listener.onAnimationEnd(null)
    else {
        view!!.animate()
            .translationX(view!!.width * xTranslate)
            .translationY(view!!.height * yTranslate)
            .alpha(endAlpha)
            .setListener(listener)
            .setDuration(duration)
            .start()
    }

}

fun View.animateView(xTranslate: Float = 0f, yTranslate: Float = 0f, endAlpha: Float = 0f, duration: Long = 300, onEnd: (() -> Unit)? = null) =
    safeRun {
        val listener = object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                onEnd?.invoke()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }

        }

        this.animate()
            .translationX(width * xTranslate)
            .translationY(height * yTranslate)
            .alpha(endAlpha)
            .setListener(listener)
            .setDuration(duration)
            .start()

    }

fun Fragment.popCurrentFromBackStack() = safeRun {
    parentFragmentManager.popBackStack()
}