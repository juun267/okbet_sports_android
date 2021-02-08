package org.cxct.sportlottery.ui.game.outright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_outright_detail.*
import kotlinx.android.synthetic.main.fragment_outright_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.home.MainViewModel

class OutrightDetailFragment : BaseSocketFragment<MainViewModel>(MainViewModel::class) {

    private val outrightOddAdapter by lazy {
        OutrightOddAdapter().apply {
            outrightOddListener = OutrightOddAdapter.OutrightOddListener {
                viewModel.updateOutrightOddsSelectedState(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_outright_detail, container, false).apply {
            setupOutrightOddList(this)
        }
    }

    private fun setupOutrightOddList(view: View) {
        view.outright_detail_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = outrightOddAdapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.outrightOddsListResult.observe(this.viewLifecycleOwner, Observer {
            if (it != null && it.success) {
                setupOutrightOddList(it)
            }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, Observer {
            outrightOddAdapter.betInfoListData = it
        })
    }

    private fun setupOutrightOddList(outrightOddsListResult: OutrightOddsListResult) {
        val matchOdd =
            outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(0)

        outright_detail_title.text =
            outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.league?.name

        outrightOddAdapter.data = matchOdd?.displayList ?: listOf()
    }
}