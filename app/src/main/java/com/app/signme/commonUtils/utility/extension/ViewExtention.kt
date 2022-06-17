package com.app.signme.commonUtils.utility.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.signme.R


/**
 * to show keyboard
 */
fun View.showKeyBoard() {
    this.postDelayed({
        val inputManager = this.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(this, InputMethodManager.HIDE_NOT_ALWAYS)
    }, 600)
}

/**
 * to hide keyboard
 */
fun View.hideKeyBoard() {
    val inputManager = this.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}


/**
 * Focus on given view and open soft keyboard
 * @receiver View  Root view
 * @param viewId Int? Id of view on which we need do focus
 * @param showKeyBoard Boolean show keyboard or not
 */
fun View.focusOnField(viewId: Int?, showKeyBoard: Boolean = true) {
    viewId?.apply {
        this@focusOnField.findViewById<View>(viewId)?.requestFocus()
    }
    if (showKeyBoard) this.showKeyBoard()
}


fun View.showViewWithAnimation() {
    this.visibility = View.VISIBLE
    this.alpha = 0.0f

// Start the animation
    this.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setListener(null)
}

fun View.hideViewWithAnimation(isUp: Boolean = false) {

// Start the animation
    this.animate()
            .translationY(if (isUp) -this.height.toFloat() else this.height.toFloat())
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    this@hideViewWithAnimation.visibility = View.GONE
                }
            })
}

fun View.hideViewWithoutTranslation() {

// Start the animation
    this.animate()
            .alpha(0.0f)
            .setInterpolator(LinearInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    this@hideViewWithoutTranslation.visibility = View.GONE
                }
            })
}

fun View.showViewWithoutTranslation(isVisible: Boolean = false) {
    if (!isVisible) {
        this.visibility = View.VISIBLE
        this.alpha = 0.0f
    }

// Start the animation
    this.animate()
            .alpha(1.0f)
            .setInterpolator(LinearInterpolator())
            .setListener(null)
}

fun ViewGroup.runLayoutAnimation(resourceId: Int) {
    val context = this.context

    val controller = AnimationUtils.loadLayoutAnimation(context, resourceId)

    this.layoutAnimation = controller
    (this as? RecyclerView)?.adapter?.notifyDataSetChanged()
    this.scheduleLayoutAnimation()
}

fun SwipeRefreshLayout.setSwipeToRefreshColor() {
    this.setColorSchemeColors(
            ContextCompat.getColor(this.context, R.color.swipe_col_1),
            ContextCompat.getColor(this.context, R.color.swipe_col_2),
            ContextCompat.getColor(this.context, R.color.swipe_col_3),
            ContextCompat.getColor(this.context, R.color.swipe_col_4)
    )
}

/**
 * This function will add material icons with given string
 * @param iconName : Name of icon
 * @param isBefore : boolean for add icon before or after the text
 */
fun String.getTextMaterialIcon(iconName: String = "", isBefore: Boolean = true): String {
    return if (isBefore) {
        "{".plus(iconName).plus("} ").plus(this)
    } else {
        this.plus("{").plus(iconName).plus("}")
    }
}

fun View.setOnClickListener(debounceTime: Long = 1000L, shouldHideKeyBoard:Boolean = true,action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            if(shouldHideKeyBoard)
                hideKeyBoard()
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}
