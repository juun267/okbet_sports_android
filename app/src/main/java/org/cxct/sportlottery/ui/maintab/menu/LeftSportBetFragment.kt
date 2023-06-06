package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentLeftSportBetBinding
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerClassificationAdapter
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerLeftMatchesAdapter

class LeftSportBetFragment:BindingSocketFragment<MainViewModel,FragmentLeftSportBetBinding>() {

    private val hotMatchAdapter= RecyclerLeftMatchesAdapter()
    //需求暂时不要了
//    private val classificationAdapter= RecyclerClassificationAdapter()

    override fun onInitView(view: View) =binding.run{

        recyclerHotMatch.layoutManager=LinearLayoutManager(requireContext())
        recyclerHotMatch.adapter=hotMatchAdapter

    }

    override fun onInitData() {
        super.onInitData()
        getHotMatchesData()
    }


    private fun getHotMatchesData(){
        hotMatchAdapter.data= arrayListOf("","","","","","","","","","","","")
    }
}