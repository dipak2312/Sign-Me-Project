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

        var mediaFileRepository: MediaFileRepository? =
            container.context?.let { MediaFileRepository.getInstance(it) }

        if (!imageList.get(position).imageUrl.equals("")) {
            Glide.with(container.context)
                .load(imageList.get(position).imageUrl)
                // .error(R.drawable.ic_theappineers_logo)
                // .placeholder(R.drawable.ic_theappineers_logo)

                .into(binding.ivGalleryImage)

        }
        if (!imageList.get(position).imageUri.equals("")) {
            Glide.with(container.context)
                .load(imageList.get(position).imageUri)
                //.error(R.drawable.ic_theappineers_logo)
                // .placeholder(R.drawable.ic_theappineers_logo)
                .into(binding.ivGalleryImage)

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