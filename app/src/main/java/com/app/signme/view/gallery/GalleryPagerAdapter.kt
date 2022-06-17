package com.app.signme.view.gallery

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.app.signme.R
import com.app.signme.databinding.ItemGalleryPagerBinding
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler
import com.bumptech.glide.Glide
import java.util.*

/**
 * Gallery pager adapter is used to show images in pager view
 * @property mediaList [ERROR : null type]
 * @constructor
 */
class GalleryPagerAdapter(private val mediaList: ArrayList<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View, anyObject: Any): Boolean {
        return view == anyObject
    }

    override fun getCount(): Int {
        return mediaList.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ItemGalleryPagerBinding.inflate(LayoutInflater.from(container.context))
        container.addView(binding.root)
        Glide.with(container.context)
                .load(mediaList[position])
                .error(R.drawable.user_profile)
                .placeholder(R.drawable.user_profile)
                .into(binding.ivGalleryImage)
        val imageMatrixTouchHandler = ImageMatrixTouchHandler(container.context)
        binding.ivGalleryImage.setOnTouchListener(imageMatrixTouchHandler)
        return binding.root
    }
}