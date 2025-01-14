package org.cxct.sportlottery.view.dialog

import android.graphics.Typeface
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.view_global_loading.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogHomeFirstDepositBinding
import org.cxct.sportlottery.net.money.data.FirstDepositDetail
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog
import org.w3c.dom.Text
import splitties.bundle.put

/**
 * 首页首充活动弹窗
 */
class HomeFirstDepositDialog : BaseDialog<BaseViewModel,DialogHomeFirstDepositBinding>() {


    companion object{
        private const val FIRST_DEPOSIT_DETAIL = "firstDepositDetail"
        fun newInstance(firstDepositDetail: FirstDepositDetail) = HomeFirstDepositDialog().apply{
            arguments = Bundle().apply { put(FIRST_DEPOSIT_DETAIL,firstDepositDetail) }
        }
        fun buildDialog(priority: Int, fm: () -> FragmentManager,firstDepositDetail: FirstDepositDetail): PriorityDialog? {
            return object : BasePriorityDialog<HomeFirstDepositDialog>() {
                override fun getId() = "HomeFirstDepositDialog"
                override fun getFragmentManager() = fm.invoke()
                override fun priority() = priority
                override fun createDialog() = newInstance(firstDepositDetail)
            }
        }
    }
    private val firstDepositDetail by lazy { requireArguments().getParcelable<FirstDepositDetail>(FIRST_DEPOSIT_DETAIL)!! }
    init {
        setStyle(R.style.FullScreen)
    }

    override fun onInitView()=binding.run {
        //判断是否限时
        if (firstDepositDetail.userStatus==0){
            ivBackground.setImageResource(R.drawable.bg_dialog_home_first_deposit_1)
            linCountingTime.visible()
            val countSecond = (firstDepositDetail.expireTime - System.currentTimeMillis())/1000
            if (countSecond>0){
                startCount(countSecond.toInt())
            }
        }else{
            ivBackground.setImageResource(R.drawable.bg_dialog_home_first_deposit)
            linCountingTime.gone()
        }
        ivClose.setOnClickListener { dismiss() }
        tvDeposit.clickDelay {
            (requireActivity() as BaseActivity<*,*>).jumpToDeposit("首冲活动弹窗",true)
            dismiss()
        }
        tvDetail.setOnClickListener {
            Constants.appendParams(Constants.getFirstDepositRules(requireContext()))?.let { it1 ->
                JumpUtil.toInternalWeb(requireContext(), it1,getString(R.string.A016))
            }
        }

    }
    private fun startCount(totalSecond: Int){
        GlobalScope.launch(lifecycleScope.coroutineContext) {
            CountDownUtil.countDown(
                this,
                totalSecond,
                { updateCountingView(totalSecond)},
                { updateCountingView(it)},
                { onComplete->
                    dismiss()
                    if (onComplete) {
                        ((requireParentFragment() as HomeFragment).getCurrentFragment() as? HomeHotFragment)?.getFirstDepositDetail()
                    }
                }
            )
        }
    }
    private fun updateCountingView(second: Int){
        var timeStr = TimeUtil.showCountDownHMS(second.toLong() * 1000)
        runWithCatch {
            timeStr= timeStr.substring(timeStr.length-8) //超长度显示，例如：239:53:12
            binding.tvNum1.text = timeStr[0].toString()
            binding.tvNum2.text = timeStr[1].toString()
            binding.tvNum3.text = timeStr[3].toString()
            binding.tvNum4.text = timeStr[4].toString()
            binding.tvNum5.text = timeStr[6].toString()
            binding.tvNum6.text = timeStr[7].toString()
        }
    }
    private fun TextView.setFormatSpanText(formatText: String,spanText: String){
        text = Spanny(String.format(formatText, spanText))
            .findAndSpan(spanText){
                ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_FF8A00))
            }
            .findAndSpan(spanText){
                StyleSpan(Typeface.BOLD)
            }
    }

}