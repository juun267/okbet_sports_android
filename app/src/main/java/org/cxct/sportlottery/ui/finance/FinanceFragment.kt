package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_finance.view.*
import kotlinx.android.synthetic.main.view_account_balance.*
import kotlinx.android.synthetic.main.view_account_balance.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.util.ArithUtil


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
        }
    }

    private fun setupRefreshBalance(view: View) {
        view.btn_refresh.setOnClickListener {
            loading()
            viewModel.getMoney()
        }
    }

    private fun setupRecordList(view: View) {
        view.rvlist.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = recordAdapter

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        view.tv_currency_type.text = sConfigData?.systemCurrency
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRecordList()

        viewModel.userMoney.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            tv_balance.text = ArithUtil.toMoneyFormat(it)
        })
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

        recordAdapter.data = recordList
    }

    override fun onStart() {
        super.onStart()

        viewModel.getMoney()
    }
}