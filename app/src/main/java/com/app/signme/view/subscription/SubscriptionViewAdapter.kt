package com.app.signme.view.subscription

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.signme.R
import com.app.signme.commonUtils.utility.extension.sharedPreference

import com.app.signme.dataclasses.SubscriptionPlan


class SubscriptionViewAdapter(
    val context: Context,
    var list: ArrayList<SubscriptionPlan>,
    val flag_upgrade_downgrade: String,
    private val subscriptionClickListener: SubscriptionClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedItemPos: Int = -1
    private var lastItemSelectedPos: Int = -1

    companion object {
        const val VIEW_TYPE_NORMAL_SUBS = 1
        const val VIEW_TYPE_DISCOUNTED_SUBS = 2
    }

    private inner class View1ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvPlanTitle: TextView = itemView.findViewById(R.id.tvPlanTitle)
        var tvPlanAmount: TextView = itemView.findViewById(R.id.tvPlanAmount)
        var ivMarkSelected: ImageView = itemView.findViewById(R.id.ivMarkSelected)
        var llSubscriptionContent: LinearLayout = itemView.findViewById(R.id.llSubscriptionContent)
        var tvDiscountPercent: TextView = itemView.findViewById(R.id.tvDiscountPercent)

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(position: Int) {
            if (position == selectedItemPos) {
                ivMarkSelected.visibility = View.VISIBLE
                llSubscriptionContent.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_un_selected)
            } else {
                ivMarkSelected.visibility = View.INVISIBLE
                llSubscriptionContent.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_un_selected)
            }
            /**
             * Mark last selected item
             */
            if (sharedPreference.subscriptionItemSelected == position && flag_upgrade_downgrade == "1") {
                ivMarkSelected.visibility = View.VISIBLE
            }


            val recyclerViewModel = list[position]
            tvPlanTitle.text = recyclerViewModel.planName
            tvPlanAmount.text = recyclerViewModel.planAmountForDisplay
            if (position == 0 && recyclerViewModel.planValidityInDays == "30") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "(Start with a 1 month free trial)" //$3 Off
                // sharedPreference.subscriptionItemSelected = position
            } else if (position == 1 && recyclerViewModel.planValidityInDays == "90") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "(Save Upto 20%)" //$3 Off
                // sharedPreference.subscriptionItemSelected = position
            } else if (position == 2 && recyclerViewModel.planValidityInDays == "180") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "(Save Upto 20%)" //$12 Off
                // sharedPreference.subscriptionItemSelected = position
            } else if (position == 3 && recyclerViewModel.planValidityInDays == "365") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "(Save Upto 20%)" //$60 Of
                //  sharedPreference.subscriptionItemSelected = position
            } else {
                tvDiscountPercent.visibility = View.GONE

            }

            itemView.setOnClickListener {
                selectedItemPos = adapterPosition
                if (lastItemSelectedPos == -1) {
                    list[position].isSelected = true
                    lastItemSelectedPos = selectedItemPos

                } else {
                    list[lastItemSelectedPos].isSelected = false
                    lastItemSelectedPos = selectedItemPos
                }
                notifyDataSetChanged()
                subscriptionClickListener.onSubscriptionClick(
                    selectedItemPos,
                    data = recyclerViewModel
                )
            }

        }
    }

    private inner class View2ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var tvPlanTitle: TextView = itemView.findViewById(R.id.tvPlanTitle)
        var tvPlanAmount: TextView = itemView.findViewById(R.id.tvPlanAmount)
        var ivMarkSelected: ImageView = itemView.findViewById(R.id.ivMarkSelected)
        var llSubsDiscountContent: LinearLayout = itemView.findViewById(R.id.llSubsDiscountContent)
        var tvDiscountPercent: TextView = itemView.findViewById(R.id.tvDiscountPercent)

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(position: Int) {
            if (position == selectedItemPos) {
                ivMarkSelected.visibility = View.VISIBLE
                llSubsDiscountContent.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_selected_new)
            } else {
                ivMarkSelected.visibility = View.INVISIBLE
                llSubsDiscountContent.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_un_selected)
            }

            val recyclerViewModel = list[position]
            tvPlanTitle.text = recyclerViewModel.planName

            if (position == 2 && recyclerViewModel.planValidityInDays == "90") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "(Save Upto 20%)"
            } else if (position == 3 && recyclerViewModel.planValidityInDays == "180") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "(Save Upto 20%)"
            } else if (position == 3 && recyclerViewModel.planValidityInDays == "365") {
                tvDiscountPercent.visibility = View.VISIBLE
                tvDiscountPercent.text = "(Save Upto 20%)"
            } else {
                tvDiscountPercent.visibility = View.GONE

            }
            val payAmount = recyclerViewModel.planAmountForDisplay
            val spannableString1 = SpannableString(payAmount)
            if (payAmount != null) {
                spannableString1.setSpan(StrikethroughSpan(), 0, payAmount.length, 0)
            }
            tvPlanAmount.text = spannableString1
            itemView.setOnClickListener {
                selectedItemPos = adapterPosition
                if (lastItemSelectedPos == -1) {
                    list[position].isSelected = true
                    lastItemSelectedPos = selectedItemPos
                } else {
                    list[lastItemSelectedPos].isSelected = false
                    lastItemSelectedPos = selectedItemPos
                }

                notifyDataSetChanged()
                subscriptionClickListener.onSubscriptionClick(
                    selectedItemPos,
                    data = recyclerViewModel
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_NORMAL_SUBS) {
            return View1ViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.subscription_plan_vertical_item, parent, false)
            )
        }
        return View2ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.subscription_plan_discounted_grid_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 4) {
            (holder as View2ViewHolder).bind(position)
        } else {
            (holder as View1ViewHolder).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 4) {
            VIEW_TYPE_DISCOUNTED_SUBS

        } else {
            VIEW_TYPE_NORMAL_SUBS
        }
    }
}