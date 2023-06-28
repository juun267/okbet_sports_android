package org.cxct.sportlottery.ui.maintab.menu

import android.util.Log
import android.view.View
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentLeftSportBetBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerLeftMatchesAdapter
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.onClick

class LeftSportBetFragment:BindingSocketFragment<SportLeftMenuViewModel,FragmentLeftSportBetBinding>() {
    //热门赛事 adapter
    private val hotMatchAdapter= RecyclerLeftMatchesAdapter()
    //需求暂时不要做了
//    private val classificationAdapter= RecyclerClassificationAdapter()
    private val loadingHolder by lazy { Gloading.wrapView(binding.content) }

    override fun onInitView(view: View) =binding.run{
        recyclerHotMatch.layoutManager=LinearLayoutManager(requireContext())
        recyclerHotMatch.adapter=hotMatchAdapter

        hotMatchAdapter.setOnItemClickListener{_,_,position->
            //item点击进入详情
            SportDetailActivity.startActivity(requireContext(),
                matchInfo = hotMatchAdapter.data[position].matchInfo!!,
                matchType = MatchType.IN_PLAY,
                true)
        }


        viewModel.betCountEvent.observe(this@LeftSportBetFragment){
            tvRecordNumber.text="${viewModel.totalCount}"
        }

        //投注详情
        constrainBetRecord.setOnClickListener {
            if(viewModel.isLogin()){
                startActivity(BetRecordActivity::class.java)
                constrainBetRecord.postDelayed({
                    val parent=parentFragment as SportLeftMenuFragment
                    parent.close()
                },500)
            }else{
                requireActivity().startLogin()
            }
        }
    }

    override fun onInitData() {
        super.onInitData()
        loadingHolder.showLoading()
        getHotMatchesData()

        if(viewModel.isLogin()){
            viewModel.getBetRecordCount()
        }
    }


    /**
     * 获取热门赛事数据
     */
    private fun getHotMatchesData(){
        //首页的数据如果不为空
        viewModel.publicityRecommend.value?.let {
            it.peekContent().let {data->
                hotMatchAdapter.setList(data)
            }
        }
        //刷新热门赛事数据
        viewModel.getRecommend()
        viewModel.publicityRecommend.observe(this){
            loadingHolder.showLoadSuccess()
            it.peekContent().let {data->
                hotMatchAdapter.setList(data)
            }
        }
    }
}