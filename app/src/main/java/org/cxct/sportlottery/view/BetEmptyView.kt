package org.cxct.sportlottery.view

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.cxct.sportlottery.databinding.ViewBetEmptyBinding

class BetEmptyView(context: Context): LinearLayout(context) {
    private val binding: ViewBetEmptyBinding

    init {
        binding =ViewBetEmptyBinding.inflate(LayoutInflater.from(context), this,true)
        initView()
    }

    private fun initView(){

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