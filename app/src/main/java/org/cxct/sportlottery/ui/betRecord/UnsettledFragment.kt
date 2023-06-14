package org.cxct.sportlottery.ui.betRecord

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentUnsettledBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.maintab.MainViewModel

class UnsettledFragment:BindingFragment<AccountHistoryViewModel,FragmentUnsettledBinding>() {
    private val mAdapter= RecyclerUnsettledAdapter()


    override fun onInitView(view: View) =binding.run {
        recyclerUnsettled.layoutManager=LinearLayoutManager(requireContext())
        recyclerUnsettled.adapter=mAdapter
    }


    override fun onInitData() {
        mAdapter.setList(arrayListOf("","","","",""))
    }
}