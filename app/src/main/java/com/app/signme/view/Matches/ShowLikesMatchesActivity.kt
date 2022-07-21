package com.app.signme.view.Matches

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.adapter.ShowLikesMatchesAdapter
import com.app.signme.commonUtils.utility.IConstants
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
import kotlinx.android.synthetic.main.fragment_home.*

class ShowLikesMatchesActivity:BaseActivity<MatchesViewModel>(),RecyclerViewActionListener {
    private var binding:ActivityShowLikesMatchesBinding?=null
    var mAdapter:ShowLikesMatchesAdapter?=null
    var type:String?=""

    companion object
    {
        fun getStartIntent(mContext: Context,type:String): Intent {
            return Intent(mContext, ShowLikesMatchesActivity::class.java).apply {
                 putExtra(IConstants.TYPE,type)
            }
        }
    }

    override fun setDataBindingLayout() {
        binding=DataBindingUtil.setContentView(this, R.layout.activity_show_likes_matches)
        binding!!.lifecycleOwner=this
        type=intent.getStringExtra(IConstants.TYPE)
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {

        mAdapter= ShowLikesMatchesAdapter(this,this)
        binding!!.mRecycler.adapter=mAdapter
        binding!!.textTitle.text=type

        addObservers()
        initListener()
        getAllList()
    }

    private fun getAllList() {
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                viewModel.getViewAllList("1",type)
            }
        }
    }

    private fun initListener() {
        binding?.apply {

            ibtnBack.setOnClickListener{
                finish()
            }
        }
    }

    private fun addObservers() {

        viewModel.viewLikeSuperlikeMatchesLiveData.observe(this) { response ->
            hideProgressDialog()
            if (response?.settings?.isSuccess == true) {

                if(!response.data.isNullOrEmpty())
                {
                    mAdapter!!.addAllItem(response.data!!)
                }
            }
        }

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