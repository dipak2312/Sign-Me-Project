package com.app.signme.view.Matches

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.adapter.ShowLikesMatchesAdapter
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityShowLikesMatchesBinding
import com.app.signme.dataclasses.Social
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.view.home.HomeActivity
import com.app.signme.view.home.OtherUserDetailsActivity
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.view.subscription.SubscriptionPlansActivity
import com.app.signme.viewModel.MatchesViewModel
import kotlinx.android.synthetic.main.activity_show_likes_matches.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.function.IntConsumer

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
                viewModel.getViewAllList(mAdapter!!.nextPage.toString(),type)
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
            hideProgressBar()
            if (response?.settings?.isSuccess == true) {

                if(!response.data.isNullOrEmpty())
                {
                    mAdapter!!.totalCount = response.settings?.count!!
                    mAdapter!!.nextPage = mAdapter!!.nextPage + 1
                    mAdapter!!.addAllItem(response.data!!)
                }

                when(type)
                {
                    IConstants.MATCH->{
                        binding!!.textShow.text=response.settings!!.count +" "+getString(R.string.label_matches_details)
                        binding!!.imgShow.setImageResource(R.drawable.ic_match_details)
                    }
                    IConstants.LIKE->
                    {
                        binding!!.textShow.text=response.settings!!.count+" "+getString(R.string.label_like_details)
                        binding!!.imgShow.setImageResource(R.drawable.ic_like_details)
                    }
                    IConstants.SUPERLIKE->{
                        binding!!.textShow.text=response.settings!!.count+" "+getString(R.string.label_superlike_details)
                        binding!!.imgShow.setImageResource(R.drawable.ic_superlike_details)
                    }
                }
            }
        }

        (application as AppineersApplication).isMatchesUpdated.observe(this){ isMatches->
            if(isMatches)
            {
                mAdapter!!.removeAll()
                getAllList()
                //(application as AppineersApplication).isMatchesUpdated.postValue(false)
            }
        }

        (application as AppineersApplication).isBlockUnblock.observe(this) { blockunblock ->
            if (blockunblock) {
                mAdapter!!.removeAll()
                getAllList()
            }
        }

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            if (serverError.code == 500) {
                binding!!.relEmptyMessage.visibility = View.VISIBLE
                binding!!.linTilte.visibility=View.GONE
            }
            else if (serverError.code == 404) {
                binding!!.relEmptyMessage.visibility = View.VISIBLE
                binding!!.linTilte.visibility=View.GONE
            }else {
                handleApiStatusCodeError(serverError)
            }
        }
    }

    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {

        when(viewId)
        {
            R.id.relLikeSuperlikeMatch->
            {
                when(type)
                {
                    IConstants.MATCH->{
                        callOtherDetails("No",position,IConstants.MATCHES)
                    }
                    IConstants.LIKE->
                    {
                        callOtherDetails("Yes",position,IConstants.LIKE)
                    }
                    IConstants.SUPERLIKE->{
                        callOtherDetails("Yes",position,IConstants.SUPERLIKE)
                    }
                }
            }
        }
    }

    private fun showProgressBar() {
        binding!!.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding!!.progressBar.visibility = View.GONE
    }

    fun callOtherDetails(isLike:String,position: Int,status:String){
        var otherUserResponse= SwiperViewResponse(isLike = isLike,name = mAdapter!!.getItem(position).firstName,profileImage = mAdapter!!.getItem(position).profileImage)
        startActivity(OtherUserDetailsActivity.getStartIntent(this@ShowLikesMatchesActivity,mAdapter!!.getItem(position).userId,otherUserResponse,status))
    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {
        showProgressBar()
        viewModel.getViewAllList(mAdapter!!.nextPage.toString(),type)
    }
}