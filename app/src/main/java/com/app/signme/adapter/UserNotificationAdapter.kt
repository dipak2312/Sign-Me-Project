package com.app.signme.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.RoundedCornersTransformation
import com.app.signme.commonUtils.utility.extension.covertTimeToText
import com.app.signme.databinding.ItemNotificationBinding
import com.app.signme.dataclasses.UserNotification
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hb.logger.Logger
import java.util.ArrayList

class UserNotificationAdapter(onClick: RecyclerViewActionListener, mActivity: Activity) :
    RecyclerView.Adapter<UserNotificationAdapter.ViewHolder>() {


    //Initialize the logger library to take logs from user actions.
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    //Interface use to capture user actions i.e click & loadMore items.
    val mOnRecyclerClick = onClick

    //Original data Array List
    var mOriginalData = java.util.ArrayList<UserNotification>()

    //Data Array List after filter applied.
    var mResultData = java.util.ArrayList<UserNotification>()

    //How much total items we want to load on this adapter.
    var totalCount: String = "0"

    //Next page number. This useful while pagination.
    var nextPage: Int = 1

    //Activity context to do any action at activity level.
    val mActivity = mActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNotificationBinding.inflate(inflater)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.root.setLayoutParams(lp)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mOriginalData[position])
    }

    fun getItem(index: Int): UserNotification {
        return mOriginalData[index]
    }

    fun getAllItems(): ArrayList<UserNotification> {
        return mOriginalData
    }

    fun addItem(data: UserNotification) {
        mOriginalData.add(data)
        mResultData.add(data)
        val index = mOriginalData.indexOf(data)
        notifyItemInserted(index)
    }

    fun addAllItem(data: List<UserNotification>) {
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

    fun replaceItem(index: Int, item: UserNotification) {
        mOriginalData.set(index, item)
        notifyItemChanged(index)
    }

    fun removeItem(data: UserNotification) {
        val index = mOriginalData.indexOf(data)
        mOriginalData.remove(data)
        notifyItemRemoved(index)
    }

    override fun getItemCount() = mOriginalData.size


    inner class ViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserNotification) {

            binding.notification = item

            if (item.notificationStatus.equals("Read"))
            {
                binding.storyDisplayNick.setTextColor(mActivity.resources.getColor(R.color.app_color_line))
                binding.textNotiDate.setTextColor(mActivity.resources.getColor(R.color.app_color_line))
            }
            else
            {
                binding.storyDisplayNick.setTextColor(mActivity.resources.getColor(R.color.white))
                binding.textNotiDate.setTextColor(mActivity.resources.getColor(R.color.white))
            }

            Glide.with(binding.root.context)
                .load(item.senderProfileImage)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(R.drawable.ic_profile_img)
                .placeholder(R.drawable.ic_profile_img)
                .apply(
                    RequestOptions.bitmapTransform(
                        RoundedCornersTransformation(binding.root.context, 0, 0)
                    )
                )
                .into(binding.sivNotificationImage)

            binding.mLayoutRoot.setOnClickListener {
                binding.storyDisplayNick.setTextColor(mActivity.resources.getColor(R.color.app_color_line))
                binding.textNotiDate.setTextColor(mActivity.resources.getColor(R.color.app_color_line))
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Root View Click")
                mOnRecyclerClick.onItemClick(  binding.mLayoutRoot.id, adapterPosition, null)
            }


            binding.textNotiDate.text=covertTimeToText(item.createdAt)
            binding.executePendingBindings()

            if (layoutPosition == (mOriginalData.size - 1) && mOriginalData.size < totalCount.toInt()) {
                mOnRecyclerClick.onLoadMore(
                    itemCount = mOriginalData.size,
                    nextPage = nextPage
                )
            }

        }

    }


}