package org.cxct.sportlottery.ui.money.recharge

import androidx.appcompat.app.AppCompatActivity
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.enums.ActivityType
import org.cxct.sportlottery.databinding.ItemDailyconfigBinding
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.TextUtil

class DailyConfigAdapter(private val onSelectedItem: (DailyConfig) -> Unit,
    private val onTCClick: ((DailyConfig) -> Unit)? = null ): BindingAdapter<DailyConfig,ItemDailyconfigBinding>() {
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
            if (onTCClick == null) {
                (context as? AppCompatActivity)?.let { it1 ->
                    FirstDepositNoticeDialog.newInstance(item.content).show(it1.supportFragmentManager)
                }
            } else {
                onTCClick.invoke(item)
            }

        }
        //4 为新用户首充，6 为每日首充
        tvType.text = when (item.activityType){
            ActivityType.FIRST_DEPOSIT_BONUS -> context.getString(R.string.P446)
            ActivityType.DAILY_BONUS-> context.getString(R.string.P445)
            else -> context.getString(R.string.P277)
        }
        linChooseReward.isSelected = selectPos ==position
        tvPercent.text = "${item.additional}%"
        tvCapped.text = "${sConfigData?.systemCurrencySign}${TextUtil.formatMoney(item.capped,0)}"
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

    fun changeSelect(dailyConfig: DailyConfig) {
        data.forEachIndexed { index, item ->
            if (item.activityType == dailyConfig.activityType) {
                selectPos = index
                notifyDataSetChanged()
                return
            }
        }
    }
}