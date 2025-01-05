package org.cxct.sportlottery.ui.profileCenter.pointshop.record

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemPointExchangeBinding
import org.cxct.sportlottery.net.point.data.ProductRedeem
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.profileCenter.pointshop.ProductType
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.view.setColors
import timber.log.Timber

class PointExchangeAdapter: BindingAdapter<ProductRedeem, ItemPointExchangeBinding>() {
    override fun onBinding(
        position: Int,
        binding: ItemPointExchangeBinding,
        item: ProductRedeem,
    )=binding.run {
        if (item.productType==ProductType.ITEM.code){
            ivImage.load("${sConfigData?.resServerHost}${item.imageUrl}")
            tvFundValue.isVisible = false
        }else{
            ivImage.load(R.drawable.img_shop_item_fund)
            tvFundValue.isVisible = true
            tvFundValue.text = TextUtil.formatMoney2(item.price)
        }
        tvName.text = item.productName
        tvTime.text = TimeUtil.stampToDateHMS(item.addTime)
        tvAmount.text = TextUtil.formatMoney2(item.totalPoints)
        //状态 0：待審核，1：審核通過, 2：審核未通過 3：未發貨 4：已發貨
        when (item.status){
            0->{
                tvStatus.background = ContextCompat.getDrawable(context,R.drawable.img_task_not_pass_button)
                tvStatus.setColors(R.color.color_9DABC9)
                tvStatus.text = context.getString(R.string.A105)
            }
            1->{
                tvStatus.background = ContextCompat.getDrawable(context,R.drawable.img_task_go_finish_button)
                tvStatus.setColors(R.color.color_FFFFFF)
                tvStatus.text = context.getString(R.string.A108)
            }
            2->{
                tvStatus.background = ContextCompat.getDrawable(context,R.drawable.img_task_not_pass_button)
                tvStatus.setColors(R.color.color_9DABC9)
                tvStatus.text = context.getString(R.string.A109)
            }
            3->{
                tvStatus.background = ContextCompat.getDrawable(context,R.drawable.img_task_not_pass_button)
                tvStatus.setColors(R.color.color_9DABC9)
                tvStatus.text = context.getString(R.string.A106)
            }
            4->{
                tvStatus.background = ContextCompat.getDrawable(context,R.drawable.img_task_claim_button)
                tvStatus.setColors(R.color.color_FFFFFF)
                tvStatus.text = context.getString(R.string.A107)
            }
        }
    }

}