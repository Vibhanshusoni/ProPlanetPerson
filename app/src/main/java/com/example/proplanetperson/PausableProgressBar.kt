
package com.example.proplanetperson

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar

class PausableProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ProgressBar(context, attrs, android.R.attr.progressBarStyleHorizontal) {

    private var animator: ValueAnimator? = null
    private var callback: Callback? = null
    private var duration = DEFAULT_DURATION

    init {
        max = 100
        progress = 0
        progressDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    fun setDuration(duration: Long) {
        this.duration = duration
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun startProgress() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, 100).apply {
            this.duration = this@PausableProgressBar.duration
            addUpdateListener {
                progress = it.animatedValue as Int
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    callback?.onStartProgress()
                }

                override fun onAnimationEnd(animation: Animator) {
                    callback?.onFinishProgress()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    fun pauseProgress() {
        animator?.pause()
    }

    fun resumeProgress() {
        animator?.resume()
    }

    fun clear() {
        animator?.cancel()
    }

    fun setMax() {
        progress = 100
        animator?.cancel()
        callback?.onFinishProgress()
    }

    fun setMin() {
        progress = 0
        animator?.cancel()
        callback?.onFinishProgress()
    }

    fun setMaxWithoutCallback() {
        progress = 100
        animator?.cancel()
    }

    fun setMinWithoutCallback() {
        progress = 0
        animator?.cancel()
    }

    interface Callback {
        fun onStartProgress()
        fun onFinishProgress()
    }

    companion object {
        private const val DEFAULT_DURATION = 1500L
    }
}
