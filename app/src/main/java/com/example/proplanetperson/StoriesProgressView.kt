package com.example.proplanetperson

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi

class StoriesProgressView : LinearLayout {
    private val PROGRESS_BAR_LAYOUT_PARAM = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
    private val SPACE_LAYOUT_PARAM = LayoutParams(5, LayoutParams.WRAP_CONTENT)

    private val progressBars = mutableListOf<PausableProgressBar>()
    private var storiesCount = -1
    private var current = -1
    private var storiesListener: StoriesListener? = null
    private var isSkipStart = false
    private var isReverseStart = false
    var isComplete: Boolean = false
        private set

    interface StoriesListener {
        fun onNext()
        fun onPrev()
        fun onComplete()
    }

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        orientation = HORIZONTAL
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StoriesProgressView)
        storiesCount = typedArray.getInt(R.styleable.StoriesProgressView_progressCount, 0)
        typedArray.recycle()
        bindViews()
    }

    private fun bindViews() {
        progressBars.clear()
        removeAllViews()

        for (i in 0 until storiesCount) {
            val progressBar = createProgressBar()
            progressBars.add(progressBar)
            addView(progressBar)
            if (i < storiesCount - 1) {
                addView(createSpace())
            }
        }
    }

    private fun createProgressBar(): PausableProgressBar {
        return PausableProgressBar(context).apply {
            layoutParams = PROGRESS_BAR_LAYOUT_PARAM
        }
    }

    private fun createSpace(): View {
        return View(context).apply {
            layoutParams = SPACE_LAYOUT_PARAM
        }
    }

    fun setStoriesCount(count: Int) {
        storiesCount = count
        bindViews()
    }

    fun setStoriesListener(listener: StoriesListener?) {
        storiesListener = listener
    }

    fun skip() {
        if (isSkipStart || isReverseStart || isComplete || current < 0) return
        isSkipStart = true
        progressBars[current].setMax()
    }

    fun reverse() {
        if (isSkipStart || isReverseStart || isComplete || current < 0) return
        isReverseStart = true
        progressBars[current].setMin()
    }

    fun setStoryDuration(duration: Long) {
        progressBars.forEachIndexed { index, bar ->
            bar.setDuration(duration)
            bar.setCallback(getCallback(index))
        }
    }

    fun setStoriesCountWithDurations(durations: LongArray) {
        storiesCount = durations.size
        bindViews()
        durations.forEachIndexed { index, duration ->
            progressBars[index].setDuration(duration)
            progressBars[index].setCallback(getCallback(index))
        }
    }

    private fun getCallback(index: Int): PausableProgressBar.Callback {
        return object : PausableProgressBar.Callback {

            override fun onStartProgress() {
                current = index
            }

            override fun onFinishProgress() {
                if (isReverseStart) {
                    storiesListener?.onPrev()
                    if (current - 1 >= 0) {
                        progressBars[current - 1].setMinWithoutCallback()
                        progressBars[--current].startProgress()
                    } else {
                        progressBars[current].startProgress()
                    }
                    isReverseStart = false
                    return
                }

                val next = current + 1
                if (next < progressBars.size) {
                    storiesListener?.onNext()
                    progressBars[next].startProgress()
                } else {
                    isComplete = true
                    storiesListener?.onComplete()
                }
                isSkipStart = false
            }
        }
    }

    fun startStories() {
        if (progressBars.isNotEmpty()) {
            progressBars[0].startProgress()
        }
    }

    fun startStories(from: Int) {
        for (i in 0 until from) {
            progressBars[i].setMaxWithoutCallback()
        }
        progressBars.getOrNull(from)?.startProgress()
    }

    fun destroy() {
        progressBars.forEach { it.clear() }
    }

    fun pause() {
        if (current >= 0) {
            progressBars[current].pauseProgress()
        }
    }

    fun resume() {
        if (current >= 0) {
            progressBars[current].resumeProgress()
        }
    }

    companion object {
        private val TAG: String = StoriesProgressView::class.java.simpleName
    }
}

