package com.app.signme.view.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.app.signme.R
import com.app.signme.adapter.BlockedUsersAdapter
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.showSnackBar
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityBlokedUserBinding
import com.app.signme.view.CustomDialog
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BlokedUserActivity : BaseActivity<SettingsViewModel>(), RecyclerViewActionListener {


    //Used for data binding
    private var binding: ActivityBlokedUserBinding? = null
    private val auth = FirebaseAuth.getInstance()
    //Use this adapter to hold the element i.e. restaurants in recyclerview
    val mAdapter: BlockedUsersAdapter = BlockedUsersAdapter(this, this)
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    var lastClickedPosition = 0

    private var user1UID: String = ""
    private var blockedUserID: String = ""

    override fun setDataBindingLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bloked_user)
        binding?.lifecycleOwner = this
    }

    /**
     * For dependency injection from dagger
     * Must to declare this activity in Activity Component
     * @param activityComponent provide the activity when needed by dagger
     */
    override fun injectDependencies(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    /**
     * Setup all View in this method
     */
    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData(
            "id-blockedUserScreen",
            "view_blockedUserScreen",
            "view_blockedUserScreen"
        )

        binding?.apply {
            mRecyclerView.adapter = mAdapter
        }

        initListeners()
        callGetBlockedUser()
    }

    private fun callGetBlockedUser() {
        when {
            checkInternet() -> {
                showProgressDialog(isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG)
                viewModel.callGetBlockedUser()
            }
        }
    }

    private fun initListeners() {
        binding?.let {
            with(it) {
                btnBack.setOnClickListener {
                    setFireBaseAnalyticsData("id-blockUserscreen", "view-blockUserscreen", "view-blockUserscreen")
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Back Button Click")
                    finish()
                }
                swipeContainer.setOnRefreshListener {
                    mAdapter!!.removeAll()
                    callGetBlockedUser()
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

    /**
     * This method is basically used for to declare all observer
     */
    override fun setupObservers() {
        super.setupObservers()
        viewModel.blockedUserLiveData.observe(this, Observer { it ->
            hideProgressDialog()
            hideProgressBar()
            binding!!.swipeContainer.isRefreshing = false
            if (it?.settings?.isSuccess == true) {
                if (binding?.swipeContainer!!.isRefreshing) {
                    mAdapter.removeAll()
                }
                if (it.data!!.size > 0) {
                    //  mAdapter.totalCount = it.settings?.totalCount!!
                    mAdapter.nextPage = mAdapter.nextPage + 1
                    for (item in it.data!!) {
                        mAdapter.addItem(item)
                    }
                }
                setMessage(it.settings!!.message)
            }
            else
            {
                binding!!.relEmptyScreen.visibility=View.VISIBLE
            }
        })

        viewModel.unblockUserLiveData.observe(this) {
            hideProgressDialog()
            hideProgressBar()
            binding!!.swipeContainer.isRefreshing = false

            if (it?.settings?.isSuccess == true) {
                it.settings?.message?.showSnackBar(this, IConstants.SNAKBAR_TYPE_SUCCESS)
                mAdapter.removeItem(lastClickedPosition)
                handleFirebaseChatSettings()
                blockedUserID = ""
                if (mAdapter.getAllItems().size == 0) {
                    mAdapter.nextPage = 1
                    showProgressDialog(isCheckNetwork = true,
                        isSetTitle = false,
                        title = IConstants.EMPTY_LOADING_MSG)

                    callGetBlockedUser()
                }
            }
        }

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            binding!!.swipeContainer.isRefreshing = false
            handleApiStatusCodeError(serverError)
        }


    }
    /**
     * This method show the message
     * @param message to show message
     * @return null
     */
    private fun setMessage(message: String?) {
        if (message?.isNotEmpty()!! && mAdapter.getAllItems().size == 0) {
            binding!!.textMessage.setText(message)
            binding!!.textMessage.visibility = View.VISIBLE
        } else {
            binding!!.textMessage.visibility = View.GONE
        }

        binding?.swipeContainer?.isRefreshing = false
        binding!!.progressBar.visibility = View.GONE
    }
    /**
     * This method is use for callback from recyclerview adapter
     * When user click on an view from list then this method call
     * @param viewId is id of that clicked view
     * @param position is position of clicked element
     * @param childPosition is useful while nested list i.e. element have list of item
     */
    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {
        when (viewId) {
            R.id.mLayoutRoot -> {

            }

            R.id.btnUnblockUser -> {
                setFireBaseAnalyticsData("id-unblockuser", "click_unblockuser", "click_unblockuser")
                CustomDialog(
                    message = getString(R.string.unblock_user_alert),
                    positiveButtonText = getString(R.string.label_yes_button),
                    negativeButtonText = getString(R.string.label_no_button),
                    cancellable = false,
                    mListener = object : CustomDialog.ClickListener {
                        override fun onSuccess() {
                            lastClickedPosition = position
                            blockedUserID = mAdapter.getItem(position).userId!!
                            showProgressDialog(isCheckNetwork = true,
                                isSetTitle = false,
                                title = IConstants.EMPTY_LOADING_MSG)
                            when {
                                checkInternet() -> {
                                    viewModel.callBlockUser(
                                        mAdapter.getItem(position).userId!!,
                                        "1"
                                    )

                                }
                            }
                        }

                        override fun onCancel() {

                        }

                    }).show(supportFragmentManager, "tag")

            }
        }
    }

    /**
     * This method is called while recycler view reached nearest to last element
     * Default threshold value is 5. Means this method called when last 5th element visible
     * @param itemCount show the current element count in recycler view
     * @param nextPage indicate the next page index
     */
    override fun onLoadMore(itemCount: Int, nextPage: Int) {
        showProgressBar()
        callGetBlockedUser()

    }


    private fun handleFirebaseChatSettings() {
        var user = auth.currentUser;
        if (user != null) {
            user1UID = user.uid
        } else {
            auth.signInAnonymously().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInAnonymously:success")
                    val authUser = auth.currentUser
                    user1UID = authUser?.uid.toString()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("", "signInAnonymously:failure", task.exception)
                }
            }
        }
        if (blockedUserID.isNotEmpty() && user1UID.isNotEmpty()) {
            val isBlockedRef =
                firebaseDatabase.getReference("users/$user1UID/$blockedUserID/isBlocked")
            isBlockedRef.setValue(false)
        }
    }

}