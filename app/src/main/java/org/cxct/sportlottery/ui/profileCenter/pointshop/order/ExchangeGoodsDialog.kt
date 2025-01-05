package org.cxct.sportlottery.ui.profileCenter.pointshop.order

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogExchangeGoodsBinding
import org.cxct.sportlottery.net.point.PointRepository
import org.cxct.sportlottery.net.point.data.Product
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.setColors


class ExchangeGoodsDialog: BaseDialog<PointShopViewModel,DialogExchangeGoodsBinding>() {


    companion object{
        fun newInstance(product: Product) = ExchangeGoodsDialog().apply {
            arguments = Bundle().apply { putParcelable("product", product) }
        }
    }
    private val product by lazy { arguments?.getParcelable<Product>("product")!! }
    private val thumbAdapter = GoodsThumbAdapter()
    private val centerLayoutManager by lazy {  ScrollCenterLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false) }

    override fun onInitView() {
       setLayoutParams()
       initView()
       initClick()
       initThumb()
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
        tvGoodsName.text = product.name
        tvDesp.text = product.desc
        tvDesp.movementMethod = ScrollingMovementMethod()
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
        ivLeftArrow.setOnClickListener {
            var targetPos = thumbAdapter.selectPos-1
            if(targetPos<0){
                targetPos= thumbAdapter.itemCount-1
            }
            selectThumbPosition(targetPos)
        }
        ivRightArrow.setOnClickListener {
            var targetPos = thumbAdapter.selectPos+1
            if(targetPos>(thumbAdapter.itemCount-1)){
                targetPos = 0
            }
            selectThumbPosition(targetPos)
        }
    }
    private fun initThumb(){
        binding.rvThumb.layoutManager = centerLayoutManager
        binding.rvThumb.addItemDecoration(SpaceItemDecoration(requireContext(), R.dimen.margin_8))
        val items: List<String> = (product.imgList?: listOf()).sortedBy { it.sort }.map { it.path }.filterNot { it.isEmpty() }
        thumbAdapter.setList(items)
        binding.rvThumb.adapter = thumbAdapter
        thumbAdapter.setOnItemClickListener { adapter, view, position ->
            selectThumbPosition(position)
        }
        if (thumbAdapter.itemCount>0){
            selectThumbPosition(0)
        }
        if (thumbAdapter.itemCount>1){
            binding.ivLeftArrow.isVisible = true
            binding.ivRightArrow.isVisible = true
        } else{
            binding.ivLeftArrow.isVisible = false
            binding.ivRightArrow.isVisible = false
        }
    }
    private fun selectThumbPosition(targetPos: Int){
        thumbAdapter.selectPos = targetPos
        centerLayoutManager.scrollToPosition(targetPos)
        binding.ivCenterPicture.load("${sConfigData?.resServerHost}${thumbAdapter.getItemOrNull(targetPos)}")
//        binding.ivLeftArrow.isVisible = thumbAdapter.selectPos!=0
//        binding.ivRightArrow.isVisible = thumbAdapter.selectPos!=(thumbAdapter.itemCount-1)
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
            product.isLimitBuy()-> {//memberLimit 會員限購數量,purchasedQuantity,已購買數量 ,超过最大购买数
                setUpBuyBtn(false,getString(R.string.A081))
            }
            !product.isStarted -> { //仅限时抢购商品，商品未到抢购时间时
                setUpBuyBtn(false,"${TimeUtil.timeFormat(product.startDate, TimeUtil.MD_HMS)} ${getString(R.string.A073)}")
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