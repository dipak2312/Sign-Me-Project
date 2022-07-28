package com.app.signme.view.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.adapter.ChatMessageListAdapter
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getFormattedDate
import com.app.signme.commonUtils.utility.extension.sharedPreference
import com.app.signme.core.BaseActivity
import com.app.signme.dagger.components.ActivityComponent
import com.app.signme.databinding.ActivityChatRoomBinding
import com.app.signme.dataclasses.ChatMessage
import com.app.signme.view.CustomDialog
import com.app.signme.view.bottomsheet.AbusiveReportBSD
import com.app.signme.viewModel.ChatViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.bottom_sheet_report_block_user.view.*
import java.util.*
import kotlin.math.log

class ChatRoomActivity:BaseActivity<ChatViewModel>() {
    var binding:ActivityChatRoomBinding?=null
    private var isBlocked: Boolean = false

    companion object {
        /**
         * Start intent to open [ChatRoomActivity] with selected menus.
         * @param mContext Context
         * @param otherUserId
         * @param otherUserName
         * @param otherUserImage
         * All above parm is Other user.
         * @return Intent
         */
        fun getStartIntent(
            context: Context,
            otherUserId: String,
            otherUserName: String?,
            otherUserImage: String?
        ): Intent {
            return Intent(context, ChatRoomActivity::class.java).apply {
                putExtra(IConstants.BUNDLE_DATA_USER_2_ID, otherUserId)
                putExtra(IConstants.BUNDLE_DATA_USER_2_NAME, otherUserName)
                putExtra(IConstants.BUNDLE_DATA_USER_2_IMAGE, otherUserImage)
            }
        }
    }

    private val TAG: String? = ChatRoomActivity::class.java.simpleName
    private var currentChatDocument: String = ""
    private val auth = FirebaseAuth.getInstance()
    private val firebaseFireStoreDB = FirebaseFirestore.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private var chatMessageListAdapter: ChatMessageListAdapter? = null

    private var user1Name: String = ""
    private var user1ImgUrl: String = ""
    var user1UserID: String = ""
    private var user1UID: String = ""
    private var user2Name: String = ""
    private var user2ImgUrl: String = ""
    private var user2UserID: String = ""
    private var user2UID: String = ""
    private var isUser2Active: Boolean = false
    var blockedBy = ""
    var action = ""
    var msgCount: Long = 0
    private var chatRoomId: String = "Chats"
    private var chatDocId: String = ""
    var originalUser1Id = ""
    var originalUser2Id = ""
    private var chatRegistration: ListenerRegistration? = null
    private var chatMessagesList: ArrayList<ChatMessage> = arrayListOf()
    private var authUserGlobal: FirebaseUser? = null
    private var chatRoomFound = false
    private var isUserOnChatScreen: Boolean = false

    override fun setDataBindingLayout() {

        binding=DataBindingUtil.setContentView(this, R.layout.activity_chat_room)
        binding!!.lifecycleOwner=this
    }

