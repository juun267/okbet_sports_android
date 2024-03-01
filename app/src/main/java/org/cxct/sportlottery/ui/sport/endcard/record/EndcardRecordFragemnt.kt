package org.cxct.sportlottery.ui.sport.endcard.record

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentEndcardRecordBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseViewModel

class EndcardRecordFragemnt: BaseFragment<BaseViewModel,FragmentEndcardRecordBinding>() {

    private val recordAdapter by lazy { EndcardRecordAdapter() }
    override fun onInitView(view: View) {
        binding.rgTab.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId==binding.rbtnSettled.id){
                binding.linSettledMenu.visible()
            }else{
                binding.linSettledMenu.gone()
            }
        }
        binding.rgDate.setOnCheckedChangeListener { group, checkedId ->

        }
        initRecordList()
    }
    private fun initRecordList(){
        binding.rvBetRecord.apply {
            layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)
            adapter = recordAdapter
            recordAdapter.setList(listOf("",",",""))
        }
    }
}