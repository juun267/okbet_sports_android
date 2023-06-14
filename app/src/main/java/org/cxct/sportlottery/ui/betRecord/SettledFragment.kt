package org.cxct.sportlottery.ui.betRecord

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentSettledBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.maintab.MainViewModel

class SettledFragment:BindingFragment<MainViewModel,FragmentSettledBinding>() {
    private val mAdapter=RecyclerUnsettledAdapter()

    override fun onInitView(view: View) =binding.run {
        recyclerSettled.layoutManager=LinearLayoutManager(requireContext())
        recyclerSettled.adapter=mAdapter
    }


    override fun onInitData() {
        super.onInitData()
        mAdapter.setList(arrayListOf("","","",""))
    }
}