    override fun injectDependencies(activityComponent: ActivityComponent) {
      activityComponent.inject(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setFireBaseAnalyticsData("id-chatscreen", "view-chatscreen", "view-chatscreen")

        intent?.apply {
            user2Name = getStringExtra(IConstants.BUNDLE_DATA_USER_2_NAME) ?: ""
            user2UserID = getStringExtra(IConstants.BUNDLE_DATA_USER_2_ID) ?: ""
            user2ImgUrl = getStringExtra(IConstants.BUNDLE_DATA_USER_2_IMAGE) ?: ""
            originalUser2Id = getStringExtra(IConstants.BUNDLE_DATA_USER_2_ID) ?: ""
            binding!!.tvUserName.text = user2Name

        }

        if (user2UserID == "") {
            Toast.makeText(
                this@ChatRoomActivity,
                "Sorry Something went wrong..Please try again..",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val user = sharedPreference.userDetail
        if (user != null) {
            user1Name = user.getFullName()
            user1ImgUrl = user.profileImage!!
            user1UserID = user.userId!!
            originalUser1Id = user.userId
        }
        initListeners()
        addObservers()
        when {
            checkInternet() -> {
                showProgressDialog(
                    isCheckNetwork = true,
                    isSetTitle = false,
                    title = IConstants.EMPTY_LOADING_MSG
                )
                initChat()
            }
        }
    }

    private fun initChat() {
        var user = auth.currentUser;
        if (user != null) {
            setUpUserForChat()
        } else {
            auth.signInAnonymously().addOnCompleteListener(this@ChatRoomActivity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
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
        val uidMap = hashMapOf(
            "UID" to authUser?.uid
        )
        val usersCollectionRef =
            firebaseFireStoreDB.collection("Users").document(originalUser1Id).set(uidMap)

        user1UID = authUser?.uid.toString()
        authUserGlobal = authUser
        setUserOnlineOffline(true)

        val usersDocRef = firebaseFireStoreDB.collection("Users").document(originalUser2Id)
        usersDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val documentData = document.data
                if (documentData?.get("UID") != null) {
                    user2UID = documentData["UID"] as String
                }
                checkUserOnline()
                checkUserIsTyping()
                checkUserBlockedYou()
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                Log.i(TAG, "DocumentSnapshot: " + documentData)
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            hideProgressDialog()
            Log.d(TAG, "get failed with ", exception)
        }
        initRecyclerView()
        loadChatRoom()

    }

    private fun loadChatRoom() {
        firebaseFireStoreDB.collection(chatRoomId)
            .whereArrayContains("users", user1UserID).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        if (task.result!!.size() > 0) {
                            for (document in task.result!!.documents) {
                                if (document["receiverId"] == user2UserID || document["senderId"] == user2UserID) {
                                    chatRoomFound = true
                                    loadChat(document.reference)
                                    break
                                }
                            }
                            if (!chatRoomFound) {
                                createNewChatRoom()
                                Log.d(TAG, "room doesn't exist create a new room")
                            }

                        } else {
                            createNewChatRoom()
                            Log.d(TAG, "room doesn't exist create a new room")
                        }
                    } else {
                        hideProgressDialog()
                        Log.d(TAG, "room doesn't exist create a new room")
                    }
                } else {
                    hideProgressDialog()
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            }
    }


    private fun createNewChatRoom() {
        val map = HashMap<String, Any>()
        val array = arrayOf(user1UserID, user2UserID)
        map["users"] = Arrays.asList(*array)
        map["friendStatus"] = "Friend"
        map["lastMessage"] = ""
        map["created"] = Timestamp.now()
        map["docId"] = ""
        map["id"] = UUID.randomUUID().toString()
        map["receiverId"] = user2UserID
        map["receiverName"] = user2Name
        map["receiverProfileImage"] = user2ImgUrl
        map["senderFireBaseId"] = user1UID
        map["senderId"] = user1UserID
        map["senderImage"] = user1ImgUrl
        map["senderName"] = user1Name
        map["isTyping"] = ""
        map["deletedBy"] = ""
        map["chatID"] = user2UserID
        map["isConnected"] = ""
        map["user2FirebaseUID"] = if (user2UID.isNullOrEmpty()) "" else user2UID
        map["user1FirebaseUID"] = user1UID

        firebaseFireStoreDB.collection(chatRoomId)
            .add(map)
            .addOnSuccessListener { documentReference ->
                if (documentReference != null) {
                    loadChat(documentReference)
                    documentReference.update("docId", documentReference.id.toString())
                    Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                    hideProgressDialog()
                } else {
                    hideProgressDialog()
                    Log.d(TAG, "DocumentSnapshot NULL:")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                hideProgressDialog()
            }
    }

    private fun loadChat(documentReference: DocumentReference) {
        hideProgressDialog()
        currentChatDocument = documentReference.id
        chatRegistration = firebaseFireStoreDB.collection(chatRoomId).document(currentChatDocument)
            .collection("thread").orderBy("created")
            .addSnapshotListener { messageSnapshot, _ ->
                if (messageSnapshot == null || messageSnapshot.isEmpty)
                    return@addSnapshotListener
                chatMessagesList.clear()
                var chatTimeStamp = ""
                for (messageDocument in messageSnapshot.documents) {
                    val newTimeStamp=(messageDocument["created"] as Timestamp?)?.toDate()?.getFormattedDate()
                    var isShowTime=false
                    if (chatTimeStamp!=newTimeStamp){
                        chatTimeStamp= "$newTimeStamp" ?:""
                        isShowTime=true
                    }
                    chatMessagesList.add(
                        ChatMessage(
                            id = messageDocument["id"] as String?,
                            created = messageDocument["created"] as Timestamp?,
                            content = messageDocument["content"] as String? ?: "",
                            isLog = messageDocument["isLog"] as Boolean? ?: false,
                            url = messageDocument["url"] as String? ?: "",
                            senderID = messageDocument["senderID"] as String?,
                            senderName = messageDocument["senderName"] as String?,
                            isShowTime=isShowTime,
                            chatTimeStamp = chatTimeStamp
                        )
                    )
                    Log.i(TAG, "loadChat: " + messageDocument)
                }
                binding!!.rvMessageList.adapter?.notifyDataSetChanged()
                binding!!.rvMessageList.smoothScrollToPosition(chatMessageListAdapter!!.itemCount)
            }
        Log.d(TAG, "loadChat:" + chatMessagesList.toString())
        chatMessageListAdapter?.chatMessagesList = chatMessagesList
        chatMessageListAdapter?.notifyDataSetChanged()

    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        binding!!.rvMessageList.layoutManager = linearLayoutManager

        chatMessageListAdapter = ChatMessageListAdapter(
            chatMessagesList = emptyList(),
            userId = sharedPreference.userDetail?.userId!!,
            user1ImgUrl = user1ImgUrl,
            user2ImgUrl = user2ImgUrl,
            activity = this@ChatRoomActivity,
            clickCallbackListener = object : ChatMessageListAdapter.ClickCallbackListener {
                override fun onMessageClick(data: ChatMessage, contentType: String) {
                    if (!data.id.isNullOrEmpty()) {
                        var message =
                            getString(R.string.alert_delete_message) + " " + "Message" + "?"

                        CustomDialog(
                            title = getString(R.string.app_name),
                            message = message,
                            positiveButtonText = getString(R.string.label_yes_button),
                            negativeButtonText = getString(R.string.label_no_button),
                            cancellable = true,
                            mListener = object : CustomDialog.ClickListener {
                                override fun onSuccess() {
                                    deleteMessage(data.id)
                                }

                                override fun onCancel() {
                                }

                            }).show(supportFragmentManager, "tag")

                    }
                }

            }
        )

        binding!!.rvMessageList.adapter = chatMessageListAdapter
        chatMessageListAdapter!!.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                Log.i(TAG, "onItemRangeInserted: " + chatMessageListAdapter!!.itemCount)
                binding!!.rvMessageList.smoothScrollToPosition(chatMessageListAdapter!!.itemCount);
            }
        })

        chatMessageListAdapter!!.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                //  binding.rvMessageList.smoothScrollToPosition(chatMessageListAdapter!!.itemCount);
                Log.i(TAG, "onChanged: ")
            }
        })
    }

    private fun deleteMessage(messageId: String?) {
        chatMessageListAdapter?.itemCount?.let {
            firebaseFireStoreDB.collection(chatRoomId).document(currentChatDocument)
                .collection("thread").get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            if (task.result!!.size() > 0) {
                                Log.d(TAG, "MESSAGE ID :$messageId")
                                for (doc in task.result!!.documents) {
                                    Log.d(TAG, "DOC ID :" + doc["id"])
                                    if (doc["id"] == messageId) {
                                        doc?.reference?.delete()
                                    }
                                }
                            }
                        }
                    }
                }

            var lastMessage = ""
             if (chatMessageListAdapter?.itemCount!! > 1) {
                lastMessage =
                    if (chatMessageListAdapter?.chatMessagesList?.get(it - 1)?.content.toString()
                            .isEmpty()
                    ) {
                        "Image"
                    } else {
                        chatMessageListAdapter?.chatMessagesList?.get(it - 1)?.content.toString()
                    }
                Log.d(TAG, "LAST MESSAGE :$lastMessage")

            }
        }

    }

    private fun setUserOnlineOffline(isOnline: Boolean = false) {
        val myConnectionsRef = firebaseDatabase.getReference("users/$originalUser1Id/isConnected")
        myConnectionsRef.setValue(isOnline)
        // When this device disconnects, remove it
        myConnectionsRef.onDisconnect().setValue(java.lang.Boolean.FALSE)

    }


    private fun checkUserOnline() {
        val myConnectionsRef =
            firebaseDatabase.getReference("users/$originalUser2Id/isConnected")
        myConnectionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var connected = false
                if (snapshot.value != null) {
                    connected = snapshot.value as Boolean
                }
                if (connected) {
                    isUser2Active = true
                    binding!!.userStatus.visibility = View.VISIBLE
                    Log.d(TAG, "Connected")
                } else {
                    isUser2Active = false
                    binding!!.userStatus.visibility = View.INVISIBLE
                    Log.d(TAG, "Not Connected")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Listener was cancelled at .info/connected")
            }
        })
    }

    private fun checkUserIsTyping() {
        val myConnectionsRef =
            firebaseDatabase.getReference("users/$user2UID/$originalUser1Id/isTyping")
        myConnectionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isTyping = false
                if (snapshot.value != null) {
                    isTyping = snapshot.value as Boolean
                }
                if (isTyping) {
                    binding!!.tvTyping.visibility = View.VISIBLE
                    Log.d(TAG, "Typing")
                } else {
                    binding!!.tvTyping.visibility = View.INVISIBLE
                    Log.d(TAG, "Not Typing")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i(TAG, "onCancelled: " + error.toString())
                Log.w(TAG, "Listener was cancelled at .isTyping")
            }
        })
    }


    private fun checkUserBlockedYou() {
        val myConnectionsRef =
            firebaseDatabase.getReference("users/$user2UID/$originalUser1Id/isBlocked")
        myConnectionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isBlocked = false
                if (snapshot.value != null) {
                    isBlocked = snapshot.value as Boolean
                }

                if (!isBlocked) {
                    binding!!.llBlockedUserLayout.visibility = View.GONE
                    binding!!.linSendChat.visibility = View.VISIBLE
                    blockedBy = if (blockedBy.equals(IConstants.ME)) blockedBy else ""
                    Log.d(TAG, "Not Blocked")


                } else {
                    binding!!.linSendChat.visibility = View.GONE
                    binding!!.llBlockedUserLayout.visibility = View.VISIBLE
                    blockedBy = IConstants.OTHER
                    Log.d(TAG, "Blocked")
                }
                checkUserBlockedByMe()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Listener was cancelled at .isBlocked")
            }
        })
    }

    private fun checkUserBlockedByMe() {
        val myConnectionsRef =
            firebaseDatabase.getReference("users/$user1UID/$originalUser2Id/isBlocked")
        myConnectionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isBlocked = false
                if (snapshot.value != null) {
                    isBlocked = snapshot.value as Boolean
                }
                if (isBlocked) {
                    binding!!.textBlockedAlert.setText(R.string.label_message_blocked_by_me)
                    binding!!.linSendChat.visibility = View.GONE
                    binding!!.llBlockedUserLayout.visibility = View.VISIBLE
                    blockedBy = IConstants.ME
                    Log.d(TAG, "Blocked by me")
                } else {
                    blockedBy = if (blockedBy.equals(IConstants.OTHER)) blockedBy else ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Listener was cancelled at .isBlocked")
            }
        })


    }


    private fun initListeners() {
        binding?.apply {
            btnBack.setOnClickListener {
                setFireBaseAnalyticsData("id_btnback", "click_btnback", "click_btnback")
                finish()
            }

            btnSend.setOnClickListener {

                setFireBaseAnalyticsData("id-sendchat", "click_sendchat", "click_sendchat")
                if (!etChatMessage.text.isNullOrEmpty()) {
                    sendChatMessage(binding!!.etChatMessage.text.toString(), false)
                }
            }

            ivMoreOption.setOnClickListener {
                setFireBaseAnalyticsData("id-menu", "click_menu", "click_menu")
                val dialog = BottomSheetDialog(this@ChatRoomActivity)
                val bottomSheet =
                    layoutInflater.inflate(R.layout.bottom_sheet_report_block_user, null)
                //Just remove "blockedBy.equals(IConstants.ME)" if u want Unblock option Here.

                bottomSheet.btnBlockUser.setText(if (blockedBy.equals(IConstants.ME)) R.string.unblock_user else R.string.block_user)
                bottomSheet.btnReportUser.setOnClickListener {
                    setFireBaseAnalyticsData(
                        "id-reportuser",
                        "click_reportuser",
                        "click_reportuser"
                    )
                    dialog.dismiss()
                    AbusiveReportBSD(
                        object : AbusiveReportBSD.ClickListener {
                            override fun onClick(viewId: Int?) {

                            }
                        },

                        "1"!!,
                        user2UserID,
                        this@ChatRoomActivity

                    ).show(supportFragmentManager, "tag")

                }
                bottomSheet.btnBlockUser.setOnClickListener {
                    setFireBaseAnalyticsData(
                        "id-blockuser",
                        "click_blockuser",
                        "click_blockuser"
                    )

                    dialog.dismiss()
                    if (blockedBy.equals(IConstants.ME)) {
                        //callUnblockUserApi()
                    } else {
                        CustomDialog(
                            message = getString(R.string.block_user_alert,user2Name),
                            positiveButtonText = getString(R.string.label_yes_button),
                            negativeButtonText = getString(R.string.label_no_button),
                            cancellable = false,
                            mListener = object : CustomDialog.ClickListener {
                                override fun onSuccess() {

                                    //callBlockUserApi("user")
                                }

                                override fun onCancel() {

                                }

                            }).show(supportFragmentManager, "tag")
                    }
                }
                bottomSheet.btnCancel.setOnClickListener{
                    dialog.dismiss()
                }

                dialog.setContentView(bottomSheet)
                dialog.show()
            }



            etChatMessage.doAfterTextChanged { it ->
                if (it!!.isNotEmpty()) {
                    val docRef = firebaseFireStoreDB.collection(chatRoomId)
                        .document(currentChatDocument)
                    docRef.update(
                        mapOf(
                            "isTyping" to user1UID,
                            "senderFireBaseId" to user1UID,
                            "chatID" to user2UserID
                        )
                    ).addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                    }
                        .addOnFailureListener { e ->

                        }

                } else {
                    val docRef = firebaseFireStoreDB.collection(chatRoomId)
                        .document(currentChatDocument)
                    docRef.update(
                        mapOf(
                            "isTyping" to "",
                            "senderFireBaseId" to user1UID,
                            "chatID" to user2UserID
                        )
                    ).addOnSuccessListener {
                        Log.d(
                            TAG,
                            "DocumentSnapshot successfully updated!"
                        )
                    }
                        .addOnFailureListener { e ->
                            Log.w(
                                TAG,
                                "Error updating document",
                                e
                            )
                        }

                }
                val database = FirebaseDatabase.getInstance()
                val myConnectionsRef =
                    database.getReference("users/$user1UID/$user2UserID/isTyping")
                myConnectionsRef.setValue(it.isNotEmpty())
            }
        }
    }

    private fun sendChatMessage(message: String, isLog: Boolean) {
        if (message.isNotEmpty()) {
            binding!!.etChatMessage.setText("")

            val chatMessageWithoutImage = hashMapOf(
                "id" to UUID.randomUUID().toString(),
                "content" to message.trim(),
                "created" to Timestamp.now(),
                "senderID" to user1UserID,
                "senderName" to user1Name,
                "isLog" to isLog
            )

            if (!currentChatDocument.isNullOrEmpty()) {
                firebaseFireStoreDB.collection(chatRoomId).document(currentChatDocument)
                    .collection("thread")
                    .add(
                        chatMessageWithoutImage
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (task.result != null) {
                                if (isUserOnChatScreen) {
                                    msgCount = 0L
                                } else {
                                    msgCount++
                                    firebaseDatabase.reference
                                        .child("users/$currentChatDocument/$user2UserID/readCount")
                                        .setValue(msgCount)
                                }

                                val docRef = firebaseFireStoreDB.collection(chatRoomId)
                                    .document(currentChatDocument)
                                docRef.update(
                                    mapOf(
                                        "created" to chatMessageWithoutImage["created"],
                                        "lastMessage" to chatMessageWithoutImage["content"],
                                        "id" to chatMessageWithoutImage["id"],
                                        "receiverId" to user2UserID,
                                        "receiverName" to user2Name,
                                        "receiverProfileImage" to user2ImgUrl,
                                        "senderName" to user1Name,
                                        "senderImage" to user1ImgUrl,
                                        "senderId" to user1UserID,
                                        "senderFireBaseId" to user1UID,
                                        "isLog" to isLog,
                                        user2UserID + "_readCount" to msgCount,
                                    )
                                ).addOnSuccessListener {
                                    Log.d(
                                        TAG,
                                        "DocumentSnapshot successfully updated!"
                                    )
                                    binding!!.rvMessageList.smoothScrollToPosition(
                                        chatMessageListAdapter!!.itemCount
                                    )
                                    if (!isUser2Active) {
                                        val map = HashMap<String, String>()
                                        map["receiver_id"] = originalUser2Id
                                        map["m_message_id"] = docRef.id
                                        map["message"] = message
                                        //viewModel.callSendChatNotificationApi(map = map)
                                    }
                                }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            TAG,
                                            "Error updating document",
                                            e
                                        )
                                    }

                                Log.d(TAG, task.result.id)

                            } else {
                                Log.d(TAG, "new message not added")
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.exception)
                        }
                    }
            }
        }
    }
    private fun addObservers() {

        viewModel.statusCodeLiveData.observe(this) { serverError ->
            hideProgressDialog()
            handleApiStatusCodeError(serverError)
        }
    }

}