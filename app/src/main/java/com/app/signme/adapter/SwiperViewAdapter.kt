package com.app.signme.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.databinding.ItemSwiperViewBinding
import com.app.signme.dataclasses.SwiperViewResponse
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hb.logger.Logger
import java.util.ArrayList

class SwiperViewAdapter (onClick: RecyclerViewActionListener, mActivity: Activity) :
    RecyclerView.Adapter<SwiperViewAdapter.ViewHolder>() {

    //Initialize the logger library to take logs from user actions.
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    //Interface use to capture user actions i.e click & loadMore items.
    val mOnRecyclerClick = onClick

    //Original data Array List
    var mOriginalData = java.util.ArrayList<SwiperViewResponse>()

    //Data Array List after filter applied.
    var mResultData = java.util.ArrayList<SwiperViewResponse>()

    //How much total items we want to load on this adapter.
    var totalCount: String = "0"

    //Next page number. This useful while pagination.
    var nextPage: Int = 1

    //Activity context to do any action at activity level.
    val mActivity = mActivity


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSwiperViewBinding.inflate(inflater)

        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: SwiperViewAdapter.ViewHolder, position: Int) {
        holder.bind(mOriginalData[position])
    }

    fun getItem(index: Int): SwiperViewResponse {
        return mOriginalData[index]
    }

    fun getAllItems(): ArrayList<SwiperViewResponse> {
        return mOriginalData
    }


    fun addItem(data: SwiperViewResponse) {

        mOriginalData.add(data)
        mResultData.add(data)
        val index = mOriginalData.indexOf(data)
        notifyItemInserted(index)
    }
    fun addAllItem(data: List<SwiperViewResponse>) {
        mOriginalData.addAll(data)
        mResultData.addAll(data)
       // notifyDataSetChanged()
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

    fun replaceItem(index: Int, item: SwiperViewResponse) {
        mOriginalData.set(index, item)
        notifyItemChanged(index)
    }

    fun removeItem(data: SwiperViewResponse) {
        val index = mOriginalData.indexOf(data)
        mOriginalData.remove(data)
        notifyItemRemoved(index)
    }

    override fun getItemCount() = mOriginalData.size

    inner class ViewHolder(val binding: ItemSwiperViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SwiperViewResponse) {

            binding.response=item
            Glide.with(binding.root.context)
                .load(item.profileImage)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.ic_no_image)
                .placeholder(R.drawable.ic_empty_img)
                .into(binding.itemImage)

            Glide.with(binding.root.context)
                .load(item.signLogo)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .into(binding.signLogo)

            binding.cardSwiperView.setOnClickListener{
                mOnRecyclerClick.onItemClick(binding.cardSwiperView.id, adapterPosition, null)
            }

              binding.executePendingBindings()

        }
    }

 }