package com.app.signme.view.home

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import com.app.signme.R
import com.app.signme.adapter.SwiperViewAdapter
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.commonUtils.utility.extension.toMMDDYYYDate
import com.app.signme.commonUtils.utility.extension.toMMDDYYYStr
import com.app.signme.core.BaseActivity
import com.app.signme.core.BaseFragment
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentHomeBinding
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.scheduler.aws.AwsService
import com.app.signme.view.chat.ChatRoomActivity
import com.app.signme.view.dialogs.MatchesDialog
import com.app.signme.view.notification.NotificationActivity
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.view.subscription.SubscriptionPlansActivity
import com.app.signme.viewModel.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.yuyakaido.android.cardstackview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HomeFragment : BaseFragment<HomeViewModel>(), RecyclerViewActionListener, CardStackListener {

    private lateinit var binding: FragmentHomeBinding
    var uploadedFiles = ArrayList<String>()
    var mAdapter: SwiperViewAdapter? = null
    var manager: CardStackLayoutManager? = null
    var status: String? = ""
    var direction: String? = ""
    private val firebaseFireStoreDB = FirebaseFirestore.getInstance()
    private var chatRoomId: String = "Chats"

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

        if (!sharedPreference.userProfileUrl.isNullOrEmpty()) {
            uploadedFiles =
                sharedPreference.userProfileUrl!!.split(",").map { it.trim() } as ArrayList<String>
            if (!uploadedFiles.isNullOrEmpty()) {
                deleteAllUploadedFiles()
            }
        }

        manager = CardStackLayoutManager(this@HomeFragment.requireContext(), this)
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
        manager!!.setDirections(
            arrayOf(
                Direction.Left,
                Direction.Right,
                Direction.Top
            ).toMutableList()
        )
        manager!!.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager!!.setOverlayInterpolator(LinearInterpolator())
        mAdapter = SwiperViewAdapter(this, mBaseActivity!!)
        binding!!.cardStackView.layoutManager = manager
        binding!!.cardStackView.adapter = mAdapter
        binding!!.cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
        addObservers()
        initListener()
        getSwiperList()


    }

    fun getSwiperList() {
        when {
            checkInternet() -> {

                binding!!.shimmer.startShimmer()

                viewModel.getSwiperList("1", sharedPreference.userDetail!!.astrologySignId)
            }
        }
    }


    fun callLikeSuperlikeCancel(userId: String?, status: String) {
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
                sharedPreference.userProfileUrl = ""

                (activity?.application as AppineersApplication).awsFileUploader.postValue(
                    null
                )
                activity?.sendBroadcast(Intent(AwsService.UPLOAD_CANCELLED_ACTION))
            }
        }
    }

    private fun initListener() {
        binding?.apply {

            btnSetting.setOnClickListener {
                startActivity(SettingsActivity.getStartIntent(this@HomeFragment.requireContext()))
            }

            btnNotificationCount.setOnClickListener{
                startActivity(NotificationActivity.getStartIntent(this@HomeFragment.requireContext()))
            }

            btnClose.setOnClickListener {
                swapReject()
            }
            btnLike.setOnClickListener {


                if (sharedPreference.likeCount == sharedPreference.configDetails!!.defaultLikeCount) {
                    startActivity(
                        SubscriptionPlansActivity.getStartIntent(
                            this@HomeFragment.requireContext(),
                            "1"
                        )
                    )
                } else {
                    swapLike()
                }
            }
            btnSuperLike.setOnClickListener {
                if (sharedPreference.superLikeCount == sharedPreference.configDetails!!.defaultSuperLikeCount) {
                    startActivity(
                        SubscriptionPlansActivity.getStartIntent(
                            this@HomeFragment.requireContext(),
                            "1"
                        )
                    )
                } else {
                    swapSuperLike()
                }
            }
        }
    }

    private fun removeByUserId(userId: String) {
        val user = mAdapter!!.getAllItems().find { it.userId == userId }
        if (user != null) {
            val index = mAdapter!!.getAllItems().indexOf(user)
            mAdapter!!.removeItem(index)
        }
    }

    private fun addObservers() {

        viewModel.swiperListLiveData.observe(this) { response ->
            binding!!.shimmer.stopShimmer()
            binding!!.shimmer.visibility = View.GONE
            binding!!.buttonContainer.visibility = View.VISIBLE
            if (response?.settings?.isSuccess == true) {
                if (!response.data.isNullOrEmpty()) {
                    mAdapter!!.addAllItem(response.data!!)
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }

        (activity?.application as AppineersApplication).notificationsCount.observe(
            this, androidx.lifecycle.Observer {
                if (it != null && it.isNotEmpty() && !it.equals("0")) {
                    binding?.textNotificationCount!!.setText(it)
                    binding?.textNotificationCount!!.visibility = View.VISIBLE
                } else {
                    binding?.textNotificationCount!!.visibility = View.INVISIBLE
                }
            })
        viewModel.userLikeSuperLikeLiveData.observe(this) { response ->
            hideProgressDialog()
        }
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            binding!!.shimmer.stopShimmer()
            if (serverError.code == 500) {
                binding!!.shimmer.visibility = View.GONE
                binding!!.relEmptyMessage.visibility = View.VISIBLE
            }
            else if (serverError.code == 404) {
                binding!!.shimmer.visibility = View.GONE
                binding!!.relEmptyMessage.visibility = View.VISIBLE
            }else {
                (activity as BaseActivity<*>).handleApiStatusCodeError(serverError)
            }
        }

        (activity?.application as AppineersApplication).isSwiperUpdated.observe(this) { isUpdate ->

            if (isUpdate) {
                mAdapter!!.removeAll()
                binding!!.shimmer.visibility = View.VISIBLE
                binding!!.shimmer.startShimmer()
                binding!!.buttonContainer.visibility = View.GONE
                getSwiperList()
                (activity?.application as AppineersApplication).isSwiperUpdated.postValue(false)
            }
        }

        (activity?.application as AppineersApplication).LikeSuperlikeCancelRequest.observe(this) { response ->
            if (response != null) {
                if (response.type == IConstants.EXPLORE) {
                    status = response.status
                    when (response.status) {
                        IConstants.LIKE -> {
                            swapLike()
                        }
                        IConstants.SUPERLIKE -> {
                            swapSuperLike()
                        }
                        IConstants.REJECT -> {
                            swapReject()
                        }
                    }
                } else {
                    removeByUserId(response.userId!!)
                }
                (activity?.application as AppineersApplication).LikeSuperlikeCancelRequest.postValue(null)
            }
        }
    }


    override fun onCardDragging(mydirection: Direction, ratio: Float) {

        direction = mydirection.name

    }

    override fun onCardSwiped(direction: Direction) {

        when (direction.name) {
            "Left" -> {
                if (status.isNullOrEmpty()) {
                    callLikeSuperlikeCancel(
                        mAdapter!!.getAllItems()[manager!!.topPosition - 1].userId,
                        IConstants.REJECT
                    )
                } else {
                    status = ""
                }
            }
            "Right" -> {
                if (status.isNullOrEmpty()) {
                    callLikeSuperlikeCancel(
                        mAdapter!!.getAllItems()[manager!!.topPosition - 1].userId,
                        IConstants.LIKE
                    )
                    if (mAdapter!!.getAllItems()[manager!!.topPosition - 1].isLike.equals("Yes")) {
                        showMatchPopup(mAdapter!!.getItem(manager!!.topPosition - 1))
                    }

                    var likeValue = sharedPreference.likeCount
                    sharedPreference.likeCount = likeValue + 1
                    likeHideShow()
                } else {
                    status = ""
                }
            }
            "Top" -> {
                if (status.isNullOrEmpty()) {
                    callLikeSuperlikeCancel(
                        mAdapter!!.getAllItems()[manager!!.topPosition - 1].userId,
                        IConstants.SUPERLIKE
                    )
                    if (mAdapter!!.getAllItems()[manager!!.topPosition - 1].isLike.equals("Yes")) {
                        showMatchPopup(mAdapter!!.getItem(manager!!.topPosition - 1))
                    }
                    var superLikeCountValue = sharedPreference.superLikeCount
                    sharedPreference.superLikeCount = superLikeCountValue + 1
                    likeHideShow()
                } else {
                    status = ""
                }
            }
        }

        //  if (manager!!.topPosition == mAdapter!!.itemCount - 5) {
//            val old = mAdapter!!.getAllItems()
//            val new = old.plus(createswiperValue())
//            val callback = SwiperDiffCallback(old, new)
//            val result = DiffUtil.calculateDiff(callback)
//            mAdapter!!.addAllItem(new)
//            result.dispatchUpdatesTo(mAdapter!!)
        //  }

        if (manager!!.topPosition == mAdapter!!.itemCount) {
            binding!!.relEmptyMessage.visibility = View.VISIBLE
            binding!!.buttonContainer.visibility = View.GONE
        }
    }

    private fun showMatchPopup(item: SwiperViewResponse) {
        addFromFirebase(item.userId)
        MatchesDialog(item, mListener = object :
            MatchesDialog.ClickListener {
            override fun onSuccess() {
                addFromFirebase(item.userId)
                var dateFormat = SimpleDateFormat("yyyy-MM-dd")
                var date = dateFormat.format(Calendar.getInstance().getTime())
                var showDob=date?.toMMDDYYYDate()
                startActivity(ChatRoomActivity.getStartIntent(this@HomeFragment.requireContext(),item.userId!!,item!!.name,item!!.profileImage, showDob?.toMMDDYYYStr()))
            }

            override fun onCancel() {
            }

        }).show(requireFragmentManager(), "Tag")
    }



    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager!!.topPosition}")
    }


    override fun onCardCanceled() {
        when (direction) {
            "Right" -> {
                if (sharedPreference.likeCount == sharedPreference.configDetails!!.defaultLikeCount) {
                    startActivity(
                        SubscriptionPlansActivity.getStartIntent(
                            this@HomeFragment.requireContext(),
                            "1"
                        )
                    )
                }
            }
            "Top" -> {
                if (sharedPreference.superLikeCount == sharedPreference.configDetails!!.defaultSuperLikeCount) {
                    startActivity(
                        SubscriptionPlansActivity.getStartIntent(
                            this@HomeFragment.requireContext(),
                            "1"
                        )
                    )
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
        when (viewId) {
            R.id.cardSwiperView -> {
                startActivity(
                    OtherUserDetailsActivity.getStartIntent(
                        this@HomeFragment.requireContext(),
                        mAdapter!!.getItem(position).userId,
                        mAdapter!!.getItem(position),
                        IConstants.EXPLORE
                    )
                )
            }
        }
    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {
    }

    override fun onResume() {
        super.onResume()

        likeHideShow()
    }

    private fun likeHideShow() {
        val likeLimitReached =
            (sharedPreference.configDetails!!.defaultLikeCount == sharedPreference.likeCount)
        val superLikeLimitReached =
            (sharedPreference.configDetails!!.defaultSuperLikeCount == sharedPreference.superLikeCount)
        when {

            (likeLimitReached && superLikeLimitReached) -> {
                manager!!.setDirections(arrayOf(Direction.Left).toMutableList())
                binding.cardStackView.layoutManager = manager
            }
            likeLimitReached -> {
                manager!!.setDirections(arrayOf(Direction.Left, Direction.Top).toMutableList())
                binding.cardStackView.layoutManager = manager
            }

            superLikeLimitReached -> {
                manager!!.setDirections(arrayOf(Direction.Left, Direction.Right).toMutableList())
                binding.cardStackView.layoutManager = manager
            }
        }

        val count =
            sharedPreference.configDetails!!.defaultSuperLikeCount?.minus(sharedPreference.superLikeCount)
        if (count == 0) {
            binding.textSuperlikeCount.visibility = View.GONE
        } else {
            binding.textSuperlikeCount.text = count.toString()
        }

    }

    private fun addFromFirebase(userId: String?) {
        firebaseFireStoreDB.collection(chatRoomId)
            .whereArrayContains("users", sharedPreference.userDetail?.userId!!).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        if (task.result!!.size() > 0) {
                            for (document in task.result!!.documents) {
                                if (document["receiverId"] == userId || document["senderId"] == userId) {
                                    firebaseFireStoreDB.collection(chatRoomId)
                                        .document(document.id).update(
                                            mapOf(
                                                "matchStatus" to "Match",
                                            )
                                        )
                                    break
                                }
                            }
                        }
                    }
                }
            }
    }



    fun swapLike() {
        val like = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Slow.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager!!.setSwipeAnimationSetting(like)
        binding.cardStackView.swipe()
    }

    fun swapSuperLike() {
        val superlike = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Top)
            .setDuration(Duration.Slow.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager!!.setSwipeAnimationSetting(superlike)
        binding.cardStackView.swipe()
    }

    fun swapReject() {
        val close = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Left)
            .setDuration(Duration.Slow.duration)
            .setInterpolator(LinearInterpolator())
            .build()
        manager!!.setSwipeAnimationSetting(close)
        binding.cardStackView.swipe()
    }
}

