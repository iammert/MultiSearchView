package com.iammert.library.ui.multisearchviewlib.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

/**
 * Runs [onAnimationEnd] when the animator finishes normally. Unlike
 * [androidx.core.animation.doOnEnd] the action is skipped when the animation is cancelled.
 */
internal fun Animator.endListener(onAnimationEnd: () -> Unit) {
    addListener(object : AnimatorListenerAdapter() {
        private var cancelled = false

        override fun onAnimationStart(animation: Animator) {
            cancelled = false
        }

        override fun onAnimationCancel(animation: Animator) {
            cancelled = true
        }

        override fun onAnimationEnd(animation: Animator) {
            if (!cancelled) {
                onAnimationEnd.invoke()
            }
        }
    })
}
