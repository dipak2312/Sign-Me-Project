package com.app.signme.view.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.signme.R
import com.app.signme.adapter.UserNotificationAdapter
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getJsonDataFromAsset
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityNotificationBinding
import com.app.signme.dataclasses.Notification
import com.app.signme.core.BaseActivity
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.view.CustomDialog
import com.app.signme.view.chat.ChatRoomActivity
import com.app.signme.view.home.OtherUserDetailsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.NotificationViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import com.tsuryo.swipeablerv.SwipeLeftRightCallback
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity : BaseActivity<NotificationViewModel>(),
    RecyclerViewActionListener {

    private var dataBinding: ActivityNotificationBinding? = null

    //recyclerview2
    var mAdapter: UserNotificationAdapter? = null
    var notificationId: String? = ""
    var notificationCount: Int? = 0
    var deletenotifyPosition: Int = 0
    var notificationPosition = 0

    companion object {
        const val UPDATE_REQUEST_STATUS = 111
        const val TAG = "NotificationActivity"
        fun getStartIntent(mContext: Context): Intent {
            return Intent(mContext, NotificationActivity::class.java).apply {
            }
        }
    }

    override fun setDataBindingLayout() {
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        dataBinding?.viewModel = viewModel
        dataBinding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        //initListeners()

        setFireBaseAnalyticsData(
            "id-notificationscreen",
            "view-notificationscreen",
            "view-notificationscreen"
        )
        mAdapter = UserNotificationAdapter(this, this)
        dataBinding!!.mRecyclerView.adapter = mAdapter
        mAdapter!!.removeAll()
        initListeners()
        addObservers()
        callGetNotification()
        callGetNotificationCount()
        swipeDeleteOnLeftRight()
    }

    private fun initListeners() {
        dataBinding?.apply {

            swipeContainer.setOnRefreshListener {
                when {
                    checkInternet() -> {
                        this@NotificationActivity.viewModel.callGetNotification()
                    }
                }
            }
            ibtnBack.setOnClickListener {
                setFireBaseAnalyticsData("id_btnback", "click_btnback", "click_btnback")
                finish()
            }

        }
    }


    //recyclerview2
    fun callGetNotification() {
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                viewModel.callGetNotification()


            }
        }
    }


    fun callGetNotificationCount() {
        when {
            checkInternet() -> {

                viewModel.callGetNotificationCount()


            }
        }
    }

    private fun addObservers() {
        viewModel.NotificationLiveData.observe(this, Observer { it ->
            hideProgressBar()
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                if (it.data != null) {
                    dataBinding!!.relEmptyScreen.visibility = View.GONE
                    if (dataBinding!!.swipeContainer.isRefreshing) {
                        mAdapter!!.removeAll()
                    }
                    mAdapter?.totalCount = it.settings?.count!!
                    mAdapter?.nextPage = mAdapter!!.nextPage + 1

                    mAdapter!!.addAllItem(it.data!!)

                    dataBinding!!.swipeContainer.isRefreshing = false

                }
            }
        })

        viewModel.NotificationCountLiveData.observe(this, Observer { response ->
            hideProgressDialog()
            if (response.settings?.isSuccess == true) {
                MSCGenerator.addAction(
                    GenConstants.ENTITY_APP,
                    GenConstants.ENTITY_USER,
                    "count added success"
                )
                if (response.data!!.size > 0) {
                    val notificationCount = response.data!![0].notifyCount
                    (application as AppineersApplication).notificationsCount.postValue(
                        notificationCount
                    )
                    sharedPreference.notifyCount = notificationCount
                } else {
                    sharedPreference.notifyCount = "0"
                }
            }
        })

        viewModel.deleteUserNotificationLiveData.observe(this, Observer {

            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {
                if (mAdapter!!.getAllItems().size != 0) {
                    mAdapter!!.removeItem(deletenotifyPosition)
                    MSCGenerator.addAction(
                        GenConstants.ENTITY_APP,
                        GenConstants.ENTITY_USER,
                        "deleted successfuly"
                    )
                    callGetNotificationCount()
                    showMessage(it.settings!!.message, IConstants.SNAKBAR_TYPE_SUCCESS)
                }

                if (mAdapter!!.getAllItems().size == 0) {
                    showProgressDialog(
                        isCheckNetwork = true,
                        isSetTitle = false,
                        title = IConstants.EMPTY_LOADING_MSG
                    )
                    callGetNotification()
                }
            }

        })
        viewModel.readNotificationLiveData.observe(this, Observer {
            hideProgressDialog()
            if (it?.settings?.isSuccess == true) {

                callGetNotificationCount()
            }
        })


        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            dataBinding!!.swipeContainer.isRefreshing = false
            if (serverError.code == 500 && serverError.success == "0") {
                dataBinding!!.relEmptyScreen.visibility = View.VISIBLE
            } else {
                handleApiStatusCodeError(serverError)
            }

        }
    }


    private fun hideProgressBar() {
        dataBinding!!.progressBar.visibility = View.GONE
    }

    private fun setMessage(message: String?) {
        if (message?.isNotEmpty()!! && mAdapter!!.getAllItems().size == 0) {
            dataBinding!!.textHideNoti.setText(message)
            dataBinding!!.textHideNoti.visibility = View.VISIBLE
        } else {
            dataBinding!!.textHideNoti.visibility = View.GONE
        }
    }


    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {
        when (viewId) {
            R.id.mLayoutRoot -> {
                val notification = mAdapter?.getItem(position)
                notificationPosition = position
                notificationId = mAdapter!!.getAllItems()[position].notificationId!!

                if (!mAdapter!!.getAllItems()[position].notificationStatus.equals("Read")) {
                    val map = HashMap<String, String>()
                    map["notification_id"] = notificationId.toString()
                    map["status"] = "read"
                    viewModel.readNotification(map)
                }

                when (notification?.redirectionType) {
                    IConstants.NOTIFICATION_TYPES.Like -> {
                        callOtherDetails("Yes",position,IConstants.LIKE)
                    }

                    IConstants.NOTIFICATION_TYPES.Superlike -> {

                        callOtherDetails("Yes",position,IConstants.SUPERLIKE)
                    }
                    IConstants.NOTIFICATION_TYPES.Match -> {
                        callOtherDetails("No",position,IConstants.MATCHES)
                    }
                }
            }
        }
    }

    fun callOtherDetails(isLike:String,position: Int,status:String){
        var otherUserResponse= SwiperViewResponse(isLike = isLike,name = mAdapter!!.getItem(position).senderFirstName,profileImage = mAdapter!!.getItem(position).senderProfileImage)
        startActivity(OtherUserDetailsActivity.getStartIntent(this@NotificationActivity,mAdapter!!.getItem(position).senderId,otherUserResponse,status))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UPDATE_REQUEST_STATUS -> {

//                val updatedNotification = mAdapter!!.getItem(notificationPosition)
//                updatedNotification.notificationStatus = "read"
//                mAdapter!!.replaceItem(notificationPosition, updatedNotification)

            }
        }
    }

    //delete notification

    private fun swipeDeleteOnLeftRight() {
        dataBinding!!.mRecyclerView.setListener(object : SwipeLeftRightCallback.Listener {
            override fun onSwipedLeft(position: Int) {
                deletenotifyPosition = position
                openDialogToDelete(position)
                mAdapter!!.notifyDataSetChanged()
            }

            override fun onSwipedRight(position: Int) {

            }
        })

    }


    fun openDialogToDelete(position: Int) {
        CustomDialog(
            title = getString(R.string.app_name),
            message = getString(R.string.alert_delete_message),
            positiveButtonText = getString(R.string.label_yes_button),
            negativeButtonText = getString(R.string.label_no_button),
            cancellable = true,
            mListener = object : CustomDialog.ClickListener {
                override fun onSuccess() {
                    val userId = mAdapter!!.getAllItems()[position].notificationId!!

                    showProgressDialog(
                        isCheckNetwork = true,
                        isSetTitle = false,
                        title = IConstants.EMPTY_LOADING_MSG
                    )
                    val map = HashMap<String, String>()
                    map["notification_id"] = userId
                    viewModel.getDeleteNotificationCall(userId)
                }

                override fun onCancel() {

                }

            }).show(supportFragmentManager, "tag")
    }



    override fun onLoadMore(itemCount: Int, nextPage: Int) {

    }
}