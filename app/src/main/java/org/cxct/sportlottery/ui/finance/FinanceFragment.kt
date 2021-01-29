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
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.ArithUtil
import timber.log.Timber


class FinanceFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userMoney.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            tv_balance.text = ArithUtil.toMoneyFormat(it)
        })

        viewModel.recordList.observe(this.viewLifecycleOwner, Observer {
            recordAdapter.data = it
        })

        viewModel.getMoney()
        viewModel.getRecordList()
    }
}