package org.cxct.sportlottery.ui.odds

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil

@Suppress("DEPRECATION")
class OddsDetailFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),
    Animation.AnimationListener, OnOddClickListener {


    companion object {

        const val TIME_LENGTH = 5

        const val GAME_TYPE = "gameType"
        const val TYPE_NAME = "typeName"//leagueName
        const val MATCH_ID = "matchId"
        const val ODDS_TYPE = "oddsType"

        fun newInstance(gameType: String?, typeName: String?, matchId: String, oddsType: String) =
            OddsDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(GAME_TYPE, gameType)
                    putString(TYPE_NAME, typeName)
                    putString(MATCH_ID, matchId)
                    putString(ODDS_TYPE, oddsType)
                }
            }
    }


    private var gameType: String? = null
    private var typeName: String? = null
    private var matchId: String? = null
    private var oddsType: String? = null


    private lateinit var dataBinding: FragmentOddsDetailBinding


    private var oddsDetailListAdapter: OddsDetailListAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameType = it.getString(GAME_TYPE)
            typeName = it.getString(TYPE_NAME)
            matchId = it.getString(MATCH_ID)
            oddsType = it.getString(ODDS_TYPE)
        }

        service.subscribeEventChannel(matchId)
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
        getData()
    }


    private fun observeSocketData() {
        receiver.matchOddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            //TODO Cheryl: 改變UI (取odds list 中的前兩個, 做顯示判斷, 根據)
            val newList = arrayListOf<OddsDetailListData>()

            it.odds.forEach { map ->
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
    }


    private fun initUI() {

        (dataBinding.rvDetail.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        oddsDetailListAdapter = gameType?.let { OddsDetailListAdapter(this@OddsDetailFragment, it) }

        dataBinding.rvDetail.apply {
            adapter = oddsDetailListAdapter
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        }

        tv_more.setOnClickListener {
            parentFragmentManager.let {
                matchId?.let { id ->
                    OddsDetailMoreFragment.newInstance(
                        id,
                        object : OddsDetailMoreFragment.ChangeGameListener {
                            override fun refreshData(matchId: String) {
                                this@OddsDetailFragment.matchId = matchId
                                getData()
                            }
                        }).apply {
                        show(it, "OddsDetailMoreFragment")
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
                    viewModel.playCateListResult.value?.rows?.get(t)?.code?.let {
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
        viewModel.playCateListResult.observe(this.viewLifecycleOwner, { result ->
            result?.success?.let {
                if (it) {
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
        })

        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner, {
            it?.oddsDetailData?.matchOdd?.matchInfo?.startTime?.let { time ->
                val strTime = TimeUtil.stampToDate(time.toLong())
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

            it?.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                it.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
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

        })

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner, {
            if (it.size > 0) {
                oddsDetailListAdapter?.oddsDetailDataList?.clear()
                oddsDetailListAdapter?.oddsDetailDataList?.addAll(it)
                oddsDetailListAdapter?.notifyDataSetChanged()

                dataBinding.tabCat.getTabAt(0)?.select()
            }
        })

        viewModel.oddsDetailMoreList.observe(this.viewLifecycleOwner, {
            //扣除當前的賽事
            it?.size?.let { count ->
                if (count - 1 == 0) {
                    tv_more.visibility = View.GONE
                }
            }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.setBetInfoList(it)
        })

        viewModel.betInfoRepository.isParlayPage.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.setCurrentMatchId(if (it) matchId else null)
        })

        viewModel.betInfoResult.observe(this.viewLifecycleOwner, {
            val eventResult = it.peekContent()
            if (eventResult?.success != true) {
                showErrorPromptDialog(getString(R.string.prompt), eventResult?.msg ?: getString(R.string.unknown_error)) {}
            }
        })

    }


    private fun getData() {
        gameType?.let { gameType ->
            viewModel.getPlayCateList(gameType)
        }

        matchId?.let { matchId ->
            oddsType?.let { oddsType ->
                viewModel.getOddsDetail(matchId, oddsType)
            }
        }
    }


    fun refreshData(gameType: String?, matchId: String?) {
        this.gameType = gameType
        this.matchId = matchId
        getData()
    }


    override fun getBetInfoList(odd: Odd) {
        viewModel.getBetInfoList(listOf(org.cxct.sportlottery.network.bet.Odd(odd.id, odd.odds)))
    }


    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }


    fun back() {
        //比照h5特別處理退出動畫
        val animation: Animation =
            AnimationUtils.loadAnimation(requireActivity(), R.anim.exit_to_right)
        animation.duration = resources.getInteger(R.integer.config_navAnimTime).toLong()
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                parentFragmentManager.popBackStack()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        this.view?.startAnimation(animation)
    }


    override fun onResume() {
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                back()
                return@OnKeyListener true
            }
            false
        })
    }


    override fun onAnimationRepeat(animation: Animation?) {
    }


    override fun onAnimationEnd(animation: Animation?) {
    }


    override fun onAnimationStart(animation: Animation?) {
    }


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            val anim = AnimationUtils.loadAnimation(activity, R.anim.enter_from_right)
            anim.setAnimationListener(this)
            anim
        } else {
            null
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (matchId?.let { viewModel.checkInBetInfo(it) } == false) {
            service.unsubscribeEventChannel(matchId)
        }
        viewModel.removeOddsDetailPageValue()
    }


}