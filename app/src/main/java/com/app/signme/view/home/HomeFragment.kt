package com.app.signme.view.home

import android.annotation.SuppressLint
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
import com.app.signme.view.dialogs.MatchesDialog
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.view.subscription.SubscriptionPlansActivity
import com.app.signme.viewModel.HomeViewModel
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.fragment_home.*
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
    var status:String?=""
    var isLike:Boolean=true
    var isSuperLike:Boolean=true
    var direction:String?=""

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
        //mAdapter!!.addAllItem(createswiperValue())
       getSwiperList()


     }

    fun getSwiperList()
    {
        when {
            checkInternet() -> {

                binding!!.shimmer.startShimmer()

                viewModel.getSwiperList("1", sharedPreference.userDetail!!.astrologySignId)
            }
        }
    }


    fun callLikeSuperlikeCancel(userId:String?,status:String)
    {
        when {
            checkInternet() -> {
                val map = HashMap<String, String>()
                map["connection_type"] = status
                map["connection_user_id"] = userId!!

                viewModel.callLikeSuperLikeCancel(map)
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

                if(sharedPreference.likeCount == sharedPreference.configDetails!!.defaultLikeCount)
                {
                    startActivity(SubscriptionPlansActivity.getStartIntent(this@HomeFragment.requireContext(),"1"))
                }
                else{
                    val like = SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Right)
                        .setDuration(Duration.Slow.duration)
                        .setInterpolator(AccelerateInterpolator())
                        .build()
                    manager!!.setSwipeAnimationSetting(like)
                    cardStackView.swipe()
                }

            }
            btnSuperLike.setOnClickListener{
                if(sharedPreference.superLikeCount == sharedPreference.configDetails!!.defaultSuperLikeCount)
                {
                    startActivity(SubscriptionPlansActivity.getStartIntent(this@HomeFragment.requireContext(),"1"))
                }
                else
                {
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
    }

    private fun addObservers() {

        viewModel.swiperListLiveData.observe(this){response->
            binding!!.shimmer.stopShimmer()
            binding!!.shimmer.visibility=View.GONE
            binding!!.buttonContainer.visibility=View.VISIBLE
            if (response?.settings?.isSuccess == true) {
                if (!response.data.isNullOrEmpty()) {
                    mAdapter!!.addAllItem(response.data!!)
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
        viewModel.userLikeSuperLikeLiveData.observe(this){response->
            hideProgressDialog()
        }
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            binding!!.shimmer.stopShimmer()
            (activity as BaseActivity<*>).handleApiStatusCodeError(serverError)
        }

        (activity?.application as AppineersApplication).isLike.observe(this)
            {isLike->
                if(isLike)
                {
                   status=IConstants.LIKE
                    val like = SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Right)
                        .setDuration(Duration.Slow.duration)
                        .setInterpolator(AccelerateInterpolator())
                        .build()
                     manager!!.setSwipeAnimationSetting(like)
                     binding!!.cardStackView.swipe()
                    (activity?.application as AppineersApplication).isLike.postValue(false)
                }
           }

        (activity?.application as AppineersApplication).isSuperLike.observe(this)
        {isSuperLike->
            if(isSuperLike)
            {
                status=IConstants.SUPERLIKE
                val superlike = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Top)
                    .setDuration(Duration.Slow.duration)
                    .setInterpolator(AccelerateInterpolator())
                    .build()
                manager!!.setSwipeAnimationSetting(superlike)
                binding!!.cardStackView.swipe()
                (activity?.application as AppineersApplication).isSuperLike.postValue(false)
            }
        }

        (activity?.application as AppineersApplication).isReject.observe(this)
        {isReject->
            if(isReject)
            {
                status=IConstants.REJECT
                val close = SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Slow.duration)
                    .setInterpolator(LinearInterpolator())
                    .build()
                manager!!.setSwipeAnimationSetting(close)
                binding!!.cardStackView.swipe()
                (activity?.application as AppineersApplication).isReject.postValue(false)
            }
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


    override fun onCardDragging(mydirection: Direction, ratio: Float) {

        direction=mydirection.name

    }

    override fun onCardSwiped(direction: Direction) {

        when(direction.name)
        {
            "Left"->{
                if(status.isNullOrEmpty())
                { callLikeSuperlikeCancel(mAdapter!!.getAllItems()[manager!!.topPosition-1].userId,IConstants.REJECT) }else { status="" }
            }
            "Right"->{
                if(status.isNullOrEmpty())
                { callLikeSuperlikeCancel(mAdapter!!.getAllItems()[manager!!.topPosition-1].userId,IConstants.LIKE)
                    if(mAdapter!!.getAllItems()[manager!!.topPosition-1].isLike.equals("Yes"))
                    {
                        showMatchPopup(mAdapter!!.getItem(manager!!.topPosition-1))
                    }

                    var likeValue= sharedPreference.likeCount
                    sharedPreference.likeCount=likeValue+1

                    if(sharedPreference.likeCount == sharedPreference.configDetails!!.defaultLikeCount)
                    {
                        manager!!.setDirections(arrayOf(Direction.Left,Direction.Top).toMutableList())
                        binding!!.cardStackView.layoutManager = manager
                    }


                }else { status="" }
            }
            "Top"->{
                if(status.isNullOrEmpty())
                { callLikeSuperlikeCancel(mAdapter!!.getAllItems()[manager!!.topPosition-1].userId,IConstants.SUPERLIKE)
                    if(mAdapter!!.getAllItems()[manager!!.topPosition-1].isLike.equals("Yes"))
                    {
                        showMatchPopup(mAdapter!!.getItem(manager!!.topPosition-1))
                    }

                    var superLikeCountValue= sharedPreference.superLikeCount
                    sharedPreference.superLikeCount=superLikeCountValue+1

                    if(sharedPreference.superLikeCount == sharedPreference.configDetails!!.defaultSuperLikeCount)
                    {
                        manager!!.setDirections(arrayOf(Direction.Left,Direction.Right).toMutableList())
                        binding!!.cardStackView.layoutManager = manager
                    }
                    var count=sharedPreference.configDetails!!.defaultSuperLikeCount?.minus(sharedPreference.superLikeCount)
                    if(count==0)
                    {
                        binding!!.textSuperlikeCount.visibility=View.GONE
                    }
                    else{
                        binding!!.textSuperlikeCount.text=count.toString()
                    }

                }else { status="" }
            }
        }

        if (manager!!.topPosition == mAdapter!!.itemCount - 5) {
//            val old = mAdapter!!.getAllItems()
//            val new = old.plus(createswiperValue())
//            val callback = SwiperDiffCallback(old, new)
//            val result = DiffUtil.calculateDiff(callback)
//            mAdapter!!.addAllItem(new)
//            result.dispatchUpdatesTo(mAdapter!!)
        }

        if(manager!!.topPosition==mAdapter!!.itemCount)
        {
            binding!!.relEmptyMessage.visibility=View.VISIBLE
            binding!!.buttonContainer.visibility=View.GONE
        }
    }

    private fun showMatchPopup(item: SwiperViewResponse) {

        MatchesDialog(item,mListener = object :
            MatchesDialog.ClickListener {
            override fun onSuccess() {

            }

            override fun onCancel() {

            }

        }).show(requireFragmentManager(), "Tag")

    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager!!.topPosition}")
    }


    override fun onCardCanceled() {

        when(direction)
        {
            "Right"->
            {
                if(isLike)
                {
                    if(sharedPreference.likeCount == sharedPreference.configDetails!!.defaultLikeCount)
                    {
                        isLike=false
                        startActivity(SubscriptionPlansActivity.getStartIntent(this@HomeFragment.requireContext(),"1"))
                    }
                }
            }
            "Top"->
            {
                if(isSuperLike)
                {
                    if(sharedPreference.superLikeCount == sharedPreference.configDetails!!.defaultSuperLikeCount)
                    {
                        isSuperLike=false
                        startActivity(SubscriptionPlansActivity.getStartIntent(this@HomeFragment.requireContext(),"1"))
                    }
                }
            }
        }

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

                startActivity(OtherUserDetailsActivity.getStartIntent(this@HomeFragment.requireContext(),mAdapter!!.getItem(position).userId,mAdapter!!.getItem(position)))
            }
        }

    }


    override fun onLoadMore(itemCount: Int, nextPage: Int) {

    }

    override fun onResume() {
        super.onResume()
        isLike=true
        isSuperLike=true
        if(sharedPreference.configDetails!!.defaultLikeCount== sharedPreference.likeCount)
        {
            manager!!.setDirections(arrayOf(Direction.Left,Direction.Top).toMutableList())
            binding!!.cardStackView.layoutManager = manager
        }

        if(sharedPreference.configDetails!!.defaultSuperLikeCount== sharedPreference.superLikeCount)
        {
            manager!!.setDirections(arrayOf(Direction.Left,Direction.Right).toMutableList())
            binding!!.cardStackView.layoutManager = manager
        }

        var count=sharedPreference.configDetails!!.defaultSuperLikeCount?.minus(sharedPreference.superLikeCount)
        if(count==0)
        {
            binding!!.textSuperlikeCount.visibility=View.GONE
        }
        else{
            binding!!.textSuperlikeCount.text=count.toString()
        }
    }
}

