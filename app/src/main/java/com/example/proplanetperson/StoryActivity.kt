package com.example.proplanetperson

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.viewpager2.widget.ViewPager2
import com.example.proplanetperson.R
import com.example.proplanetperson.adapters.StoryPagerAdapter

class StoryActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var progressLayout: LinearLayout
    private lateinit var leftTapZone: View
    private lateinit var rightTapZone: View
    private lateinit var storyProfileImage: ImageView

    private val storyDuration = 5000L // 5 seconds per story
    private var currentIndex = 0
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isPaused = false

    // 🔁 Replace with real image URLs
    private val storyList = listOf(
        "https://your-image-url-1.jpg",
        "https://your-image-url-2.jpg",
        "https://your-image-url-3.jpg"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        viewPager = findViewById(R.id.viewPager)
        progressLayout = findViewById(R.id.progressLayout)
        leftTapZone = findViewById(R.id.leftTapZone)
        rightTapZone = findViewById(R.id.rightTapZone)
        storyProfileImage = findViewById(R.id.story_profile_image)

        handler = Handler(Looper.getMainLooper())

        setupProgressBars()
        setupViewPager()
        setupTouchListeners()

        startStory()
    }

    private fun setupProgressBars() {
        progressLayout.removeAllViews()
        for (i in storyList.indices) {
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                    marginEnd = if (i != storyList.lastIndex) 8 else 0
                }
                max = 1000
                progress = 0
            }
            progressLayout.addView(progressBar)
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = StoryPagerAdapter(this, storyList)
        viewPager.isUserInputEnabled = false // disable manual swiping
    }

    private fun setupTouchListeners() {
        val longPressThreshold = 200L
        var downTime = 0L

        val listener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downTime = System.currentTimeMillis()
                    pauseStory()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val duration = System.currentTimeMillis() - downTime
                    resumeStory()
                    if (duration < longPressThreshold) {
                        when (v.id) {
                            R.id.leftTapZone -> showPreviousStory()
                            R.id.rightTapZone -> showNextStory()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        leftTapZone.setOnTouchListener(listener)
        rightTapZone.setOnTouchListener(listener)
    }

    private fun startStory() {
        updateProgressBars()
        runnable = object : Runnable {
            override fun run() {
                if (!isPaused) {
                    updateProgress()
                    handler.postDelayed(this, 50)
                } else {
                    handler.postDelayed(this, 50) // keep checking for resume
                }
            }
        }
        handler.post(runnable)
    }

    private fun updateProgressBars() {
        progressLayout.children.forEachIndexed { index, view ->
            (view as ProgressBar).progress = when {
                index < currentIndex -> 1000
                index == currentIndex -> 0
                else -> 0
            }
        }
    }

    private fun updateProgress() {
        val currentBar = progressLayout.getChildAt(currentIndex) as ProgressBar
        if (currentBar.progress < 1000) {
            currentBar.progress += (1000 / (storyDuration / 50)).toInt()
        } else {
            showNextStory()
        }
    }

    private fun showNextStory() {
        if (currentIndex < storyList.lastIndex) {
            currentIndex++
            viewPager.currentItem = currentIndex
            updateProgressBars()
        } else {
            finish() // Close activity after last story
        }
    }

    private fun showPreviousStory() {
        if (currentIndex > 0) {
            currentIndex--
            viewPager.currentItem = currentIndex
            updateProgressBars()
        }
    }

    private fun pauseStory() {
        isPaused = true
    }

    private fun resumeStory() {
        isPaused = false
    }

    override fun onPause() {
        super.onPause()
        pauseStory()
    }

    override fun onResume() {
        super.onResume()
        resumeStory()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
