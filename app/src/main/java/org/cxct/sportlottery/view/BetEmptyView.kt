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
        orientation = VERTICAL
        gravity=Gravity.CENTER_HORIZONTAL
        binding =ViewBetEmptyBinding.inflate(LayoutInflater.from(context), this)
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

}