package com.app.signme.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.RoundedCornersTransformation
import com.app.signme.databinding.ItemBlockedUserBinding
import com.app.signme.dataclasses.BlockedUser
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hb.logger.Logger

class BlockedUsersAdapter(
    onRecyclerClick: RecyclerViewActionListener,
    val mActivity: Activity
) : RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder>() {

    //Initialize the logger library to take logs from user actions.
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    //Interface use to capture user actions i.e click & loadMore items.
    val mOnRecyclerClick = onRecyclerClick

    //Original data Array List
    var mOriginalData = java.util.ArrayList<BlockedUser>()

    //Data Array List after filter applied.
    var mResultData = java.util.ArrayList<BlockedUser>()

    //How much total items we want to load on this adapter.
    var totalCount: String = "0"

    //Next page number. This useful while pagination.
    var nextPage: Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBlockedUserBinding.inflate(inflater)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.root.setLayoutParams(lp)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(mOriginalData[position])


    fun getItem(index: Int): BlockedUser {
        return mResultData[index]
    }

    fun getAllItems(): ArrayList<BlockedUser> {
        return mResultData
    }

    fun addItem(data: BlockedUser) {
        mOriginalData.add(data)
        mResultData.add(data)
        val index = mOriginalData.indexOf(data)
        notifyItemInserted(index)
    }

    fun addAllItem(data: List<BlockedUser>) {
        mOriginalData.addAll(data)
        mResultData.addAll(data)
        notifyDataSetChanged()
    }

    fun removeItem(index: Int) {
        mOriginalData.removeAt(index)
        mResultData.removeAt(index)
        notifyItemRemoved(index)
    }


    fun removeAll() {
        mOriginalData.clear()
        mResultData.clear()
        nextPage = 1
        notifyDataSetChanged()
    }

    fun replaceItem(index: Int, item: BlockedUser) {
        mOriginalData.set(index, item)
        notifyItemChanged(index)
    }

    fun removeItem(data: BlockedUser) {
        val index = mOriginalData.indexOf(data)
        mOriginalData.remove(data)
        notifyItemRemoved(index)
    }

    override fun getItemCount() = mOriginalData.size

    inner class ViewHolder(val binding: ItemBlockedUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BlockedUser) {
            with(binding) {
                binding.user = item
                Glide.with(binding.root.context)
                    .load(item.profileImage)
                    .skipMemoryCache(false)
                    .error(R.drawable.ic_profile_img)
                    .placeholder(R.drawable.ic_profile_img)
                    .apply(
                        RequestOptions.bitmapTransform(
                            RoundedCornersTransformation(binding.root.context, 20, 0)
                        )
                    ).into(binding.imageUser)

                binding.btnUnblockUser.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Unblock Button Click")
                    mOnRecyclerClick.onItemClick(btnUnblockUser.id, adapterPosition, null)
                }

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
}