package org.cxct.sportlottery.ui.sport.endcard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.utils.ToastUtils
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.common.loading.LoadingAdapter
import org.cxct.sportlottery.databinding.FragmentEndcardHomeBinding
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.home.adapter.DateAdapter
import org.cxct.sportlottery.ui.sport.endcard.home.adapter.LeagueAdapter
import org.cxct.sportlottery.ui.sport.endcard.home.adapter.MatchAdapter

class EndCardHomeFragment: BaseFragment<EndCardVM, FragmentEndcardHomeBinding>() {

    private lateinit var loadingHolder: Gloading.Holder
    private lateinit var leagueAdapter: LeagueAdapter
    private lateinit var dateAdapter: DateAdapter
    private lateinit var matchAdapter: MatchAdapter

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
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
        loadingHolder.withRetry{ viewModel.loadEndCardMatchList() }
        loadingHolder.go()

    }

    private fun initObserver() {
        viewModel.endcardMatchList.observe(viewLifecycleOwner) {
            if (it == null) {
                ToastUtils.showToast(context(), context().getString(R.string.J871))
                loadingHolder.showLoadFailed()
                return@observe
            }

            matchAdapter.setNewInstance(it.toMutableList())
            dateAdapter.setNewInstance(mutableListOf("Mon", "Tue", "Today", "Thu", "Fri"))
            leagueAdapter.setNewInstance(mutableListOf("NBA", "CBA", "PSL", "KBL", "NBL", "PBA"))
            loadingHolder.showLoadSuccess()
        }
    }

    private fun initRecyclerView() = binding.run {
        rcvLeague.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        leagueAdapter = LeagueAdapter()
        rcvLeague.adapter = leagueAdapter

        rcvDate.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        dateAdapter = DateAdapter()
        rcvDate.adapter = dateAdapter

        binding.rcvMatchList.setLinearLayoutManager()
        matchAdapter = MatchAdapter(::showOddsList)
        binding.rcvMatchList.adapter = matchAdapter
    }

    private fun showOddsList(matchOdd: MatchOdd) {
        matchOdd.matchInfo?.let { (activity as EndCardActivity).showEndCardGame(it) }
    }

}