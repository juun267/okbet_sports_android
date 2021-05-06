package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil


@Suppress("DEPRECATION")
class OddsDetailFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),
    OnOddClickListener {

    private val args: OddsDetailFragmentArgs by navArgs()

    companion object {
        const val TIME_LENGTH = 5
    }

    var matchId: String? = null
    private var mSportCode: String? = null
    private var matchOdd: MatchOdd? = null

    private lateinit var dataBinding: FragmentOddsDetailBinding

    private var oddsDetailListAdapter: OddsDetailListAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSportCode = args.sportType.code
        matchId = args.matchId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_odds_detail, container, false)
        dataBinding.apply {
            view = this@OddsDetailFragment
            gameViewModel = this@OddsDetailFragment.viewModel
            lifecycleOwner = this@OddsDetailFragment.viewLifecycleOwner
        }
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        observeData()
        observeSocketData()
    }


    override fun onStart() {
        super.onStart()
        getData()
    }


    private fun observeSocketData() {
        receiver.matchOddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            //TODO Cheryl: 改變UI (取odds list 中的前兩個, 做顯示判斷, 根據)
            val newList = arrayListOf<OddsDetailListData>()

            it.odds?.forEach { map ->
                val key = map.key
                val value = map.value
                val filteredOddList = mutableListOf<Odd>()
                value.odds?.forEach { odd ->
                    if (odd != null)
                        filteredOddList.add(odd)
                }
                newList.add(
                    OddsDetailListData(
                        key,
                        TextUtil.split(value.typeCodes),
                        value.name,
                        filteredOddList
                    )
                )
            }

            oddsDetailListAdapter?.updatedOddsDetailDataList = newList
        })

        receiver.producerUp.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            service.unsubscribeAllEventChannel()
            service.subscribeEventChannel(matchId)
        })

    }


    private fun initUI() {

        (dataBinding.rvDetail.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        oddsDetailListAdapter = OddsDetailListAdapter(this@OddsDetailFragment).apply {
            sportCode = mSportCode
        }

        dataBinding.rvDetail.apply {
            adapter = oddsDetailListAdapter
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        }

        tv_more.visibility = View.GONE
        tv_more.setOnClickListener {
            parentFragmentManager.let {
                matchId?.let { id ->
                    OddsDetailMoreFragment.newInstance(
                        id,
                        object : OddsDetailMoreFragment.ChangeGameListener {
                            override fun refreshData(mid: String) {
                                matchId = mid
                                getData()
                            }
                        }).apply {
                        show(it, OddsDetailMoreFragment::class.java.simpleName)
                    }
                }
            }
        }

        tab_cat.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { t ->
                    viewModel.playCateListResult.value?.peekContent()?.rows?.get(t)?.code?.let {
                        (dataBinding.rvDetail.adapter as OddsDetailListAdapter).notifyDataSetChangedByCode(
                            it
                        )
                    }
                }
            }
        }
        )
    }


    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.playCateListResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                result.success.let { success ->
                    if (success) {
                        dataBinding.tabCat.removeAllTabs()
                        if (result.rows.isNotEmpty()) {
                            for (row in result.rows) {
                                dataBinding.tabCat.addTab(
                                    dataBinding.tabCat.newTab().setText("   ${row.name}   "),
                                    false
                                )
                            }
                        } else {
                            dataBinding.tabCat.visibility = View.GONE
                        }
                    }
                }
            }
        })

        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->

                matchOdd = result.oddsDetailData?.matchOdd

                result.oddsDetailData?.matchOdd?.matchInfo?.startTime?.let { time ->
                    val strTime = TimeUtil.stampToDateInOddsDetail(time.toLong())
                    val color = ContextCompat.getColor(requireContext(), R.color.colorRedDark)
                    val startPosition = strTime.length - TIME_LENGTH
                    val endPosition = strTime.length
                    val style = SpannableStringBuilder(strTime)
                    style.setSpan(
                        ForegroundColorSpan(color),
                        startPosition,
                        endPosition,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    dataBinding.tvTime.text = style
                }

                result.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                    result.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                        val strVerse = getString(R.string.verse_)
                        val strMatch = "$home${strVerse}$away"
                        val color = ContextCompat.getColor(requireContext(), R.color.colorOrange)
                        val startPosition = strMatch.indexOf(strVerse)
                        val endPosition = startPosition + strVerse.length
                        val style = SpannableStringBuilder(strMatch)
                        style.setSpan(
                            ForegroundColorSpan(color),
                            startPosition,
                            endPosition,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        dataBinding.tvMatch.text = style

                        oddsDetailListAdapter?.homeName = home
                        oddsDetailListAdapter?.awayName = away

                    }
                }
            }
        })

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { list ->
                if (list.isNotEmpty()) {
                    oddsDetailListAdapter?.oddsDetailDataList = list
                    dataBinding.tabCat.getTabAt(0)?.select()
                } else {
                    navGameInPlay()
                }
            }
        })

        viewModel.oddsDetailMoreList.observe(this.viewLifecycleOwner, {
            //扣除當前的賽事
            it?.size?.let { count ->
                tv_more.visibility = if (count - 1 == 0) View.GONE else View.VISIBLE
            }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.betInfoList = it
        })

        viewModel.betInfoResult.observe(this.viewLifecycleOwner, {
            val eventResult = it.getContentIfNotHandled()
            eventResult?.success?.let { success ->
                if (!success && eventResult.code != HttpError.BET_INFO_CLOSE.code) {
                    showErrorPromptDialog(getString(R.string.prompt), eventResult.msg) {}
                }
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.oddsType = it
        })

    }


    private fun getData() {
        mSportCode?.let { mSportCode ->
            matchId?.let { matchId ->
                viewModel.getPlayCateListAndOddsDetail(mSportCode, matchId)
            }
        }
    }


    fun back() {
        findNavController().navigateUp()
    }


    private fun navGameInPlay() {
        val action =
            OddsDetailFragmentDirections.actionOddsDetailFragmentToGameV3Fragment(MatchType.IN_PLAY)

        findNavController().navigate(action)
    }


    override fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData) {
        matchOdd?.let { matchOdd ->
            viewModel.updateMatchBetList(
                matchType = args.matchType,
                args.sportType,
                playCateName = oddsDetail.name,
                matchOdd = matchOdd,
                odd = odd
            )
        }
    }


    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }


    override fun onStop() {
        super.onStop()

        service.unsubscribeEventChannel(matchId)
    }


}