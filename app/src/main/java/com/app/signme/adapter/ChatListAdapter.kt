package com.app.signme.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.RoundedCornersTransformation
import com.app.signme.databinding.CvChatMessagesItemBinding
import com.app.signme.dataclasses.ChatRoom
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hb.logger.Logger
import java.util.*

class ChatListAdapter(onRecyclerClick: RecyclerViewActionListener,mActivity: Activity) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    //Initialize the logger library to take logs from user actions.
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    //Interface use to capture user actions i.e click & loadMore items.
    val mOnRecyclerClick = onRecyclerClick

    //Original data Array List
    var mOriginalData = java.util.ArrayList<ChatRoom>()

    //Data Array List after filter applied.
    var mResultData = java.util.ArrayList<ChatRoom>()

    //How much total items we want to load on this adapter.
    var totalCount: String = "0"

    //Next page number. This useful while pagination.
    var nextPage: Int = 1

    var userId: String? = null
    val mActivity = mActivity
    private val mFilter = ItemFilter()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CvChatMessagesItemBinding.inflate(inflater)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.root.setLayoutParams(lp)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(mResultData[position])


    fun getItem(index: Int): ChatRoom {
        return mResultData[index]
    }

    fun getAllItems(): ArrayList<ChatRoom> {
        return mResultData
    }

    fun addItem(data: ChatRoom) {
        mOriginalData.add(data)
        mResultData.add(data)
        val index = mOriginalData.indexOf(data)
        notifyItemInserted(index)
    }

    fun addAllItem(data: List<ChatRoom>) {
        mOriginalData.addAll(data)
        mResultData.addAll(data)
        notifyDataSetChanged()
    }

    fun removeItem(index: Int) {
        mOriginalData.removeAt(index)
        notifyItemRemoved(index)
    }


    fun removeAll() {
        mOriginalData.clear()
        mResultData.clear()
        nextPage = 1
        notifyDataSetChanged()
    }

    fun replaceItem(index: Int, item: ChatRoom) {
        mOriginalData.set(index, item)
        notifyItemChanged(index)
    }

    fun removeItem(data: ChatRoom) {
        val index = mOriginalData.indexOf(data)
        mOriginalData.remove(data)
        notifyItemRemoved(index)
    }

    override fun getItemCount() = mResultData.size

    fun getFilter(): Filter {
        return mFilter
    }

    inner class ViewHolder(val binding: CvChatMessagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatRoom) {
            with(binding) {
                binding.chat = item

                binding.tvReceiverName.text =
                    if (userId != item.receiverId) item.receiverName else item.senderName


                Glide.with(binding.root.context)
                    .load(if (userId != item.receiverId) item.receiverProfileImage else item.senderImage)
                    .error(R.drawable.ic_profile_img)
                    .placeholder(R.drawable.ic_profile_img)
                    .apply(
                        RequestOptions.bitmapTransform(
                            RoundedCornersTransformation(binding.root.context, 20, 0)
                        )
                    )
                    .into(binding.sivChatImage)

                if (item.matchStatus.equals("Match"))
                {
                    binding.tvReceiverName.setTextColor(mActivity.resources.getColor(R.color.white))
                    binding.tvLastMessage.setTextColor(mActivity.resources.getColor(R.color.white))
                }
                else
                { binding.tvReceiverName.setTextColor(mActivity.resources.getColor(R.color.app_color_line))
                    binding.tvLastMessage.setTextColor(mActivity.resources.getColor(R.color.app_color_line))

                }

                binding.tvLastMessage.text = item.lastMessage
                val database = FirebaseDatabase.getInstance()
                val myConnectionsRef =
                    database.getReference("users/${item.senderFireBaseId}/${item.chatID}/isTyping")
                myConnectionsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var connected = false
                        if (snapshot.value != null) {
                            connected = snapshot.value as Boolean
                        }
                        if (connected) {
                            binding.tvTyping.visibility = View.VISIBLE
                            binding.tvLastMessage.visibility = View.GONE
                        } else {
                            binding.tvTyping.visibility = View.GONE
                            binding.tvLastMessage.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

                binding.mLayoutRoot.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Root View Click")
                    mOnRecyclerClick.onItemClick(mLayoutRoot.id, adapterPosition, null)
                }
                binding.executePendingBindings()

                if (layoutPosition == (mOriginalData.size - 1) && mOriginalData.size < totalCount.toInt()) {
                    mOnRecyclerClick.onLoadMore(itemCount = mOriginalData.size, nextPage = nextPage)
                }
            }
        }
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterString = constraint.toString().toLowerCase(Locale.ROOT)
            val results = FilterResults()
            val count: Int = mOriginalData.size
            val tempFilterList: ArrayList<ChatRoom> = ArrayList<ChatRoom>(count)
            var filterableString: String
            for (i in 0 until count) {
                filterableString =
                    if (userId != mOriginalData.get(i).receiverId) mOriginalData.get(i).receiverName!! else mOriginalData.get(
                        i
                    ).senderName!!
                if (filterableString.toLowerCase(Locale.ROOT).contains(filterString)) {
                    tempFilterList.add(mOriginalData.get(i))
                }
            }
            results.values = tempFilterList
            results.count = tempFilterList.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            mResultData.clear()
            mResultData = results.values as java.util.ArrayList<ChatRoom>
            notifyDataSetChanged()
        }
    }
}