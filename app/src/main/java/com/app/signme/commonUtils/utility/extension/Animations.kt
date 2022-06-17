package com.app.signme.commonUtils.utility.extension

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

/**
 * for face animation
 */
fun View.fade(from: Float, to: Float, duration: Long, func: (Animation) -> Unit = {}) {
    val fadeIn = AlphaAnimation(from, to)
    fadeIn.duration = duration
    func(fadeIn)
    startAnimation(fadeIn)
}

/**
 * for scale animation
 */
fun View.scale(fromX: Float, toX: Float, fromY: Float, toY: Float, pivotX: Float, pivotY: Float, duration: Long, func: (Animation) -> Unit = {}) {
    val anim = ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY)
    anim.duration = duration
    func(anim)
    startAnimation(anim)
}