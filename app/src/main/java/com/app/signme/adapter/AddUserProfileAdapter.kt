package com.app.signme.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.databinding.ItemUserProfileBinding
import com.app.signme.dataclasses.ProfileImageModel
import com.app.signme.dataclasses.UserImage
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hb.logger.Logger
import java.util.ArrayList

class AddUserProfileAdapter(onClick: RecyclerViewActionListener, mActivity: Activity) :
    RecyclerView.Adapter<AddUserProfileAdapter.ViewHolder>() {

    //Initialize the logger library to take logs from user actions.
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    //Interface use to capture user actions i.e click & loadMore items.
    val mOnRecyclerClick = onClick

    //Original data Array List
    var mOriginalData = java.util.ArrayList<UserImage>()

    //Data Array List after filter applied.
    var mResultData = java.util.ArrayList<UserImage>()

    //How much total items we want to load on this adapter.
    var totalCount: String = "0"

    //Next page number. This useful while pagination.
    var nextPage: Int = 1

    //Activity context to do any action at activity level.
    val mActivity = mActivity


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemUserProfileBinding.inflate(inflater)
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

    fun getItem(index: Int): UserImage {
        return mOriginalData[index]
    }

    fun getAllItems(): ArrayList<UserImage> {
        return mOriginalData
    }


    fun addItem(data: UserImage) {

        mOriginalData.add(data)
        mResultData.add(data)
        val index = mOriginalData.indexOf(data)
        notifyItemInserted(index)
    }
    fun addAllItem(data: List<UserImage>) {
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

    fun replaceItem(index: Int, item: UserImage) {
        mOriginalData.set(index, item)
        notifyItemChanged(index)
    }

    fun removeItem(data: UserImage) {
        val index = mOriginalData.indexOf(data)
        mOriginalData.remove(data)
        notifyItemRemoved(index)
    }

    override fun getItemCount() = mOriginalData.size

    inner class ViewHolder(val binding: ItemUserProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserImage) {
              binding.model=item

            if (!item.imageUri.equals("") && item.imageUrl.equals("")) {
                binding.progressBar.visibility =
                    if (item.uploadStatus == IConstants.IN_PROGRESS) View.VISIBLE else View.GONE
                binding.ibtnRemoveImage.visibility =
                    if (item.uploadStatus == IConstants.PENDING) View.VISIBLE else View.GONE
            }

            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.ic_feedback_bag)
                .placeholder(R.drawable.ic_feedback_bag)
                .into(binding.ivFeedbackImage)

//            val initials = getInitials(item)
//            binding.tvInitials.text = initials


            binding.ibtnAddImage.setOnClickListener{
                mOnRecyclerClick.onItemClick(binding.ibtnAddImage.id, adapterPosition, null)
            }
            binding.ibtnRemoveImage.setOnClickListener{
                mOnRecyclerClick.onItemClick(binding.ibtnRemoveImage.id, adapterPosition, null)
            }

            binding.ivRetry.setOnClickListener {
                logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Retry Button Click")
                mOnRecyclerClick.onItemClick(binding.ivRetry.id, adapterPosition, null)
            }

            binding.executePendingBindings()

//            if (layoutPosition == (mOriginalData.size - 1) && mOriginalData.size < totalCount.toInt()) {
//                mOnRecyclerClick.onLoadMore(
//                    itemCount = mOriginalData.size,
//                    nextPage = nextPage
//                )
//            }
        }
    }
}