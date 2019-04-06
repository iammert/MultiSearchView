package com.iammert.library.ui.multisearchviewlib.helper

import android.animation.ObjectAnimator
import android.view.View

object LayoutTransitions {

    val ALPHA_APPEAR = ObjectAnimator.ofFloat(null, View.ALPHA, 0f, 1f)

    val ALPHA_DISAPPEAR = ObjectAnimator.ofFloat(null, View.ALPHA, 1f, 0f)
}