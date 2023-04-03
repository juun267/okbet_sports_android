package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_finance.view.*
import kotlinx.android.synthetic.main.view_account_balance.*
import kotlinx.android.synthetic.main.view_account_balance.view.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
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
class FinanceFragment : BaseSocketFragment<FinanceViewModel>(FinanceViewModel::class) {
    private val recordAdapter by lazy {
        FinanceRecordAdapter().apply {
            financeRecordListener = FinanceRecordListener {
                viewModel.setRecordType(it.first)
            }
        }
    }

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
        view.rvlist.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = recordAdapter

        }

        view.tv_currency_type.text = sConfigData?.systemCurrencySign
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRecordList()

        viewModel.userMoney.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            it?.apply {
                tv_balance.text = TextUtil.format(it)
            }
        })
        //总资产锁定金额
        viewModel.lockMoney.observe(viewLifecycleOwner) {
            if (sConfigData?.enableLockBalance.isNullOrEmpty() || sConfigData?.enableLockBalance?.equals(
                    "0") == true
            ) {
                iv_deposit_tip.visibility = View.GONE
            } else {
                if ((it?.toInt() ?: 0) > 0) {
                    iv_deposit_tip.visibility = View.VISIBLE
                    iv_deposit_tip.setOnClickListener { _ ->
                        val depositSpannable =
                            SpannableString(
                                getString(
                                    R.string.text_security_money,
                                    TextUtil.formatMoneyNoDecimal(it ?: 0.0)
                                )
                            )
                        val daysLeftText = getString(
                            R.string.text_security_money2,
                            TimeUtil.getRemainDay(viewModel.userInfo.value?.uwEnableTime).toString()
                        )
                        val remainDaySpannable = SpannableString(daysLeftText)
                        val remainDay =
                            TimeUtil.getRemainDay(viewModel.userInfo.value?.uwEnableTime).toString()
                        val remainDayStartIndex = daysLeftText.indexOf(remainDay)
                        remainDaySpannable.setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(requireContext(),
                                    R.color.color_317FFF_1053af)
                            ),
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
                } else {
                    iv_deposit_tip.visibility = View.GONE
                }
            }

        }
    }

    private fun getRecordList() {
        val recordStrList = context?.resources?.getStringArray(R.array.finance_array)
        val recordHideStrList = context?.resources?.getStringArray(R.array.finance_hide_array)
        val recordImgList = context?.resources?.obtainTypedArray(R.array.finance_img_array)


        val recordList = recordStrList?.filter {
            if (sConfigData?.thirdOpen == FLAG_OPEN)
                true
            else
                recordHideStrList?.contains(it) == false
        }?.map {
            it to (recordImgList?.getResourceId(recordStrList.indexOf(it), -1) ?: -1)
        } ?: listOf()

        recordImgList?.recycle()

        val list = recordList.toMutableList()

        //之後config api會提供參數判斷
        if (BuildConfig.APPLICATION_ID == "com.happysport.sl.test" || BuildConfig.APPLICATION_ID == "com.okbet.ph") {
            list.remove(
                list.find { it.first == getString(R.string.redenvelope_record) }
            )
        }

        if (sConfigData?.creditSystem == FLAG_CREDIT_OPEN) {
            list.remove(
                list.find { it.first == getString(R.string.record_recharge) }
            )
            list.remove(
                list.find { it.first == getString(R.string.record_withdrawal) }
            )

        }

        if (sConfigData?.thirdOpen == FLAG_OPEN) {
            list.remove(
                list.find { it.first == getString(R.string.redenvelope_record) }
            )
        }

        recordAdapter.data = list
    }

    override fun onStart() {
        super.onStart()

        viewModel.getMoneyAndTransferOut()
        viewModel.getLockMoney()
    }
}