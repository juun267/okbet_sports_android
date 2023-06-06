package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentLeftSportBetBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerLeftMatchesAdapter
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity

class LeftSportBetFragment:BindingSocketFragment<SportLeftMenuViewModel,FragmentLeftSportBetBinding>() {

    private val hotMatchAdapter= RecyclerLeftMatchesAdapter()
    //需求暂时不要做了
//    private val classificationAdapter= RecyclerClassificationAdapter()

    override fun onInitView(view: View) =binding.run{
        recyclerHotMatch.layoutManager=LinearLayoutManager(requireContext())
        recyclerHotMatch.adapter=hotMatchAdapter

        hotMatchAdapter.setOnItemClickListener{_,_,position->
            SportDetailActivity.startActivity(requireContext(),
                matchInfo = hotMatchAdapter.data[position].matchInfo!!,
                matchType = MatchType.IN_PLAY,
                true)
        }
    }

    override fun onInitData() {
        super.onInitData()
        getHotMatchesData()
    }



    private fun getHotMatchesData(){
        viewModel.publicityRecommend.value?.let {
            it.peekContent().let {data->
                hotMatchAdapter.setList(data)
            }
        }
        viewModel.getRecommend()
        viewModel.publicityRecommend.observe(this){
            it.peekContent().let {data->
                hotMatchAdapter.setList(data)
            }
        }
    }
}