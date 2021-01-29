package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import androidx.fragment.app.Fragment
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FinanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinanceFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val recordAdapter by lazy {
        FinanceRecordAdapter().apply {
            financeRecordListener = FinanceRecordListener {
                viewModel.setRecordType(it.first)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FinanceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FinanceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}