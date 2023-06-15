package org.cxct.sportlottery.ui.betRecord

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentSettledBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.view.onClick

class SettledFragment:BindingFragment<MainViewModel,FragmentSettledBinding>() {
    private val mAdapter=RecyclerUnsettledAdapter()

    override fun onInitView(view: View) =binding.run {
        recyclerSettled.layoutManager=LinearLayoutManager(requireContext())
        recyclerSettled.adapter=mAdapter


        empty.emptyView.onClick {
            requireActivity().finish()
        }
    }


    override fun onInitData() {
        super.onInitData()
        binding.empty.emptyView.visible()
//        mAdapter.setList(arrayListOf("","","",""))
    }
}