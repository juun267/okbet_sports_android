package org.cxct.sportlottery.ui.finance

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.android.synthetic.main.fragment_finance.view.*
import kotlinx.android.synthetic.main.view_account_balance.*
import kotlinx.android.synthetic.main.view_account_balance.view.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.repository.FLAG_CREDIT_OPEN
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.SecurityDepositDialog
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.refreshMoneyLoading

/**
 * @app_destination 資金明細
 */
class FinanceFragment : BaseSocketFragment<FinanceViewModel>(FinanceViewModel::class), OnItemClickListener {

    private val recordAdapter by lazy { FinanceRecordAdapter(this@FinanceFragment) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_finance, container, false).apply {
            setupRefreshBalance(this)
            setupRecordList(this)
            viewModel.getLockMoney()
        }
    }

    private fun setupRefreshBalance(view: View) {
        view.btn_refresh.setOnClickListener {
            it.refreshMoneyLoading()
            viewModel.getMoneyAndTransferOut()
            viewModel.getLockMoney()
        }
    }

    private fun setupRecordList(view: View) {
        val rvlist = view.rvlist
        rvlist.setLinearLayoutManager()
        rvlist.adapter = recordAdapter
        view.tv_currency_type.text = sConfigData?.systemCurrencySign
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRecordList(view.context)
        viewModel.userMoney.observe(viewLifecycleOwner) {
            hideLoading()
            it?.apply {
                tv_balance.text = TextUtil.format(it)
            }
        }
        //总资产锁定金额
        viewModel.lockMoney.observe(viewLifecycleOwner) {
            if (sConfigData?.enableLockBalance.isNullOrEmpty()
                || sConfigData?.enableLockBalance?.equals("0") == true) {
                iv_deposit_tip.visibility = View.GONE
                return@observe
            }

            if (it == null || it <= 0.0) {
                iv_deposit_tip.visibility = View.GONE
                return@observe
            }

            iv_deposit_tip.visibility = View.VISIBLE
            iv_deposit_tip.setOnClickListener { _ ->
                val depositSpannable = SpannableString(getString(R.string.text_security_money, TextUtil.formatMoneyNoDecimal(it)))
                val daysLeftText = getString(R.string.text_security_money2, TimeUtil.getRemainDay(viewModel.userInfo.value?.uwEnableTime).toString())
                val remainDaySpannable = SpannableString(daysLeftText)
                val remainDay = TimeUtil.getRemainDay(viewModel.userInfo.value?.uwEnableTime).toString()
                val remainDayStartIndex = daysLeftText.indexOf(remainDay)
                remainDaySpannable.setSpan(ForegroundColorSpan(
                    ContextCompat.getColor(requireContext(), R.color.color_317FFF_1053af)),
                    remainDayStartIndex,
                    remainDayStartIndex + remainDay.length, 0
                )

                fragmentManager?.let { it1 ->
                    SecurityDepositDialog().apply {
                        this.depositText = depositSpannable
                        this.daysLeftText = remainDaySpannable
                    }.show(it1, this::class.java.simpleName)
                }
            }
        }
    }

    private fun getRecordList(context: Context) {

        val recharge = context.getString(R.string.record_recharge)
        val withdrawal = context.getString(R.string.record_withdrawal)
        val conversion = context.getString(R.string.record_conversion)
        val history = context.getString(R.string.record_history)
        val record = context.getString(R.string.redenvelope_record)

        val dataList = mutableListOf<String>()

        if (sConfigData?.creditSystem != FLAG_CREDIT_OPEN) {
            dataList.add(recharge)
            dataList.add(withdrawal)
        }

        dataList.add(conversion)
        dataList.add(history)

        //之後config api會提供參數判斷
        if (sConfigData?.thirdOpen != FLAG_OPEN &&
            BuildConfig.APPLICATION_ID != "com.happysport.sl.test" &&
            BuildConfig.APPLICATION_ID != "com.okbet.ph") {
            dataList.add(record)
        }

        recordAdapter.setList(dataList)
    }

    override fun onStart() {
        super.onStart()

        viewModel.getMoneyAndTransferOut()
        viewModel.getLockMoney()
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        viewModel.setRecordType(recordAdapter.getItem(position))
    }
}