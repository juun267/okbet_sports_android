package org.cxct.sportlottery.ui.home2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel
import timber.log.Timber

class HomeStartPageFragment : BaseFragment<MainViewModel>(MainViewModel::class)  {

    private val mMarqueeAdapter = MarqueeAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_start_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvMarquee()
        initObserve()
        queryData()
    }

    //公告
    private fun initRvMarquee() {
        rv_marquee.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }


    private fun initObserve() {
        viewModel.messageListResult.observe(viewLifecycleOwner, Observer {
            hideLoading()
            updateUiWithResult(it)
        })

        viewModel.matchTypeCardForParlay.observe(viewLifecycleOwner, Observer {
            when (it) {
                MatchType.PARLAY -> {
                    tabLayout.getTabAt(4)?.select()
                }
                MatchType.AT_START -> {
                    tabLayout.getTabAt(6)?.select()
                }
                else -> {
                }
            }
        })
    }


    private fun updateUiWithResult(messageListResult: MessageListResult) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult.rows?.forEach { data -> titleList.add(data.title + " - " + data.message) }

        if (messageListResult.success && titleList.size > 0) {
            rv_marquee.startAuto() //啟動跑馬燈
        } else {
            rv_marquee.stopAuto() //停止跑馬燈
        }

        mMarqueeAdapter.setData(titleList)
    }

    private fun queryData() {
        getAnnouncement()
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

}
