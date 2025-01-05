package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.isVisible
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.animScale
import org.cxct.sportlottery.databinding.ContentMoneyPayTypeRvBinding
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MoneyManager
import org.cxct.sportlottery.util.TextUtil

class MoneyBankTypeAdapter : BindingAdapter<MoneyPayWayData,ContentMoneyPayTypeRvBinding>() {

    private var mSelectedPosition = 0

    @SuppressLint("SetTextI18n")
    override fun onBinding(
        position: Int,
        binding: ContentMoneyPayTypeRvBinding,
        item: MoneyPayWayData,
    )=binding.run {
        icBank.setImageResource(MoneyManager.getBankIcon(item.image))
        tvType.text = item.titleNameMap[LanguageManager.getLanguageString()]
        root.isSelected = mSelectedPosition == position //選中改變背景
        tvType.isSelected = mSelectedPosition == position
        imgTri.visibility = if (mSelectedPosition == position) View.VISIBLE else View.GONE
        if(item.rebateFeeNew > 0.0){
            tvPromot.animScale(1.15f)
            tvPromot.text = "${TextUtil.formatMoney2(item.rebateFeeNew*100)}%\nBonus"
            tvPromot.isVisible =true
        }else{
            tvPromot.isVisible =false
        }
    }

    override fun setList(list: Collection<MoneyPayWayData>?) {
        mSelectedPosition = 0
        super.setList(list)
    }
    fun setSelectedPosition(position: Int){
        mSelectedPosition = position
        notifyDataSetChanged()
    }
    fun updaterebateFeeNew(rechCfg: RechCfg){
        data.forEach {
            if(it.onlineType == rechCfg.onlineType){
                it.rebateFeeNew = rechCfg.rebateFeeNew?:0.0
            }
        }
        notifyDataSetChanged()
    }

}