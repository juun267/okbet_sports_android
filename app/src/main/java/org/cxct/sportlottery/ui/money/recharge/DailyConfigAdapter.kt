package org.cxct.sportlottery.ui.money.recharge

import androidx.appcompat.app.AppCompatActivity
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemDailyconfigBinding
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.TextUtil

class DailyConfigAdapter(val onSelectedItem: (DailyConfig)->Unit): BindingAdapter<DailyConfig,ItemDailyconfigBinding>() {
    private var selectPos = -1
    override fun onBinding(position: Int, binding: ItemDailyconfigBinding, item: DailyConfig)=binding.run {
        linChooseReward.isSelected = false
        linChooseReward.setOnClickListener {
            selectPos = position
            notifyDataSetChanged()
            linChooseReward.isSelected = true
            onSelectedItem.invoke(item)
        }
        tvRewardTC.setOnClickListener {
            (context as? AppCompatActivity)?.let { it1 ->
                FirstDepositNoticeDialog.newInstance(item.content).show(it1.supportFragmentManager)
            }
        }
        linChooseReward.isSelected = selectPos ==position
        tvPercent.text = "${item.additional}%"
        tvCapped.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(item.capped,0)}"
        tvRewardDesp.text = when{
            item.rewards==1&&item.principal==0->context.getString(R.string.P279,item.times.toString())
            item.rewards==0&&item.principal==1->context.getString(R.string.P280,item.times.toString())
            item.rewards==1&&item.principal==1->context.getString(R.string.P281,item.times.toString())
            else -> ""
        }
    }
    fun clearSelected(){
        selectPos = -1
        notifyDataSetChanged()
    }
    fun getSelectedItem(): DailyConfig?=data.getOrNull(selectPos)
}