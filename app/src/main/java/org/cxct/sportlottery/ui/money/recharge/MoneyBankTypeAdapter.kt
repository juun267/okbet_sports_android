package org.cxct.sportlottery.ui.money.recharge

import android.view.View
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ContentMoneyPayTypeRvBinding
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MoneyManager

class MoneyBankTypeAdapter : BindingAdapter<MoneyPayWayData,ContentMoneyPayTypeRvBinding>() {

    private var mSelectedPosition = 0

    override fun onBinding(
        position: Int,
        binding: ContentMoneyPayTypeRvBinding,
        item: MoneyPayWayData,
    ) {
        binding.icBank.setImageResource(MoneyManager.getBankIcon(item.image))
        binding.tvType.text = item.titleNameMap[LanguageManager.getLanguageString()]
        binding.root.isSelected = mSelectedPosition == position //選中改變背景
        binding.tvType.isSelected = mSelectedPosition == position
        binding.imgTri.visibility = if (mSelectedPosition == position) View.VISIBLE else View.GONE
    }

    override fun setList(list: Collection<MoneyPayWayData>?) {
        mSelectedPosition = 0
        super.setList(list)
    }
    fun setSelectedPosition(position: Int){
        mSelectedPosition = position
        notifyDataSetChanged()
    }

}