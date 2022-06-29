package com.app.signme.view.chat

import android.content.Context
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.getJsonDataFromAsset
import com.app.signme.dagger.components.FragmentComponent
import com.app.signme.databinding.FragmentChatBinding
import com.app.signme.dataclasses.ChatListData
import com.app.signme.core.BaseFragment
import com.app.signme.view.settings.SettingsActivity
import com.app.signme.viewModel.ChatViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatFragment : BaseFragment<ChatViewModel>(), ChatClickListener {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatListAdapter: ChatListAdapter
    // Selected subscription plan
    private var selectedChatMessage: ChatListData? = null

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
        loadChatList()
    }
    /**
     * Function to init recyclerview
     */
    private fun initRecycleView() {
        chatListAdapter = ChatListAdapter(
            /* offerDate = user?.offerDate!!.toBoolean(),*/
            context = this@ChatFragment.requireContext(),
            list = ArrayList<ChatListData>(),
            chatClickListener = this@ChatFragment.requireContext()
        )
        val layoutManager = LinearLayoutManager(this@ChatFragment.requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rcvChatList.setHasFixedSize(true)
        binding.rcvChatList.layoutManager = layoutManager
        binding.rcvChatList.adapter = chatListAdapter
    }
    /**
     * Load chat list from json data
     */
    private fun loadChatList() {
        var chatList: ArrayList<ChatListData> = ArrayList()
        chatList =
            getJsonListDataFromAsset(this@ChatFragment.requireContext(), "chat_messages_list.json")
        setChatListData(chatList)
    }
    /*Subcrption plan json file parsing in array list*/
    private fun getJsonListDataFromAsset(
        context: Context,
        fileName: String
    ): ArrayList<ChatListData> {
        val jsonFileString = getJsonDataFromAsset(context, fileName)
        Log.i("data", jsonFileString.toString())
        val gson = Gson()
        val listChat = object : TypeToken<ArrayList<ChatListData>>() {}.type
        var chats: ArrayList<ChatListData> =
            gson.fromJson(jsonFileString, listChat)
        chats.forEachIndexed { idx, review -> Log.i("data", "> Item $idx:\n$review") }
        return chats
    }


    /**
     * Set Chat List Data
     * @param it ArrayList<ChatListData>
     */
    private fun setChatListData(it: ArrayList<ChatListData>) {
        if (it.isNotEmpty()) {
            showData()
        } else {
            showNoData()
        }
        chatListAdapter.list = it
        chatListAdapter.notifyDataSetChanged()
    }

    private fun showData() {
        binding.rcvChatList.visibility = View.VISIBLE

    }

    private fun showNoData() {
        binding.rcvChatList.visibility = View.GONE

    }

    override fun onChatClick(data: ChatListData) {
        selectedChatMessage = data
    }

}