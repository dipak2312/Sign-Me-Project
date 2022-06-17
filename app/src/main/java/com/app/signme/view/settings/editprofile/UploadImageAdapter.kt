package com.app.signme.view.settings.editprofile


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.extension.loadImage
import com.app.signme.databinding.CvListItemUploadImageBinding
import com.app.signme.dataclasses.UserImage
import com.hb.logger.Logger

/**
 * @author Mahesh Lipane
 * This adapter use to hold the Event menu items.
 * This adapter hold the [com.nightlifee.app.dataclasses.FeedbackImageModel] type of items.
 */
class UploadImageAdapter(
    onRecyclerClick: RecyclerViewActionListener
) :
    RecyclerView.Adapter<UploadImageAdapter.ViewHolder>() {
    val logger by lazy {
        Logger(this::class.java.simpleName)
    }
    val mOnRecyclerClick = onRecyclerClick
    var mOriginalData = java.util.ArrayList<UserImage>()
    var mResultData = java.util.ArrayList<UserImage>()
    var nextPage: Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CvListItemUploadImageBinding.inflate(inflater)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.root.layoutParams = lp
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(mOriginalData[position])


    fun getItem(index: Int): UserImage {
        return mResultData[index]
    }

    fun getAllItems(): ArrayList<UserImage> {
        return mResultData
    }

    /*fun addItem(indexAt: Int, data: UserImage) {
        mOriginalData.add(indexAt, data)
        mResultData.add(indexAt, data)
        val index = mOriginalData.indexOf(data)
        notifyItemInserted(index)
    }*/

    fun addItem(data: UserImage) {
        mOriginalData.add(data)
        mResultData.add(data)
        val index = mOriginalData.indexOf(data)
        notifyItemInserted(index)
    }

   /* fun addAllItem(data: List<UserImage>) {
        mOriginalData.addAll(data)
        mResultData.addAll(data)
        notifyDataSetChanged()
    }*/

    fun removeItem(index: Int) {
        mOriginalData.removeAt(index)
        mResultData.removeAt(index)
        notifyItemRemoved(index)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun removeAll() {
        mOriginalData.clear()
        mResultData.clear()
        nextPage = 1
        notifyDataSetChanged()
    }

  /*  fun replaceItem(index: Int, item: UserImage) {
        mOriginalData.set(index, item)
        notifyItemChanged(index)
    }

    fun removeItem(data: UserImage) {
        val index = mOriginalData.indexOf(data)
        mOriginalData.remove(data)
        notifyItemRemoved(index)
    }

    /**
     * Remove deleted card from list
     */
    fun removeImage(image: String?) {
        if (image?.toInt() != 0) {
            mOriginalData.removeAt(image?.toInt()!!)
            mResultData.removeAt(image.toInt())
            notifyItemRemoved(image.toInt())
        }
    }
    */
    override fun getItemCount() = mOriginalData.size


    inner class ViewHolder(val binding: CvListItemUploadImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserImage) {
            with(binding) {
                binding.model = item
                binding.ivRetry.visibility = View.GONE
                if (!item.imageUri.equals("") && item.imageUrl.equals("")) {
                    binding.ivRetry.visibility =
                        if (item.uploadStatus == IConstants.PENDING) View.VISIBLE else View.GONE
                    binding.progressBar.visibility =
                        if (item.uploadStatus == IConstants.IN_PROGRESS) View.VISIBLE else View.GONE
                    binding.ibtnRemoveImage.visibility =
                        if (item.uploadStatus == IConstants.PENDING) View.VISIBLE else View.GONE
                    loadImage(binding.ivFeedbackImage, item.imageUri)

                }
                if (item.imageUri.equals("")) {
                    loadImage(binding.ivFeedbackImage, item.imageUrl)
                } else {
                    loadImage(binding.ivFeedbackImage, item.imageUri)
                }

                binding.ibtnAddImage.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Add Image Button Click")
                    mOnRecyclerClick.onItemClick(ibtnAddImage.id, adapterPosition, null)
                }
                binding.ivFeedbackImage.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Image View Click")
                    mOnRecyclerClick.onItemClick(ivFeedbackImage.id, adapterPosition, null)
                }
                binding.ibtnRemoveImage.setOnClickListener {
                    logger.dumpCustomEvent(IConstants.EVENT_CLICK, "Remove Image Button Click")
                    mOnRecyclerClick.onItemClick(ibtnRemoveImage.id, adapterPosition, null)
                }
                binding.executePendingBindings()
            }
        }
    }

}