package org.cxct.sportlottery.view

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.cxct.sportlottery.common.event.ShowInPlayEvent
import org.cxct.sportlottery.databinding.ViewBetEmptyBinding
import org.cxct.sportlottery.util.EventBusUtil

class BetEmptyView(context: Context): LinearLayout(context) {
    val binding: ViewBetEmptyBinding

    init {
        binding =ViewBetEmptyBinding.inflate(LayoutInflater.from(context), this,true)
        initView()
    }

    private fun initView(){
        binding.tvReturn.onClick {
            if(context is Activity){
                (context as Activity).finish()
                EventBusUtil.post(ShowInPlayEvent())
            }
        }
    }
    fun marginTop(top: Int){
        (binding.ivEmpty.layoutParams as MarginLayoutParams).apply {
            topMargin = top
            binding.ivEmpty.layoutParams = this
        }
    }
    fun center(){
        (binding.ivEmpty.layoutParams as MarginLayoutParams).apply {
            topMargin = 0
            binding.ivEmpty.layoutParams = this
        }
        binding.root.gravity = Gravity.CENTER
    }


}