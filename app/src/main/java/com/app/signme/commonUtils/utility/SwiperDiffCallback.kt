package com.app.signme.commonUtils.utility

import androidx.recyclerview.widget.DiffUtil
import com.app.signme.dataclasses.SwiperViewResponse

class SwiperDiffCallback (  private val old: List<SwiperViewResponse>,
                            private val new: List<SwiperViewResponse>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition].userId == new[newPosition].userId
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition] == new[newPosition]
    }

}