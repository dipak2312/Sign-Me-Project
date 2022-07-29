package com.app.signme.view.chat

import android.content.Context
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.signme.R
import com.app.signme.adapter.ChatListAdapter
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getJsonDataFromAsset
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentChatBinding
import com.app.signme.dataclasses.ChatListData
import com.app.signme.core.BaseFragment
import com.app.signme.dataclasses.ChatRoom
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.viewModel.ChatViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatFragment : BaseFragment<ChatViewModel>(), RecyclerViewActionListener {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatListAdapter: ChatListAdapter
    // Selected subscription plan
    private var selectedChatMessage: ChatListData? = null
    private val auth = FirebaseAuth.getInstance()
    private val firebaseFireStoreDB = FirebaseFirestore.getInstance()
    var chatRoomId: String = "Chats"
    private var user1Name: String = ""
    private var user1ImgUrl: String = ""
    private var user1UserID: String = ""
    private var chatRoomList: ArrayList<ChatRoom> = arrayListOf()
    private var chatRegistration: ListenerRegistration? = null
    private var authUserGlobal: FirebaseUser? = null
    private var user1UID: String = ""

    override fun setDataBindingLayout() {}

    override fun provideLayoutId(): Int {
        return R.layout.fragment_chat
    }

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupView(view: View) {
        binding = DataBindingUtil.bind(view)!!
        binding.lifecycleOwner = this
        val user = sharedPreference.userDetail
        if (user != null) {
            user1Name = user.getFullName()
            user1ImgUrl = user.profileImage!!
            user1UserID = user.userId!!
        }
        initListeners()
    }
    private fun initListeners() {
        binding?.let {
            with(it) {

                btnSettings.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Setting Button Click")
                    startActivity(SettingsActivity.getStartIntent(this@ChatFragment.requireContext()))
                }
            }
        }

        initRecycleView()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatRegistration?.remove()
        if (authUserGlobal != null) {
            setUserOnlineOffline(false)
        }
        Log.d("FRAGMENT_CHAT", "ON_DESTROY_CALLED")

    }

    override fun onPause() {
        super.onPause()
        if (authUserGlobal != null) {
            setUserOnlineOffline(false)
        }
        Log.d("FRAGMENT_CHAT", "ON_PAUSED_CALLED")

    }

    override fun onResume() {
        super.onResume()
        initChat()
        if (authUserGlobal != null) {
            setUserOnlineOffline(true)
        }
        Log.d("FRAGMENT_CHAT", "ON_RESUME_CALLED")
    }

    private fun setUserOnlineOffline(isOnline: Boolean = false) {
        val database = FirebaseDatabase.getInstance()
        val myConnectionsRef = database.getReference("users/$user1UserID/isConnected")
        myConnectionsRef.setValue(isOnline)
        // When this device disconnects, remove it
        myConnectionsRef.onDisconnect().setValue(java.lang.Boolean.FALSE)
    }
    private fun initChat() {
        val user = auth.currentUser;
        if (user != null) {
            setUpUserForChat()
        } else {
            auth.signInAnonymously()
                .addOnCompleteListener(mBaseActivity!!) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInAnonymously:success")
                        setUpUserForChat()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("", "signInAnonymously:failure", task.exception)
                    }
                }
          }
    }

    private fun setUpUserForChat() {
        val authUser = auth.currentUser
        user1UID = authUser?.uid.toString()
        authUserGlobal = authUser
        setUserOnlineOffline(true)
        initRecycleView()
        getChatRooms()
    }

    private fun getChatRooms() {
        chatRegistration =
            firebaseFireStoreDB.collection(chatRoomId).whereArrayContains("users", user1UserID)
                .addSnapshotListener { chatRoomSnapshot, e ->
                    if (e != null) {
                        Log.w("TAG", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (chatRoomSnapshot == null || chatRoomSnapshot.isEmpty)
                        return@addSnapshotListener
                    chatRoomList.clear()
                    val myUserId=sharedPreference.userDetail?.userId!!
                    for (chatRoomDocument in chatRoomSnapshot.documents) {
                        if ((chatRoomDocument.data?.get("lastMessage") as String).isNotEmpty()) {
                                chatRoomList.add(
                                    ChatRoom(
                                        id = chatRoomDocument["id"] as String?,
                                        created = chatRoomDocument["created"] as Timestamp?,
                                        senderID = chatRoomDocument["senderId"] as String?,
                                        senderName = chatRoomDocument["senderName"] as String?,
                                        senderImage = chatRoomDocument["senderImage"] as String?,
                                        receiverId = chatRoomDocument["receiverId"] as String?,
                                        receiverName = chatRoomDocument["receiverName"] as String?,
                                        receiverProfileImage = chatRoomDocument["receiverProfileImage"] as String?,
                                        lastMessage = chatRoomDocument["lastMessage"] as String?,
                                        isTyping = chatRoomDocument["isTyping"] as String?,
                                        docId = chatRoomDocument["docId"] as String?,
                                        deletedBy = chatRoomDocument["deletedBy"] as String?,
                                        senderFireBaseId = chatRoomDocument["senderFireBaseId"] as String?,
                                        chatID = chatRoomDocument["chatID"] as String?,
                                        chatCount = chatRoomDocument[myUserId+"_readCount"] as Long?,
                                        friendStatus = chatRoomDocument["friendStatus"] as String?
                                    )
                                )
                            }

                    }
                    chatRoomList.sortByDescending { it.created }
                    //chatRoomList.sortedWith(compareBy { it.created })
                    loadData()
                }
        loadData()
    }

    private fun loadData() {
        chatListAdapter!!.removeAll()
        if (chatRoomList.isNotEmpty()) {
            showData()
        } else {
            showNoData()
        }
        chatListAdapter!!.addAllItem(chatRoomList)
    }

    private fun showData() {
        binding!!.relEmptyScreen1.visibility = View.GONE
    }

    private fun showNoData() {
        binding!!.relEmptyScreen1.visibility = View.VISIBLE
    }


    /**
     * Function to init recyclerview
     */
    private fun initRecycleView() {
        chatListAdapter = ChatListAdapter(this)
        val layoutManager = LinearLayoutManager(this@ChatFragment.requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rcvChatList.setHasFixedSize(true)
        binding.rcvChatList.layoutManager = layoutManager
        binding.rcvChatList.adapter = chatListAdapter
    }



    override fun onItemClick(viewId: Int, position: Int, childPosition: Int?) {
        when(viewId){
            R.id.mLayoutRoot -> {
            setFireBaseAnalyticsData("id_btnback", "click_btnback", "click_btnback")
            val chatRoomModel = chatListAdapter!!.getItem(position)
            if (sharedPreference.userDetail?.userId!! != chatRoomModel.receiverId) {
                startActivity(
                    ChatRoomActivity.getStartIntent(
                        this@ChatFragment.requireContext(),
                        chatRoomModel.receiverId!!,
                        chatRoomModel.receiverName!!,
                        chatRoomModel.receiverProfileImage,
                        )
                )
            } else {
                startActivity(
                    ChatRoomActivity.getStartIntent(
                        this@ChatFragment.requireContext(),
                        chatRoomModel.senderID!!,
                        chatRoomModel.senderName!!,
                        chatRoomModel.senderImage
                    )
                )
            }
        }
      }
    }

    override fun onLoadMore(itemCount: Int, nextPage: Int) {

    }

}