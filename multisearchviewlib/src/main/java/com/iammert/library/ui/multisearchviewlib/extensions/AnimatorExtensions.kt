package com.iammert.library.ui.multisearchviewlib.extensions

import android.animation.Animator
import android.animation.ValueAnimator
import com.iammert.library.ui.multisearchviewlib.helper.SimpleAnimationListener

fun ValueAnimator.endListener(onAnimationEnd: ()->Unit){
    addListener(object : SimpleAnimationListener(){
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            onAnimationEnd.invoke()
        }
    })
}