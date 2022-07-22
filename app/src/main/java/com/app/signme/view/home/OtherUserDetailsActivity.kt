package com.app.signme.view.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityOtherUserDetailsBinding
import com.app.signme.dataclasses.LikeSuperlikeCancelCallback
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.dataclasses.UserImage
import com.app.signme.view.dialogs.MatchesDialog
import com.app.signme.view.profile.PagerImageAdapter
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.view.subscription.SubscriptionPlansActivity
import com.app.signme.viewModel.HomeViewModel
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_other_user_details.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.mTabLayout
import kotlinx.android.synthetic.main.fragment_profile.viewPagerImages
import java.lang.reflect.Type
import java.util.HashMap
import java.util.function.IntConsumer

class OtherUserDetailsActivity :BaseActivity<HomeViewModel>(), RecyclerViewActionListener {

    companion object {
        fun getStartIntent(
            context: Context,
            userId:String?,
            otherUserResponse:SwiperViewResponse,
            type:String?
        ): Intent {
            return Intent(context, OtherUserDetailsActivity::class.java).apply {
                putExtra(IConstants.USERID, userId)
                putExtra(IConstants.USERDETAILS,otherUserResponse)
                putExtra(IConstants.TYPE,type)
            }
        }
    }
    private var binding:ActivityOtherUserDetailsBinding?=null
    var lookingFor=ArrayList<String>()
    var userId:String?=""
    var status:String?=""
    var type:String?=""
    var otherUserResponse:SwiperViewResponse?=null
    var mImageAdapter = PagerImageAdapter(false, this)
    override fun setDataBindingLayout() {
        binding=DataBindingUtil.setContentView(this, R.layout.activity_other_user_details)
        binding!!.lifecycleOwner=this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        initListeners()
        addObservers()
        userId=intent.getStringExtra(IConstants.USERID)
        otherUserResponse=intent.getParcelableExtra(IConstants.USERDETAILS)
        type=intent.getStringExtra(IConstants.TYPE)
        viewPagerImages.adapter = mImageAdapter
        viewPagerImages.currentItem = 0
        mTabLayout.touchables?.forEach { tabDots ->
            tabDots.isEnabled = false
        }
        mTabLayout.setupWithViewPager(viewPagerImages)
        getOtherDetails()
        getSupeLikeCount()
    }


    fun getOtherDetails()
    {
        when {
            checkInternet() -> {
                binding!!.shimmer.startShimmer()

                viewModel.callOtherUserDetailsList(userId)
            }
        }
    }

