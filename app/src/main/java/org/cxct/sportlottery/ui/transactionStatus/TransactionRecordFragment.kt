package org.cxct.sportlottery.ui.transactionStatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_transaction_status.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.GameViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionRecordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionRecordFragment : BaseFragment<GameViewModel>(GameViewModel::class) {
    private val recordDiffAdapter by lazy { TransactionRecordDiffAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initObserve()
        getBetListData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_status, container, false)
    }

    private fun initRecyclerView() {
        rv_record.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordDiffAdapter
        }
    }

    private fun initObserve() {
        //TODO observe data
    }

    private fun getBetListData() {
        //TODO 設置投注列表資料
        recordDiffAdapter.submitList(listOf())
    }
}