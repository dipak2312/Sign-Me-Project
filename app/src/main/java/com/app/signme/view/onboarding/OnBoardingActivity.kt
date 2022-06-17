package com.app.signme.view.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.common.DepthPageTransformer
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityOnBoardingBinding
import com.app.signme.dataclasses.onBoardings
import com.app.signme.core.BaseActivity
import com.app.signme.viewModel.OnBoardingActivityViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_on_boarding.*

class OnBoardingActivity : BaseActivity<OnBoardingActivityViewModel>() {

    private lateinit var onBoardingAdapter: OnBoardingAdapter
    private var binding: ActivityOnBoardingBinding? = null
    private var currentPosition: Int = 0

    private var viewpagerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            currentPosition = position
            when (currentPosition) {
                0 -> {
                    mbtnBack.visibility= View.INVISIBLE
                    mbtnSkip.visibility= View.VISIBLE
                    mbtnNext.setBackgroundResource(R.drawable.bg_button_unselect)
                }
                2 -> {
                    mbtnNext.text = getString(R.string.label_start_button)
                    mbtnSkip.visibility= View.INVISIBLE
                    mbtnBack.visibility= View.VISIBLE
                    mbtnNext.setBackgroundResource(R.drawable.bg_button_select)

                }
                else -> {
                    mbtnBack.visibility= View.VISIBLE
                    mbtnSkip.visibility= View.VISIBLE
                    mbtnNext.setBackgroundResource(R.drawable.bg_button_unselect)
                    mbtnNext.text = getString(R.string.label_next_button)
                }
            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_boarding)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-onBoardingScreen","view_onBoardingScreen","view_onBoardingScreen")
        binding?.let {
            with(it) {
                setOnBoardingDetails(binding!!.vpOnBoarding)
                setTabLayout(binding!!.mTabLayout, binding!!.vpOnBoarding)

                binding?.mTabLayout?.touchables?.forEach { tabDots ->
                    tabDots.isEnabled = false
                }

                mbtnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Previous Button Click")
                    currentPosition--
                    vpOnBoarding.currentItem = currentPosition
                    Log.i("TAG", "initListener: ")
                }

                binding?.mbtnNext?.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Next Button Click")
                    currentPosition.let { position ->
                        when (position) {
                            2 -> {
                                sharedPreference.isOnBoardingShown = true
                                openLoginActivity()
                            }
                            else -> {
                                currentPosition++
                                vpOnBoarding.currentItem = currentPosition
                            }
                        }
                    }
                }

                binding?.mbtnSkip?.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Skip Button Click")
                    openLoginActivity()
                }
            }
        }
    }

    private fun setTabLayout(mTabLayout: TabLayout, vpOnBoarding: ViewPager2) {
        TabLayoutMediator(mTabLayout,
            vpOnBoarding) { _, _ ->
        }.attach()
    }

    private fun setOnBoardingDetails(vpOnBoarding: ViewPager2) {
        onBoardingAdapter = OnBoardingAdapter(onBoardings)
        vpOnBoarding.adapter = onBoardingAdapter
        vpOnBoarding.registerOnPageChangeCallback(viewpagerPageChangeCallback)
        vpOnBoarding.setPageTransformer(DepthPageTransformer())
    }

    private fun openLoginActivity() {
        startActivity(
            Intent(
                this@OnBoardingActivity,
                (application as AppineersApplication).getLoginActivity()
            )
        )
        finish()
    }
}