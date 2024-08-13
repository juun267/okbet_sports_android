package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogHomeFirstDepositBinding
import org.cxct.sportlottery.net.money.data.FirstDepositDetail
import org.cxct.sportlottery.net.money.data.FirstDepositConfig
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.jumpToDeposit
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog
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
            flFirstDeposit.visible()
            flDepositTomorrow.visible()
            tvFirstDepositLimit.text = "${sConfigData?.systemCurrencySign}${firstDepositDetail.activityConfigDailyTimeLimit?.limit}"
            tvFirstDeposit.text = "${sConfigData?.systemCurrencySign}${firstDepositDetail.isFirstDeposit?.limit}"
            tvDepositLimitTomorrow.text = "${sConfigData?.systemCurrencySign}${firstDepositDetail.activityConfigAfterLimitDay?.limit}"
            tvDepositTomorrow.text = "${sConfigData?.systemCurrencySign}${firstDepositDetail.activityConfigAfterDay?.limit}"
            binding.tvExtraBonus.text = String.format(getString(R.string.A017_1), "${firstDepositDetail.activityConfigDailyTimeLimit?.percent}%")
            binding.tvTomorrowBack.text = String.format(getString(R.string.A018), "${firstDepositDetail.activityConfigAfterLimitDay?.percent}%")
            val countSecond = (firstDepositDetail.expireTime - System.currentTimeMillis())/1000
            if (countSecond>0){
                startCount(countSecond.toInt())
            }
        }else{
            ivBackground.setImageResource(R.drawable.bg_dialog_home_first_deposit)
            linCountingTime.gone()
            flFirstDeposit.gone()
            flDepositTomorrow.gone()
            tvFirstDepositLimit.text = "${sConfigData?.systemCurrencySign}${firstDepositDetail.isFirstDeposit?.limit}"
            tvDepositLimitTomorrow.text = "${sConfigData?.systemCurrencySign}${firstDepositDetail.activityConfigAfterDay?.limit}"
            firstDepositDetail.activityConfigDailyTimeLimit
            binding.tvExtraBonus.text = String.format(getString(R.string.A017_1), "${firstDepositDetail.isFirstDeposit?.percent}%")
            binding.tvTomorrowBack.text = String.format(getString(R.string.A018), "${firstDepositDetail.activityConfigAfterDay?.percent}%")
        }
        ivClose.setOnClickListener { dismiss() }
        tvDeposit.clickDelay { (requireActivity() as BaseActivity<*,*>).jumpToDeposit() }
        tvDetail.setOnClickListener {  }

    }
    private fun startCount(expireTime: Int){
        GlobalScope.launch(lifecycleScope.coroutineContext) {
            CountDownUtil.countDown(
                this,
                expireTime/1000,
                { updateCountingView(expireTime)},
                { updateCountingView(it)},
                { dismiss() }
            )
        }
    }
    private fun updateCountingView(second: Int){
       val timeStr = TimeUtil.timeFormat(second*1000.toLong(),TimeUtil.HM_FORMAT_SS)
        binding.tvNum1.text = timeStr[0].toString()
        binding.tvNum2.text = timeStr[1].toString()
        binding.tvNum3.text = timeStr[3].toString()
        binding.tvNum4.text = timeStr[4].toString()
        binding.tvNum5.text = timeStr[6].toString()
        binding.tvNum6.text = timeStr[7].toString()
    }


}