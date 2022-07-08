package devs.core.utils

import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import devs.core.R
fun FragmentActivity.replaceFragment(
    fragment: Fragment,
    containerId: Int,
    addToBackStack: Boolean = true,
    @AnimRes enterAnim: Int = R.anim.slide_up_enter,
    @AnimRes exitAnim: Int = R.anim.slide_down_enter,
    @AnimRes popEnter: Int = 0,
    @AnimRes popExit: Int = 0
) = safeRun {
    supportFragmentManager.beginTransaction()
        .setCustomAnimations(enterAnim, exitAnim, popEnter, popExit)
        .replace(containerId, fragment)
        .let {
            if (addToBackStack) it.addToBackStack(fragment.javaClass.simpleName)
            it.commit()
        }
}

fun FragmentActivity.addFragment(
    fragment: Fragment,
    containerId: Int,
    addToBackStack: Boolean = true,
    @AnimRes enterAnim: Int = R.anim.slide_up_enter,
    @AnimRes exitAnim: Int = R.anim.slide_down_enter,
    @AnimRes popEnter: Int = 0,
    @AnimRes popExit: Int = 0
) = safeRun {
    supportFragmentManager.beginTransaction()
        .setCustomAnimations(enterAnim, exitAnim, popEnter, popExit)
        .add(containerId, fragment)
        .let {
            if (addToBackStack) it.addToBackStack(fragment.javaClass.simpleName)
            it.commit()
        }
}