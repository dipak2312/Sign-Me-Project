package com.app.signme.view.home

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.app.signme.R
import com.app.signme.adapter.SwiperViewAdapter
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.SwiperDiffCallback
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentHomeBinding
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.scheduler.aws.AwsService
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.HomeViewModel
import com.yuyakaido.android.cardstackview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : BaseFragment<HomeViewModel>(),RecyclerViewActionListener,CardStackListener {

    private lateinit var binding:FragmentHomeBinding
    var uploadedFiles = ArrayList<String>()
    var mAdapter: SwiperViewAdapter? =null
    var manager:CardStackLayoutManager?=null

    override fun setDataBindingLayout() {}

    override fun provideLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupView(view: View) {
        binding = DataBindingUtil.bind(view)!!
        binding.lifecycleOwner = this

        if(!sharedPreference.userProfileUrl.isNullOrEmpty())
        {
            uploadedFiles = sharedPreference.userProfileUrl!!.split(",").map { it.trim() } as ArrayList<String>
            if(!uploadedFiles.isNullOrEmpty())
            {
                deleteAllUploadedFiles()
            }
        }

        manager= CardStackLayoutManager(this@HomeFragment.requireContext(),this)
        manager!!.setStackFrom(StackFrom.Bottom)
        manager!!.setVisibleCount(3)
        manager!!.setTranslationInterval(8.0f)
        manager!!.setScaleInterval(0.95f)
        manager!!.setSwipeThreshold(0.10f)
        manager!!.setMaxDegree(120.0f)
        manager!!.setDirections(Direction.HORIZONTAL)
        manager!!.setDirections(Direction.VERTICAL)
        manager!!.setCanScrollHorizontal(true)
        manager!!.setCanScrollVertical(true)
        manager!!.setDirections(arrayOf(Direction.Left,Direction.Right,Direction.Top).toMutableList())
        manager!!.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager!!.setOverlayInterpolator(LinearInterpolator())
        mAdapter= SwiperViewAdapter(this,mBaseActivity!!)
        binding!!.cardStackView.layoutManager = manager
        binding!!.cardStackView.adapter=mAdapter
        binding!!.cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
        addObservers()
        initListener()
        mAdapter!!.addAllItem(createswiperValue())
        //getSwiperList()
     }

    fun getSwiperList()
    {
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )

                viewModel.getSwiperList("1")
            }
        }
    }

    private fun deleteAllUploadedFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            if (!uploadedFiles.isNullOrEmpty()) {
                uploadedFiles.forEach { image ->
                    AwsService.deleteFile(image.substringAfter(".com/"))
                }
                uploadedFiles.clear()
                sharedPreference.userProfileUrl=""

                (activity?.application as AppineersApplication).awsFileUploader.postValue(
                    null
                )
                activity?.sendBroadcast(Intent(AwsService.UPLOAD_CANCELLED_ACTION))
            }
        }
    }

    private fun initListener() {
        binding?.apply {

            btnSetting.setOnClickListener{
                startActivity(SettingsActivity.getStartIntent(this@HomeFragment.requireContext()))
            }

            btnClose.setOnClickListener{
                val close = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Slow.duration)
                    .setInterpolator(LinearInterpolator())
                    .build()
                manager!!.setSwipeAnimationSetting(close)
                cardStackView.swipe()

            }
            btnLike.setOnClickListener{
                val like = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Slow.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager!!.setSwipeAnimationSetting(like)
                cardStackView.swipe()
            }
            btnSuperLike.setOnClickListener{
                val superlike = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Top)
                    .setDuration(Duration.Slow.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager!!.setSwipeAnimationSetting(superlike)
                cardStackView.swipe()
            }
        }
    }

    private fun addObservers() {

        viewModel.swiperListLiveData.observe(this){response->
            hideProgressDialog()
            if (response?.settings?.isSuccess == true) {
                if (!response.data.isNullOrEmpty()) {
                    mAdapter!!.addAllItem(response.data!!)
                }
            }
        }
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            (activity as BaseActivity<*>).handleApiStatusCodeError(serverError)
        }
    }


    private fun createswiperValue(): List<SwiperViewResponse> {
        val swiperValue = ArrayList<SwiperViewResponse>()

        swiperValue.add(SwiperViewResponse(name = "Yasaka Shrine", signName = "Aquarius", profileImage = "https://appineers.s3.amazonaws.com/sign_me/image/3/1657019138744.png",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Yasaka Shrine", signName = "Aquarius", profileImage = "https://source.unsplash.com/Xq1ntWruZQI/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Poonam", signName = "Aquarius", profileImage = "https://source.unsplash.com/NYyCqdBOKwc/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Bamboo Forest", signName = "Aquarius", profileImage = "https://source.unsplash.com/buF62ewDLcQ/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Empire State", signName = "Aquarius", profileImage = "https://source.unsplash.com/USrZRcRS2Lw/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Sagar", signName = "Aquarius", profileImage = "https://source.unsplash.com/PeFk7fzxTdk/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Louvre Museum", signName = "Aquarius", profileImage = "https://source.unsplash.com/LrMWHKqilUw/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Eiffel Tower", signName = "Aquarius", profileImage = "https://source.unsplash.com/HN-5Z6AmxrM/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Big Ben", signName = "Aquarius", profileImage = "https://source.unsplash.com/CdVAUADdqEc/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        swiperValue.add(SwiperViewResponse(name = "Pravin", signName = "Aquarius", profileImage = "https://source.unsplash.com/AWh9C-QjhE4/600x800",signLogo="http://appineers.s3.amazonaws.com/sign_me/astrology_sign/1/Aries.png",relationshipPercent="20",age="29",relationshipDescription = "Similar minds"))
        return swiperValue
    }


    override fun onCardDragging(direction: Direction, ratio: Float) {
    }

    override fun onCardSwiped(direction: Direction) {

        if (manager!!.topPosition == mAdapter!!.itemCount - 5) {
            val old = mAdapter!!.getAllItems()
            val new = old.plus(createswiperValue())
            val callback = SwiperDiffCallback(old, new)
            val result = DiffUtil.calculateDiff(callback)
            mAdapter!!.addAllItem(new)
            result.dispatchUpdatesTo(mAdapter!!)
        }
    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager!!.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager!!.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {

        Log.d("CardStackView", "onCardCanceled: ${manager!!.topPosition}")

    }

    override fun onCardDisappeared(view: View, position: Int) {
        Log.d("CardStackView", "onCardCanceled: ${manager!!.topPosition}")
    }

    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {

        when(viewId)
        {
            R.id.cardSwiperView->{

                startActivity(OtherUserDetailsActivity.getStartIntent(this@HomeFragment.requireContext()))
            }
        }

    }


    override fun onLoadMore(itemCount: Int, nextPage: Int) {

    }

}

