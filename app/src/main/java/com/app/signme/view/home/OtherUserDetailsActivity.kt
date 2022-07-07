package com.app.signme.view.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.RoundedCornersTransformation
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityOtherUserDetailsBinding
import com.app.signme.dataclasses.UserImage
import com.app.signme.view.profile.PagerImageAdapter
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.HomeViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.fragment_profile.*

class OtherUserDetailsActivity :BaseActivity<HomeViewModel>(), RecyclerViewActionListener {

    companion object {
        fun getStartIntent(
            context: Context
        ): Intent {
            return Intent(context, OtherUserDetailsActivity::class.java).apply {
            }
        }
    }
    private var binding:ActivityOtherUserDetailsBinding?=null
    var lookingFor: Array<String>? = null
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

        var friends=getString(R.string.label_friends)+" "+"<font color='#FF5F0D'>80%</font>"
        binding!!.textFriends.text=Html.fromHtml(friends)
        var quickmeet=getString(R.string.label_quickmeet)+" "+"<font color='#FF5F0D'>90%</font>"
        binding!!.textQuickMeet.text=Html.fromHtml(quickmeet)
        var relationship=getString(R.string.label_relationship)+" "+"<font color='#FF5F0D'>90%</font>"
        binding!!.textRelationship.text=Html.fromHtml(relationship)

        viewPagerImages.adapter = mImageAdapter
        viewPagerImages.currentItem = 0
        mTabLayout.touchables?.forEach { tabDots ->
            tabDots.isEnabled = false
        }
        mTabLayout.setupWithViewPager(viewPagerImages)


        mImageAdapter.insertItem(UserImage("", "", "https://appineers.s3.amazonaws.com/sign_me/image/3/1657019138744.png", ""))
        mImageAdapter.insertItem(UserImage("", "", "https://source.unsplash.com/Xq1ntWruZQI/600x800", ""))
        mImageAdapter.insertItem(UserImage("", "", "https://source.unsplash.com/NYyCqdBOKwc/600x800", ""))
        binding!!.mTabLayout.visibility = View.VISIBLE
        mImageAdapter.notifyDataSetChanged()
        Glide.with(this@OtherUserDetailsActivity)
            .load("http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png")
            .into(binding!!.signLogo)

    }

    private fun initListeners() {
        binding?.let {
            with(it) {

                btbBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }
            }
        }
        lookingFor = arrayOf("Friendship", "Quick-Meets", "Relationship")
        addLookingFor()

    }

    private fun addObservers() {

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        }
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

    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {

    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {

    }
}