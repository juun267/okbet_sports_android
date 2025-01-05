package org.cxct.sportlottery.ui.profileCenter.pointshop.order

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogExchangeRewardBinding
import org.cxct.sportlottery.net.point.PointRepository
import org.cxct.sportlottery.net.point.data.Product
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.setColors


class ExchangeRewardDialog: BaseDialog<PointShopViewModel,DialogExchangeRewardBinding>() {


    companion object{
        fun newInstance(product: Product) = ExchangeRewardDialog().apply {
            arguments = Bundle().apply { putParcelable("product",product) }
        }
    }
    private val product by lazy { arguments?.getParcelable<Product>("product")!! }
    override fun onInitView() {
       setLayoutParams()
       initView()
       initClick()
       LogUtil.toJson(product)
    }
    private fun setLayoutParams() {
        dialog?.window?.let { window->
            val lp = window.attributes
            lp.gravity = Gravity.BOTTOM
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = lp
        }
    }
    private fun initView()=binding.run{
        tvValue.text = "$showCurrencySign${TextUtil.formatMoney2(product.price)}"
        tvTurnOver.text = "${product.validCheckPercentage}x"
        setBtnStauts()
    }
    private fun initClick()=binding.run{
        ivClose.setOnClickListener {
            dismiss()
        }
        linBuy.setOnClickListener {
            loginedRun(requireContext(),true){
                ConfirmOrderActivity.start(requireContext(), product)
                dismiss()
            }
        }
    }
    private fun setBtnStauts(){
        when{
            !LoginRepository.isLogined()-> {
                setUpBuyBtn(true,getString(R.string.A078))
            }
            PointRepository.blocked.value==1-> {
                setUpBuyBtn(true,getString(R.string.A078))
                binding.linBuy.setBtnEnable(false)
            }
            (PointRepository.userPoint.value?:0) < product.realPoints-> {//余额不足
                setUpBuyBtn(false,getString(R.string.A079))
            }//memberLimit未零，不限购买
            product.isLimitBuy() -> {//超过最大购买数
                setUpBuyBtn(false,getString(R.string.A081))
            }
            !product.isStarted -> { //仅限时抢购商品，商品未到抢购时间时
                setUpBuyBtn(false,"${TimeUtil.timeFormat(product.startDate,TimeUtil.MD_HMS)} ${getString(R.string.A073)}")
            }
            else->{
                setUpBuyBtn(true,getString(R.string.A078))
            }
        }
    }
    private fun setUpBuyBtn(enable: Boolean, name: String){
        binding.linBuy.isEnabled = enable
        if (enable){
            binding.linBuy.setBackgroundResource(R.drawable.img_task_reward_all)
            binding.tvBuyName.text = name
            binding.tvBuyName.setColors(R.color.color_FFFFFF)
            binding.tvBuyPrice.visible()
            binding.tvBuyPrice.text = TextUtil.formatMoney2(product.realPoints)
        }else{
            binding.linBuy.setBackgroundResource(R.drawable.bg_gray_radius_21_f3f5fa)
            binding.tvBuyName.text = name
            binding.tvBuyName.setColors(R.color.color_9DABC9)
            binding.tvBuyPrice.gone()
        }
    }

}