package com.app.signme.view.landing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityLocationEnableBinding
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.viewModel.LandingViewMode

class LocationEnableActivity : BaseActivity<LandingViewMode>() {

    var binding:ActivityLocationEnableBinding?=null

    companion object {

        fun getStartIntent(mContext: Context): Intent {
            return Intent(mContext, LocationEnableActivity::class.java).apply {

            }
        }
    }

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_enable)
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
       activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {

        binding!!.rippleBackground.startRippleAnimation()
    }
}