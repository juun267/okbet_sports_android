package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import org.cxct.sportlottery.databinding.FragmentLeftInplayBinding
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerInPlayAdapter

class LeftInPlayFragment:BindingSocketFragment<MainViewModel,FragmentLeftInplayBinding>() {
    private val inPlayAdapter= RecyclerInPlayAdapter()
    override fun onInitView(view: View) =binding.run {
        recyclerInPlay.layoutManager=GridLayoutManager(requireContext(),2)
        recyclerInPlay.adapter=inPlayAdapter
    }


    override fun onInitData() {
        super.onInitData()
        inPlayAdapter.data= arrayListOf("","","","","","","","","","","","","","","","","","","","","","","")
    }
}