    private fun initListeners() {
        binding?.let {
            with(it) {

                btbBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }
                btnClose.setOnClickListener{
                    status=IConstants.REJECT
                    callLikeSuperlikeCancel(userId,IConstants.REJECT)
                }

                btnSuperLike.setOnClickListener{
                    if(sharedPreference.superLikeCount == sharedPreference.configDetails!!.defaultSuperLikeCount)
                    {
                        startActivity(SubscriptionPlansActivity.getStartIntent(this@OtherUserDetailsActivity,"1"))
                    }
                    else
                    {
                        status=IConstants.SUPERLIKE
                        callLikeSuperlikeCancel(userId,IConstants.SUPERLIKE)
                    }
                }

                btnLike.setOnClickListener{
                    if(sharedPreference.likeCount == sharedPreference.configDetails!!.defaultLikeCount)
                    {
                        startActivity(SubscriptionPlansActivity.getStartIntent(this@OtherUserDetailsActivity,"1"))
                    }
                    else
                    {
                        status=IConstants.LIKE
                        callLikeSuperlikeCancel(userId,IConstants.LIKE)
                    }

                }

            }
        }

    }

    fun callLikeSuperlikeCancel(userId:String?,status:String)
    {
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                val map = HashMap<String, String>()
                map["connection_type"] = status
                map["connection_user_id"] = userId!!

                viewModel.callLikeSuperLikeCancel(map)
            }
        }
    }

    private fun addObservers() {

        viewModel.otherUserDetailsLiveData.observe(this){response->
            binding!!.shimmer.stopShimmer()
            binding!!.shimmer.visibility=View.GONE
            binding!!.scrollview.visibility=View.VISIBLE

            binding!!.imageView.visibility=View.VISIBLE
            if(type!!.equals(IConstants.MATCHES))
            {
                binding!!.buttonContainer.visibility=View.GONE
                binding!!.buttonContainerMatch.visibility=View.VISIBLE
            }else
            {
                binding!!.buttonContainer.visibility=View.VISIBLE
                binding!!.buttonContainerMatch.visibility=View.GONE
            }

            if (response?.settings?.isSuccess == true) {
                if (!response.data.isNullOrEmpty()) {

                    for (lookfor in response.data!![0].lookingForRelationType!!) {
                        lookingFor.add(lookfor.relationshipStatus.toString())
                    }
                    if(!lookingFor.isNullOrEmpty())
                    {
                        addLookingFor()
                    }
                    binding!!.higherData=response.data?.get(0)?.higherCompatibilityData?.get(0)
                    binding!!.response=response.data?.get(0)
                    Glide.with(this@OtherUserDetailsActivity)
                        .load(response.data!![0].signLogo)
                        .into(binding!!.signLogo)

                    for (userImage in response.data!![0].userMedia!!) {
                        mImageAdapter.insertItem(
                            UserImage(
                                userImage.mediaId,
                                "",
                                userImage.imageUrl,
                                ""
                            )
                        )
                    }
                    if(mImageAdapter.getAllItem().size!=1)
                    {
                        binding!!.mTabLayout.visibility = View.VISIBLE
                    }
                    Glide.with(this@OtherUserDetailsActivity)
                        .load("http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png")
                        .into(binding!!.signLogo)

                    for((i,response) in response.data?.get(0)?.compatibilityData?.withIndex()!!)
                    {
                        if(i==0) {
                            var friends= response.relationshipStatus +" "+getColoredSpanned(response.compatibilityPercentage+"%","#FF5F0D")
                            binding!!.textFriends.text=Html.fromHtml(friends) }
                        if(i==1) {
                            var quickmeet=response.relationshipStatus+" "+getColoredSpanned(response.compatibilityPercentage+"%","#FF5F0D")
                            binding!!.textQuickMeet.text=Html.fromHtml(quickmeet) }
                        if(i==2) {
                            var relationship=response?.relationshipStatus+" "+getColoredSpanned(response.compatibilityPercentage+"%","#FF5F0D")
                            binding!!.textRelationship.text=Html.fromHtml(relationship) }
                    }
                }
            }
        }

        viewModel.userLikeSuperLikeLiveData.observe(this){response->
            hideProgressDialog()
            if (response?.settings?.isSuccess == true) {
               binding!!.buttonContainer.visibility=View.GONE
                when (status) {
                    IConstants.LIKE -> {
                        addLikeSuperLikeCancelStatus(IConstants.LIKE)
                        var likeValue = sharedPreference.likeCount
                        sharedPreference.likeCount = likeValue + 1
                        if (otherUserResponse!!.isLike.equals("Yes")) {
                            showMatchPopup()
                        } else {
                            showMessage(
                                response.settings!!.message,
                                IConstants.SNAKBAR_TYPE_SUCCESS
                            )
                        }

                    }
                    IConstants.SUPERLIKE -> {
                        btnSuperLike.isFocusable = false
                        addLikeSuperLikeCancelStatus(IConstants.SUPERLIKE)
                        var superLikeCountValue = sharedPreference.superLikeCount
                        sharedPreference.superLikeCount = superLikeCountValue + 1

                        getSupeLikeCount()

                        if (otherUserResponse!!.isLike.equals("Yes")) {
                            showMatchPopup()
                        } else {
                            showMessage(
                                response.settings!!.message,
                                IConstants.SNAKBAR_TYPE_SUCCESS
                            )
                        }
                    }
                    IConstants.REJECT -> {
                        addLikeSuperLikeCancelStatus(IConstants.REJECT)
                        finish()
                    }
                }
            }
        }

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            binding!!.shimmer.stopShimmer()

            handleApiStatusCodeError(serverError)
        }
    }


    private fun showMatchPopup() {

        MatchesDialog(otherUserResponse!!,mListener = object :
            MatchesDialog.ClickListener {
            override fun onSuccess() {

                finish()
            }

            override fun onCancel() {

            }

        }).show(supportFragmentManager, "Tag")

    }

    fun addLookingFor()
    {
        for (lookingfor in lookingFor!!) {
            val chip = Chip(this@OtherUserDetailsActivity)
            chip.text = lookingfor
            chip.setTextAppearanceResource(R.style.mychipText);
            binding?.lookingForChipGroup?.addView(chip)
        }
    }


    fun addLikeSuperLikeCancelStatus(status:String)
    {
        (application as AppineersApplication).isMatchesUpdated.postValue(true)
        var likeSuperlikerejectStatus = LikeSuperlikeCancelCallback()
        likeSuperlikerejectStatus.userId=userId
        likeSuperlikerejectStatus.status=status
        likeSuperlikerejectStatus.type=type.toString()
        (application as AppineersApplication).LikeSuperlikeCancelRequest.postValue(likeSuperlikerejectStatus)
    }

    private fun getColoredSpanned(text: String?, color: String?): String? {
        return "<font color=$color>$text</font>"
    }

    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {

    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {

    }

    fun getSupeLikeCount()
    {
        var count = sharedPreference.configDetails!!.defaultSuperLikeCount?.minus(
            sharedPreference.superLikeCount
        )
        if (count == 0) {
            binding!!.textSuperLikeCount.visibility = View.GONE
        } else {
            binding!!.textSuperLikeCount.visibility = View.VISIBLE
            binding!!.textSuperLikeCount.text = count.toString()
        }

    }
}