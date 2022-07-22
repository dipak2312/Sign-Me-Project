package com.app.signme.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.databinding.ItemMatchesBinding
import com.app.signme.databinding.ItemSuperLikesProfileBinding
import com.app.signme.dataclasses.MatchesResponse
import com.app.signme.dataclasses.SuperLikesResponse
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hb.logger.Logger
import java.util.ArrayList

class SuperLikesAdapter (onClick: RecyclerViewActionListener, mActivity: Activity) :
    RecyclerView.Adapter<SuperLikesAdapter.ViewHolder>() {

    //Initialize the logger library to take logs from user actions.
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }

    //Interface use to capture user actions i.e click & loadMore items.
    val mOnRecyclerClick = onClick

    //Original data Array List
    var mOriginalData = java.util.ArrayList<SuperLikesResponse>()

    //Data Array List after filter applied.
    var mResultData = java.util.ArrayList<SuperLikesResponse>()

    //How much total items we want to load on this adapter.
    var totalCount: String = "0"

    //Next page number. This useful while pagination.
    var nextPage: Int = 1

    //Activity context to do any action at activity level.
    val mActivity = mActivity


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSuperLikesProfileBinding.inflate(inflater)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.root.setLayoutParams(lp)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mOriginalData[position])
    }

    fun getItem(index: Int): SuperLikesResponse {
        return mOriginalData[index]
    }

    fun getAllItems(): ArrayList<SuperLikesResponse> {
        return mOriginalData
    }


    fun addItem(data: SuperLikesResponse) {

        mOriginalData.add(data)
        mResultData.add(data)
        val index = mOriginalData.indexOf(data)
        notifyItemInserted(index)
    }
    fun addAllItem(data: List<SuperLikesResponse>) {
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

    fun replaceItem(index: Int, item: SuperLikesResponse) {
        mOriginalData.set(index, item)
        notifyItemChanged(index)
    }

    fun removeItem(data: SuperLikesResponse) {
        val index = mOriginalData.indexOf(data)
        mOriginalData.remove(data)
        notifyItemRemoved(index)
    }

    override fun getItemCount() = mOriginalData.size

    inner class ViewHolder(val binding: ItemSuperLikesProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SuperLikesResponse) {
           // binding.model=item

            if(totalCount.toInt()>4 && adapterPosition==3)
            {
                binding.textsupelike.visibility= View.VISIBLE
                binding.textsupelike.text="+"+totalCount.toInt().minus(4).toString()
            }
            else
            {
                binding.textsupelike.visibility= View.GONE
            }


            Glide.with(binding.root.context)
                .load(item.profileImage)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.ic_profile)
                .placeholder(R.drawable.ic_profile)
                .into(binding.superlikeProfile)

            binding.imgSuperlike.setOnClickListener{
                mOnRecyclerClick.onItemClick(binding.imgSuperlike.id, adapterPosition, null)
            }

             binding.executePendingBindings()


        }
    }
}