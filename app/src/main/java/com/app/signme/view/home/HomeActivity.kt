package com.app.signme.view.home


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.app.signme.R
import com.app.signme.application.AppineersApplication
import com.app.signme.application.AppineersApplication.Companion.sharedPreference
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.toMMDDYYYDate
import com.app.signme.commonUtils.utility.extension.toMMDDYYYStr
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityHomeBinding
import com.app.signme.dataclasses.Social
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.dataclasses.response.GoogleReceipt
import com.app.signme.view.chat.ChatFragment
import com.app.signme.view.Matches.MatchesFragment
import com.app.signme.view.chat.ChatRoomActivity
import com.app.signme.view.profile.ProfileFragment
import com.app.signme.view.subscription.SubscriptionPlansActivity
import com.app.signme.viewModel.HomeViewModel
import com.google.gson.Gson
import com.hb.logger.msc.MSCGenerator
import com.hb.logger.msc.core.GenConstants
import org.json.JSONObject


class HomeActivity : BaseActivity<HomeViewModel>() {
    private lateinit var binding: ActivityHomeBinding
    object TAGS {
        val HOME = "home"
        val Matches = "matches"
        val Messages = "messages"
        var Profile = "profile"
    }

    companion object {
        const val TAG = "HomeActivity"

        fun getStartIntent(mContext: Context, payload: String?): Intent {
            return Intent(mContext, HomeActivity::class.java).apply {
                putExtra(IConstants.PARAM_NOTIFICATION_PAYLOAD, payload)
            }
        }
    }

    private var social: Social? = null
    val fm: FragmentManager = supportFragmentManager
    val homeFragment: Fragment = HomeFragment()
    val matchesFragment: Fragment = MatchesFragment()
    val messagesFragment: Fragment = ChatFragment()
    val profileFragment: Fragment = ProfileFragment()
    var active: Fragment = homeFragment
    var isTabChanging = false
    var payload: String? = null
    private var lastSelectedTab: Int? = null

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.lifecycleOwner = this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        social = intent.getParcelableExtra("social")
        if (social != null) {
            sharedPreference.socialUserDetails = social
        }

        if (intent != null && intent.extras != null) {
            Log.i(TAG, "setupView: " + Gson().toJson(intent.extras))
            payload = intent.getStringExtra(IConstants.PARAM_NOTIFICATION_PAYLOAD)
            lastSelectedTab = intent.getIntExtra(IConstants.BUNDLE_TAB_ID, R.id.action_explore)
            //checkNotificationsData(data)
        }



        setFireBaseAnalyticsData("id-homeScreen", "view_homeScreen", "view_homeScreen")
        binding.apply {
            bottomNavigation.setOnNavigationItemSelectedListener {
                if (isTabChanging) {
                    return@setOnNavigationItemSelectedListener false
                }
                isTabChanging = true
                setCurrentFragment(it.itemId)
            }
        }

