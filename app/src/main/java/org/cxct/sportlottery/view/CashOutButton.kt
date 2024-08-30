package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.startInfiniteRotation
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewBtnCashoutBinding
import org.cxct.sportlottery.view.setColors
import splitties.systemservices.layoutInflater

class CashOutButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    companion object{
        const val STATUS_COMFIRMING = 1//确认中
        const val STATUS_BETTING = 2//下注中
    }
    val binding = ViewBtnCashoutBinding.inflate(layoutInflater,this,true)
    var amountText = ""

    /**
     * cashout狀態 0:不可 ,1:可 ,2按鈕不可按,
     * operationStatus 1 确认中 ，2 下注中
     */
   fun setCashOutStatus(status: Int, operationStatus: Int, amountStr: String?=null){
        if (!amountStr.isNullOrEmpty()){
            this.amountText = amountStr
        }
        if (operationStatus==0){
            if (status==0){
               return gone()
            }else{
                visible()
            }
            when(status){
                1->{
                    binding.linRoot.setBackgroundResource(R.drawable.bg_blue_radius_8_stroke_1)
                    binding.icon.setImageResource(R.drawable.ic_cashout_yellow)
                    binding.tvName.visible()
                    binding.tvName.setColors(R.color.color_025BE8)
                    binding.tvName.text = context.getString(R.string.B71)
                    binding.tvAmount.visible()
                    binding.tvAmount.setColors(R.color.color_025BE8)
                    binding.tvAmount.text = amountText
                    binding.ivLoading.gone()
                    isEnabled =true
                }
                2->{
                    binding.linRoot.setBackgroundResource(R.drawable.bg_white_radius_8_stroke_e0e3ee)
                    binding.icon.setImageResource(R.drawable.ic_lock)
                    binding.tvName.gone()
                    binding.tvAmount.gone()
                    binding.ivLoading.gone()
                    isEnabled =false
                }
            }
        }else{
            visible()
            when(operationStatus){
                STATUS_COMFIRMING->{
                    binding.linRoot.setBackgroundResource(R.drawable.bg_blue_radius_8)
                    binding.icon.setImageResource(R.drawable.ic_cashout_yellow)
                    binding.tvName.visible()
                    binding.tvName.setColors(R.color.color_FFFFFF)
                    binding.tvName.text = context.getString(R.string.B72)
                    binding.tvAmount.visible()
                    binding.tvAmount.setColors(R.color.color_FFFFFF)
                    binding.tvAmount.text = amountText
                    binding.ivLoading.gone()
                    isEnabled =true
                }
                STATUS_BETTING->{
                    binding.linRoot.setBackgroundResource(R.drawable.bg_blue_radius_8_stroke_1)
                    binding.icon.setImageResource(R.drawable.ic_cashout_yellow)
                    binding.tvName.visible()
                    binding.tvName.setColors(R.color.color_025BE8)
                    binding.tvName.text = context.getString(R.string.B104)
                    binding.tvAmount.gone()
                    binding.ivLoading.visible()
                    binding.ivLoading.startInfiniteRotation(1000)
                    isEnabled =false
                }
            }
        }
   }

}