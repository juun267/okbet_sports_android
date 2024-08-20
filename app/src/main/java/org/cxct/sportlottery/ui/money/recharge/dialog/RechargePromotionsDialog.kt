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
import org.cxct.sportlottery.ui.money.recharge.FirstDepositNoticeDialog
import org.cxct.sportlottery.ui.money.recharge.MoneyRechViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class RechargePromotionsDialog private constructor(): BaseDialog<MoneyRechViewModel, DialogRechargePromotionsBinding>() {

    companion object {

        private fun newInstance(dataList: ArrayList<DailyConfig>, selectedItem: DailyConfig?): RechargePromotionsDialog {
            val instance = RechargePromotionsDialog()
            val bundle = Bundle()
            bundle.putParcelableArrayList("dataList", dataList)
            bundle.putParcelable("DailyConfig", selectedItem)
            instance.arguments = bundle
            return instance
        }

        fun show(fragment: BaseFragment<MoneyRechViewModel, *>, dataList: ArrayList<DailyConfig>, selectedItem: DailyConfig?) {
            if (fragment !is OnSelectListener) {
                return
            }

            newInstance(dataList, selectedItem).show(fragment.childFragmentManager)
        }

        fun show(activity: BaseActivity<MoneyRechViewModel, *>, dataList: ArrayList<DailyConfig>, selectedItem: DailyConfig?) {
            if (activity !is OnSelectListener) {
                return
            }

            newInstance(dataList, selectedItem).show(activity.supportFragmentManager)
        }
    }

    interface OnSelectListener {
        fun onSelected(dailyConfig: DailyConfig?)
    }

    private val adapter = DailyConfigAdapter(::onItemClick, ::onTCClick)
    private val dataList by lazy { requireArguments().getParcelableArrayList<DailyConfig>("dataList")!! }
    private var selectItem: DailyConfig? = null

    override fun onInitView() = binding.run {
        marginHorizontal = 12.dp
        root.background = ShapeDrawable().setSolidColor(Color.WHITE).setRadius(12.dp.toFloat())
        ivClose.setOnClickListener { dismiss() }
        rcvPromotions.setLinearLayoutManager()
        rcvPromotions.adapter = adapter
        selectItem = arguments?.getParcelable<DailyConfig>("DailyConfig")
        linNoChoose.setOnClickListener {
            changeSelected(true)
            adapter.clearSelected()
            onSelectChange(null)
        }
    }

    override fun onBindViewStatus(view: View) {
        val list = dataList.toMutableList()
        adapter.setNewInstance(list)
        selectItem?.let { adapter.changeSelect(it) }
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

    private fun onTCClick(dailyConfig: DailyConfig) {
        FirstDepositNoticeDialog.newInstance(dailyConfig.content).show(childFragmentManager)
    }


}