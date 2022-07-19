package com.app.signme.view.Matches

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.adapter.ShowLikesMatchesAdapter
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityShowLikesMatchesBinding
import com.app.signme.dataclasses.Social
import com.app.signme.view.home.HomeActivity
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.view.subscription.SubscriptionPlansActivity
import com.app.signme.viewModel.MatchesViewModel

class ShowLikesMatchesActivity:BaseActivity<MatchesViewModel>(),RecyclerViewActionListener {
    private var binding:ActivityShowLikesMatchesBinding?=null
    var mAdapter:ShowLikesMatchesAdapter?=null

    companion object
    {
        fun getStartIntent(mContext: Context): Intent {
            return Intent(mContext, ShowLikesMatchesActivity::class.java).apply {

            }
        }
    }

    override fun setDataBindingLayout() {
        binding=DataBindingUtil.setContentView(this, R.layout.activity_show_likes_matches)
        binding!!.lifecycleOwner=this

    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {

        mAdapter= ShowLikesMatchesAdapter(this,this)
        binding!!.mRecycler.adapter=mAdapter

        addObservers()
        initListener()
    }

    private fun initListener() {
        binding?.apply {

            ibtnBack.setOnClickListener{
                finish()
            }
        }
    }

    private fun addObservers() {

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        }
    }

    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {
        TODO("Not yet implemented")
    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {
        TODO("Not yet implemented")
    }
}