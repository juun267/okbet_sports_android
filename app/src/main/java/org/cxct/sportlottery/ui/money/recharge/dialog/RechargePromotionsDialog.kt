package org.cxct.sportlottery.ui.money.recharge.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.DialogRechargePromotionsBinding
import org.cxct.sportlottery.net.money.data.DailyConfig
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.money.recharge.DailyConfigAdapter
import org.cxct.sportlottery.ui.money.recharge.MoneyRechViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class RechargePromotionsDialog private constructor(): BaseDialog<MoneyRechViewModel, DialogRechargePromotionsBinding>() {

    companion object {

        private fun newInstance(dataList: ArrayList<DailyConfig>): RechargePromotionsDialog {
            val instance = RechargePromotionsDialog()
            val bundle = Bundle()
            bundle.putParcelableArrayList("dataList", dataList)
            instance.arguments = bundle
            return instance
        }

        fun show(fragment: BaseFragment<MoneyRechViewModel, *>, dataList: ArrayList<DailyConfig>) {
            if (fragment !is OnSelectListener) {
                return
            }
            newInstance(dataList).show(fragment.childFragmentManager)
        }

        fun show(activity: BaseActivity<MoneyRechViewModel, *>, dataList: ArrayList<DailyConfig>) {
            if (activity !is OnSelectListener) {
                return
            }
            newInstance(dataList).show(activity.supportFragmentManager)
        }
    }

    interface OnSelectListener {
        fun onSelected(dailyConfig: DailyConfig?)
    }

    private val adapter = DailyConfigAdapter(::onItemClick)
    private val dataList by lazy { requireArguments().getParcelableArrayList<DailyConfig>("dataList")!! }


    override fun onInitView() = binding.run {
        marginHorizontal = 12.dp
        root.background = ShapeDrawable().setSolidColor(Color.WHITE).setRadius(12.dp.toFloat())
        ivClose.setOnClickListener { dismiss() }
        rcvPromotions.setLinearLayoutManager()
        rcvPromotions.adapter = adapter
        linNoChoose.setOnClickListener {
            changeSelected(true)
            adapter.clearSelected()
            onSelectChange(null)
        }
    }

    override fun onBindViewStatus(view: View) {
        adapter.setNewInstance(dataList.toMutableList())
    }

    private fun onItemClick(item: DailyConfig) {
        changeSelected(false)
        onSelectChange(item)
        dismiss()
    }

    private fun changeSelected(select: Boolean) {
        if (binding.ivNoChooseCheck.isSelected != select) {
            binding.ivNoChooseCheck.isSelected = select
        }
    }

    private fun onSelectChange(dailyConfig: DailyConfig?) {
        val parent = parentFragment
        val act = activity
        if (parent is OnSelectListener) {
            parent.onSelected(dailyConfig)
        } else if (act is OnSelectListener) {
            act.onSelected(dailyConfig)
        }
    }


}