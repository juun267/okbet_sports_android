package org.cxct.sportlottery.ui.sport.endcard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.luck.picture.lib.utils.ToastUtils
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.common.loading.LoadingAdapter
import org.cxct.sportlottery.databinding.FragmentEndcardHomeBinding
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardGuideDialog
import org.cxct.sportlottery.ui.sport.endcard.home.adapter.DateAdapter
import org.cxct.sportlottery.ui.sport.endcard.home.adapter.EndCardLeague
import org.cxct.sportlottery.ui.sport.endcard.home.adapter.LeagueAdapter
import org.cxct.sportlottery.ui.sport.endcard.home.adapter.MatchAdapter
import org.cxct.sportlottery.ui.sport.endcard.home.adapter.WinnersMarqueeAdapter

class EndCardHomeFragment: BaseFragment<EndCardVM, FragmentEndcardHomeBinding>() {

    private lateinit var loadingHolder: Gloading.Holder
    private lateinit var leagueAdapter: LeagueAdapter
    private lateinit var dateAdapter: DateAdapter
    private lateinit var matchAdapter: MatchAdapter
    private lateinit var marqueeAdapter: WinnersMarqueeAdapter

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loadingHolder = Gloading
            .from(LoadingAdapter(bgColor = context().getColor(R.color.color_0E131F)))
            .wrap(super.createRootView(inflater, container, savedInstanceState))
        return loadingHolder.wrapper
    }

    override fun loading(message: String?) {
        loadingHolder.showLoading()
    }

    override fun dismissLoading() {
        loadingHolder.showLoadSuccess()
    }

    override fun onInitView(view: View) {
        initRecyclerView()
        initMarquee()
        binding.tvRule.clickDelay {
            (activity as EndCardActivity).showEndCardRule()
        }
        binding.tvTutorial.clickDelay{
            EndCardGuideDialog().show(childFragmentManager)
        }
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
        binding.rcvMarquee.bindLifecycler(this)
        loadingHolder.withRetry{
            loadingHolder.showLoading()
            viewModel.loadEndCardMatchList()
        }
        loadingHolder.go()

    }

    private fun initMarquee() {
        binding.rcvMarquee.setLinearLayoutManager(LinearLayoutManager.HORIZONTAL)
        marqueeAdapter = WinnersMarqueeAdapter()
        binding.rcvMarquee.adapter = marqueeAdapter

        marqueeAdapter.setNewInstance(mutableListOf("Sid win ₱ 888,888 in New Orleans Pelicans VS Los Angles Lakers. Bill bet in New Orleans Pelicans VS Los Angles Lakers."
        ,"Sid win ₱ 888,888 in New Orleans Pelicans VS Los Angles Lakers. Bill bet in New Orleans Pelicans VS Los Angles Lakers."
        ,"Sid win ₱ 888,888 in New Orleans Pelicans VS Los Angles Lakers. Bill bet in New Orleans Pelicans VS Los Angles Lakers."
        ,"Sid win ₱ 888,888 in New Orleans Pelicans VS Los Angles Lakers. Bill bet in New Orleans Pelicans VS Los Angles Lakers."
        ,"Sid win ₱ 888,888 in New Orleans Pelicans VS Los Angles Lakers. Bill bet in New Orleans Pelicans VS Los Angles Lakers."))

        binding.rcvMarquee.startAuto(false)
    }

    private fun initObserver() {
        viewModel.endcardMatchList.observe(viewLifecycleOwner) {
            if (it == null) {
                ToastUtils.showToast(context(), context().getString(R.string.J871))
                loadingHolder.showLoadFailed()
                return@observe
            }

            if (it.isEmpty()) {
                loadingHolder.showEmpty()
                return@observe
            }

            val list = it.toMutableList()
            leagueAdapter.setNewInstance(list)
            changeLeague(list.getOrNull(0))
            loadingHolder.showLoadSuccess()
        }

        viewModel.betNum.collectWith(lifecycleScope) {
            matchAdapter.updateBetsNum(it.first, it.second)
        }
    }

    private fun initRecyclerView() = binding.run {
        rcvLeague.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        leagueAdapter = LeagueAdapter { changeDateList(it) }
        rcvLeague.adapter = leagueAdapter


        rcvDate.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        dateAdapter = DateAdapter { leagueOdd, matchOdds ->
            changeMatchList(leagueOdd, matchOdds)
        }
        rcvDate.adapter = dateAdapter

        binding.rcvMatchList.setLinearLayoutManager()
        matchAdapter = MatchAdapter(::showOddsList)
        binding.rcvMatchList.adapter = matchAdapter
    }

    private fun showOddsList(matchOdd: MatchOdd) {
        matchOdd.matchInfo?.let { (activity as EndCardActivity).showEndCardGame(it) }
    }

    private fun changeLeague(leagueOdd: LeagueOdd?) {
        if (leagueOdd == null) {
            dateAdapter.setNewInstance(null)
            matchAdapter.setNewInstance(null)
            return
        }

        changeDateList(leagueOdd)
    }

    private fun changeDateList(leagueOdd: LeagueOdd) {
        changeMatchList(leagueOdd, dateAdapter.setNewLeagueData(leagueOdd).first().second)
    }

    private fun changeMatchList(leagueOdd: LeagueOdd, matchList: List<MatchOdd>) {
        matchAdapter.setNewInstance(listOf(EndCardLeague(leagueOdd, matchList as MutableList<BaseNode>?)).toMutableList())
    }

}