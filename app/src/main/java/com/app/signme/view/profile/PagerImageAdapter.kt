package com.app.signme.view.profile


import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.app.signme.application.AppineersApplication
import com.app.signme.commonUtils.utility.IConstants
import com.app.signme.commonUtils.utility.RoundedCornersTransformation
import com.app.signme.databinding.ItemPagerImageBinding
import com.app.signme.dataclasses.UserImage
import com.app.signme.db.repo.MediaFileRepository
import com.app.signme.view.gallery.GalleryPagerActivity
import com.app.signme.view.settings.editprofile.RecyclerViewActionListener
import com.app.signme.view.settings.editprofile.UploadPostMediaService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author Anita Chipkar
 * This adapter use to hold the profile image items.
 * This adapter hold the [com.whitelabel.app.dataclasses.FeedbackImageModel] type of items.
 */
class PagerImageAdapter(
    editable: Boolean = false,
    mRecyclerViewActionListener: RecyclerViewActionListener
) :
    PagerAdapter() {

    val TAG = "CheckInImageAdapter"
    private val imageList: ArrayList<UserImage> = ArrayList()
    var sBorder = 10
    var sColor = "#7D9067"
    var sCorner = 15
    var sMargin = 0
    private var editable = editable
    private val mRecyclerViewActionListener: RecyclerViewActionListener =
        mRecyclerViewActionListener

    var mActivity: AppCompatActivity? = null
    var userId: String? = null
    var mediaFileRepository: MediaFileRepository? = null
    private var refresh = false

    override fun isViewFromObject(view: View, anyObject: Any): Boolean {
        return view == anyObject
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ItemPagerImageBinding.inflate(LayoutInflater.from(container.context))
        binding.btnRetry.visibility = View.GONE
        var mediaFileRepository: MediaFileRepository? =
            container.context?.let { MediaFileRepository.getInstance(it) }

        if (!imageList.get(position).imageUrl.equals("")) {
            binding.btnRetry.visibility = View.GONE
            Glide.with(container.context)
                .load(imageList.get(position).imageUrl)
                // .error(R.drawable.ic_theappineers_logo)
                // .placeholder(R.drawable.ic_theappineers_logo)
                .apply(
                    RequestOptions.bitmapTransform(
                        RoundedCornersTransformation(container.context, sCorner, sMargin)
                    )
                )
                .into(binding.ivGalleryImage)
            binding.btnRetry.visibility = View.GONE
        }
        if (!imageList.get(position).imageUri.equals("")) {

            Glide.with(container.context)
                .load(imageList.get(position).imageUri)
                //.error(R.drawable.ic_theappineers_logo)
                // .placeholder(R.drawable.ic_theappineers_logo)
                .apply(
                    RequestOptions.bitmapTransform(
                        RoundedCornersTransformation(container.context, sCorner, sMargin)
                    )
                )
                .into(binding.ivGalleryImage)
            binding.btnRetry.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE
        } /*else {
            when (imageList[position].uploadStatus) {
                IConstants.PENDING -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRetry.visibility = View.VISIBLE
                }
                (IConstants.IN_PROGRESS) -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.GONE
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                }
            }
*/

            /* Glide.with(container.context)
                 .load(imageList.get(position).imageUri)
                 //.error(R.drawable.ic_theappineers_logo)
                // .placeholder(R.drawable.ic_theappineers_logo)
                 .apply(
                     RequestOptions.bitmapTransform(
                         RoundedCornersTransformation(container.context, sCorner, sMargin)
                     ))
                 .into(binding.ivGalleryImage)*/
      //  }
        binding.btnRetry.setOnClickListener {
            binding.btnRetry.visibility = View.GONE
            // binding.progressBar.visibility = View.VISIBLE
            if (container.context != null) {
                mediaFileRepository = MediaFileRepository.getInstance(container.context!!)
                CoroutineScope(Dispatchers.IO).launch {
                    var localFiles = imageList.filter { it.imageUri.equals("") }
                    (mActivity?.application as AppineersApplication).isImageCout = localFiles.size
                    var count: Int = localFiles.size
                    for (filePath in localFiles) {
                        //(activity?.application as MainApplication).isImageCout=count
                        val file =
                            filePath.localImageId
                        if (file != null) {
                            startFileUploadService(
                                filePath.imageUrl!!,
                                filePath.localImageId!!,
                                position
                            )
                        } else {
                            removeItem(position)
                        }
                    }

                }

            }
        }


        if (editable) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                mRecyclerViewActionListener.onItemClick(
                    binding.btnDelete.id,
                    position,
                    null
                )
            }
        } else {
            binding.btnDelete.visibility = View.GONE
        }

        binding.ivGalleryImage.setOnClickListener {

            val selectedUrl = imageList[position].imageUrl!!
            if (selectedUrl.isEmpty()) {
                return@setOnClickListener
            }
            val images = ArrayList<String>()
            var latestPosition = 0
            for ((index, img) in imageList.withIndex()) {
                if (getFileTypeFromURL(img.imageUrl!!).equals(IConstants.PHOTO)) {
                    images.add(img.imageUrl.toString())
                    if (selectedUrl.equals(img.imageUrl.toString())) {
                        latestPosition = index
                    }
                }
            }
            ContextCompat.startActivity(
                container.context,
                GalleryPagerActivity.getStartIntent(
                    container.context,
                    images,
                    latestPosition
                ),
                null
            )


        }
        binding.executePendingBindings()
        binding.root.tag = position
        container.addView(binding.root)
        return binding.root
    }

    private fun startFileUploadService(filePath: String, fileId: String, position: Int) {
        val uploadMedia = Intent(mActivity!!, UploadPostMediaService::class.java)
        uploadMedia.putExtra(UploadPostMediaService.KEY_FILE_URI, filePath)
        uploadMedia.putExtra(UploadPostMediaService.KEY_USER_ID, userId)
        uploadMedia.putExtra(UploadPostMediaService.KEY_FILE_ID, fileId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mActivity!!.startForegroundService(uploadMedia)
        } else {
            mActivity!!.startService(uploadMedia)
        }

        val imageItem = imageList[position]
        imageItem.uploadStatus = IConstants.IN_PROGRESS
        replaceItem(position, imageItem)
    }

    /* fun refreshAdapter() {
         refresh = true
         notifyDataSetChanged()
     }*/

    /*override fun getItemPosition(`object`: Any): Int {
        return if (refresh) {
            refresh = false
            POSITION_NONE
        } else {
            super.getItemPosition(`object`)
        }
    }*/
    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE;
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View?)
    }

    fun insertItem(item: UserImage) {
        imageList.add(item)
        notifyDataSetChanged()
    }

    fun insertAllItem(items: ArrayList<UserImage>) {
        imageList.addAll(items)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        imageList.removeAt(position)
        notifyDataSetChanged()
    }

    fun removeItem(item: UserImage) {
        imageList.remove(item)
        notifyDataSetChanged()
    }

    fun replaceItem(index: Int, item: UserImage) {
        try {
            Runnable {
                imageList.set(index, item)
                notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.i(TAG, "replaceItem: " + e.message)
        }
    }

    fun removeAll() {
        imageList.clear()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): UserImage {
        try {
            return imageList[position]
        } catch (e: Exception) {
            return UserImage("", "")
        }

    }

    fun getAllItem(): ArrayList<UserImage> {
        return imageList
    }


    fun itemCount(): Int {
        return imageList.size
    }


    private fun getFileTypeFromURL(url: String): String {
        val extension: String = url.substring(url.lastIndexOf(".") + 1)
        Log.i("TAG", "getFileTypeFromURL: " + extension)
        return if (extension == "mp4" || extension == "flv" || extension == "m4a" || extension == "3gp" || extension == "mkv") {
            IConstants.VIDEO
        } else if (extension == "mp3" || extension == "ogg") {
            IConstants.AUDIO
        } else if (extension == "jpg" || extension == "png" || extension == "gif") {
            IConstants.PHOTO
        } else {
            ""
        }
    }

}