        if (lastSelectedTab != null && lastSelectedTab != R.id.action_explore) {
            setCurrentFragment(lastSelectedTab!!)
        } else {
            setFragment(homeFragment, TAGS.HOME, 0)
        }
        //(application as AppineersApplication).isActiveFilter.postValue(false)
        addObservers()
        callGetNotificationCount()
        checkNotificationsData()
    }

    private fun checkNotificationsData() {
        if (payload != null) {
            val jsonObject = JSONObject(payload)
            //Log.i(TAG, "checkNotificationsData:checkNotificationsData:checkNotificationsData:checkNotificationsData: " + jsonObject)
            val notificationType = jsonObject.getString(IConstants.PARAM_NOTIFICATION_TYPE)
            val name = jsonObject.getString(IConstants.PARAM_SENDER_NAME)
            val sender_id = jsonObject.getString(IConstants.PARAM_SENDER_ID)
            val profile = jsonObject.getString(IConstants.PARAM_SENDER_PROFILE)
            val matchDate = jsonObject.getString(IConstants.MATCH_DATE)
            logger.dumpCustomEvent(
                IConstants.EVENT_NOTIFICATION_PAYLOAD,
                Gson().toJson(jsonObject)
            )
            Log.i(TAG, "checkNotificationsData: " + jsonObject)
            when (notificationType) {

                IConstants.NOTIFICATION_TYPES.Like -> {
                    if(sharedPreference.isSubscription)
                    {
                        callOtherDetails("Yes",IConstants.LIKE,name,profile,sender_id)
                    }
                    else
                    {
                        startActivity(SubscriptionPlansActivity.getStartIntent(this@HomeActivity,"1"))

                    }

                }
                IConstants.NOTIFICATION_TYPES.Message -> {
                    var matchdate=matchDate?.toMMDDYYYDate()
                    startActivity(
                        ChatRoomActivity.getStartIntent(
                            this@HomeActivity,
                            sender_id,
                            name,
                            profile,
                            matchdate?.toMMDDYYYStr()
                        )
                    )
                }

                IConstants.NOTIFICATION_TYPES.Superlike -> {
                    callOtherDetails("Yes",IConstants.SUPERLIKE,name,profile,sender_id)
                }
                IConstants.NOTIFICATION_TYPES.Match -> {
                    callOtherDetails("No",IConstants.MATCHES,name,profile,sender_id)
                }
            }
        }
    }


    fun callOtherDetails(isLike:String,status:String,userName:String,profileImage:String,userId:String){
        var otherUserResponse= SwiperViewResponse(isLike = isLike,name = userName,profileImage =profileImage)
        startActivity(OtherUserDetailsActivity.getStartIntent(this@HomeActivity,userId,otherUserResponse,status))
    }



    fun callGetNotificationCount() {

        viewModel.callGetNotificationCount()
    }

    /**
     * Set the fragment in frame container by Id
     */
    private fun setCurrentFragment(tabId: Int): Boolean {
        return when (tabId) {
            R.id.action_explore -> {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Home Tab CLick")
                setFireBaseAnalyticsData(
                    "id-userHomeTab",
                    "click_userHomeTab",
                    "click_userHomeTab"
                )
                setFragment(homeFragment, TAGS.HOME, 0)
                true
            }
            R.id.action_matches -> {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Sessions Tab CLick")
                setFireBaseAnalyticsData(
                    "id-sessionTab",
                    "click_sessionTab",
                    "click_sessionTab"
                )
                setFragment(matchesFragment, TAGS.Matches, 1)
                true
            }
            R.id.action_chat -> {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Feeds Tab Click")
                setFireBaseAnalyticsData(
                    "id-feedsTab",
                    "click_feedsTab",
                    "click_feedsTab"
                )
                setFragment(messagesFragment, TAGS.Messages, 2)
                true
            }
            R.id.action_profile -> {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Favorite Tab Click")
                setFireBaseAnalyticsData(
                    "id-favoriteTab",
                    "click_favoriteTab",
                    "click_favoriteTab"
                )
                setFragment(profileFragment, TAGS.Profile, 3)
                true
            }

            else -> false
        }
    }

    private fun addObservers() {

        viewModel.notificationCountLiveData.observe(this, androidx.lifecycle.Observer { response ->
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
        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()

            if (serverError.code == 500 || serverError.code == 404) {
            }
            else {
                handleApiStatusCodeError(serverError)
            }
        }

    }


    fun setFragment(fragment: Fragment, tag: String?, position: Int) {
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        val curFrag: Fragment? = fm.getPrimaryNavigationFragment()
        if (curFrag != null) {
            fragmentTransaction.hide(curFrag)
        }
        if (fragment.isAdded) {
            fragmentTransaction.show(fragment)
        } else {
            fragmentTransaction.add(R.id.frameContainer, fragment, tag)
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragment)
        fragmentTransaction.setReorderingAllowed(true)
        fragmentTransaction.commitNowAllowingStateLoss()
        binding.bottomNavigation.getMenu().getItem(position).setChecked(true)
        active = fragment
        isTabChanging = false
    }
}