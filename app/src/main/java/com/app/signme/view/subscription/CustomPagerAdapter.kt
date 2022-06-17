package com.app.signme.view.subscription

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.app.signme.R
import com.app.signme.commonUtils.utility.extension.loadCircleImage

import com.app.signme.dataclasses.response.LoginResponse

class CustomPagerAdapter(private val mContext: Context, private val user: LoginResponse?) :
        PagerAdapter() {
    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val customPagerEnum = CustomPagerEnum.values()[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(customPagerEnum.layoutResId, collection, false) as ViewGroup
        val ivProfileImage = layout.findViewById<ImageView>(R.id.ivProfileImage)
        if (user != null && ivProfileImage != null&& !user.profileImage.equals("")) {
            ivProfileImage.loadCircleImage(
                    user.profileImage,
                    R.drawable.user_profile
            )
        }
        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return CustomPagerEnum.values().size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence {
        val customPagerEnum = CustomPagerEnum.values()[position]
        return mContext.getString(customPagerEnum.titleResId)
    }
}