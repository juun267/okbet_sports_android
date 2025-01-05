package org.cxct.sportlottery.ui.profileCenter.pointshop


import android.content.Context
import android.content.Intent
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ActivityOrderDetailBinding
import org.cxct.sportlottery.net.point.data.ProductRedeem
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.setServiceClick
import org.cxct.sportlottery.view.setColors

class OrderDetailActivity: BaseActivity<PointShopViewModel, ActivityOrderDetailBinding>() {

    override fun pageName() = "积分商城订单详情"
    companion object{
        fun start(context: Context,productRedeem: ProductRedeem){
            context.startActivity(Intent(context,OrderDetailActivity::class.java).apply {
                putExtra("productRedeem", productRedeem)
            })
        }
    }

    private val productRedeem by lazy { intent.getParcelableExtra<ProductRedeem>("productRedeem")!! }

    override fun onInitView() {
        setStatusbar(R.color.color_F6F7F8, true)
        binding.toolBar.binding.root.setBackgroundResource(R.color.color_F6F7F8)
        initView()
    }
    private fun initView()=binding.run{
        toolBar.setOnBackPressListener { finish() }
        tvCustomService.setServiceClick(supportFragmentManager)
        setOrderType(productRedeem.productType)
        setStatusType(productRedeem.status)
        tvOrderNo.text = productRedeem.orderNo
        tvName.text = productRedeem.productName
        tvPrice.text = TextUtil.formatMoney2(productRedeem.totalPoints)
        tvContact.text = productRedeem.customerName
        tvMobile.text = "+63${productRedeem.customerPhone}"
        tvAddress.text = productRedeem.customerAddress
    }

    /**
     *  1线上发送的, 2线下寄送的 ，
     */
    private fun setOrderType(productType: Int)=binding.run{
        if (productType==1){
            ivDivider1.gone()
            linStep2.gone()
            ivStepName3.setImageResource(R.drawable.ic_order_finish_nor)
            tvStepName3.text = getString(R.string.A108)
            linMobile.gone()
            linAddress.gone()
            linContact.gone()
        }
    }

    /**
     *  状态 0：待審核，1：審核通過, 2：審核未通過 3：未發貨 4：已發貨
     */
    private fun setStatusType(status: Int)=binding.run{
        when(status){
            0-> {
                ivStepName1.setImageResource(R.drawable.ic_order_verify_sel)
                tvStepName1.setColors(R.color.color_025BE8)
                ivStepName1.setBackgroundResource(R.drawable.bg_order_status_blue)
            }
            1-> {
                ivStepName3.setImageResource(R.drawable.ic_order_finish_sel)
                tvStepName3.setColors(R.color.color_025BE8)
                ivStepName3.setBackgroundResource(R.drawable.bg_order_status_blue)
            }
            2-> {
                ivStepName3.setImageResource(R.drawable.ic_order_reject_sel)
                tvStepName3.setColors(R.color.color_ff0000)
                ivStepName3.setBackgroundResource(R.drawable.bg_order_status_red)
                tvStepName3.text = getString(R.string.A109)
            }
            3-> {
                ivStepName2.setImageResource(R.drawable.ic_order_send_sel)
                tvStepName2.setColors(R.color.color_025BE8)
                ivStepName2.setBackgroundResource(R.drawable.bg_order_status_blue)
            }
            4-> {
                ivStepName3.setImageResource(R.drawable.ic_order_sent_sel)
                tvStepName3.setColors(R.color.color_025BE8)
                ivStepName3.setBackgroundResource(R.drawable.bg_order_status_blue)
            }
        }
    }


}