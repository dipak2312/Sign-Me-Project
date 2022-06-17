package com.app.signme.view.notification

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getJsonDataFromAsset
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityNotificationBinding
import com.app.signme.dataclasses.Notification
import com.app.signme.core.BaseActivity
import com.app.signme.viewModel.NotificationViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity : BaseActivity<NotificationViewModel>(), NotificationClickListener {

    private var binding: ActivityNotificationBinding? = null
    private lateinit var notificationListAdapter: NotificationListAdapter
    // Selected subscription plan
    private var selectedNotification: Notification? = null


    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }
    override fun setupView(savedInstanceState: Bundle?) {
        initListeners()


    }

    private fun initListeners() {
        binding?.let {
            with(it, {

                ibtnBack.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                     finish()
                }
            })
        }
        tvClearAll.setOnClickListener{
            clearAllNotifications()
        }
        initRecycleView()
        loadNotificationList()
    }

    private fun initRecycleView() {
        notificationListAdapter = NotificationListAdapter(
            /* offerDate = user?.offerDate!!.toBoolean(),*/
            context = this@NotificationActivity,
            list = ArrayList<Notification>(),
            notificationListener = this@NotificationActivity
        )
        val layoutManager = LinearLayoutManager(this@NotificationActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding?.rcvNotificationList?.setHasFixedSize(true)
        binding?.rcvNotificationList?.layoutManager = layoutManager
        binding?.rcvNotificationList?.adapter = notificationListAdapter
    }

    private fun loadNotificationList() {
        var notificationList: ArrayList<Notification> = ArrayList()
        notificationList =
            getJsonListDataFromAsset(this@NotificationActivity, "push_notification_list.json")
        setNotificationListData(notificationList)
    }
    /*Subcrption plan json file parsing in array list*/
    private fun getJsonListDataFromAsset(
        context: Context,
        fileName: String
    ): ArrayList<Notification> {
        val jsonFileString = getJsonDataFromAsset(context, fileName)
        Log.i("data", jsonFileString.toString())
        val gson = Gson()
        val listReviewsType = object : TypeToken<ArrayList<Notification>>() {}.type
        var reviews: ArrayList<Notification> =
            gson.fromJson(jsonFileString, listReviewsType)
        reviews.forEachIndexed { idx, review -> Log.i("data", "> Item $idx:\n$review") }
        return reviews
    }
    private fun clearAllNotifications()
    {
        showNoData()
    }

    /**
     * Set Entertainment List Data
     * @param it ArrayList<Entertainment>
     */
    private fun setNotificationListData(it: ArrayList<Notification>) {
        if (it.isNotEmpty()) {
            showData()
        } else {
            showNoData()
        }
        notificationListAdapter.list = it
        notificationListAdapter.notifyDataSetChanged()
    }

    private fun showData() {
        binding?.rcvNotificationList?.visibility = View.VISIBLE
        binding?.llNoNotifications?.visibility = View.GONE
    }

    private fun showNoData() {
        binding?.rcvNotificationList?.visibility = View.GONE
        binding?.llNoNotifications?.visibility = View.VISIBLE
    }
    override fun onNotificationClick(data: Notification) {
        selectedNotification = data
    }
}