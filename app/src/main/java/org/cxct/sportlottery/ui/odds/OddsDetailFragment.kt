package org.cxct.sportlottery.ui.odds

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
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
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil

class OddsDetailFragment : BaseFragment<MainViewModel>(MainViewModel::class), Animation.AnimationListener, OnOddClickListener {


    companion object {
        const val GAME_TYPE = "gameType"
        const val TYPE_NAME = "typeName"
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
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_odds_detail, container, false)
        dataBinding.apply {
            view = this@OddsDetailFragment
            oddsDetailViewModel = this@OddsDetailFragment.viewModel
            lifecycleOwner = this@OddsDetailFragment.viewLifecycleOwner
        }
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        observeData()
        getData()
        observeSocketData()
    }


    private fun observeSocketData() {
        viewModel.matchStatusChange.observe(this.viewLifecycleOwner, Observer{
            if (it == null) return@Observer
            Log.e(">>>>>", "matchStatusChange")
        })

        viewModel.matchClock.observe(viewLifecycleOwner, Observer{
            if (it == null) return@Observer
            Log.e(">>>>>", "matchClock")
        })

        viewModel.matchOddsChange.observe(viewLifecycleOwner, Observer{
            if (it == null) return@Observer
            Log.e(">>>>>", "matchOddsChange")
        })
    }


    private fun initUI() {
        tv_type_name.text = typeName

        (dataBinding.rvDetail.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        oddsDetailListAdapter = OddsDetailListAdapter(this@OddsDetailFragment)

        dataBinding.rvDetail.apply {
            adapter = oddsDetailListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        tv_more.setOnClickListener {
            parentFragmentManager.let {
                matchId?.let { id ->
                    OddsDetailMoreFragment.newInstance(id, object : OddsDetailMoreFragment.ChangeGameListener {
                        override fun refreshData(matchId: String) {
                            this@OddsDetailFragment.matchId = matchId
                            getData()
                        }
                    }).apply {
                        show(it, "")
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
                        (dataBinding.rvDetail.adapter as OddsDetailListAdapter).notifyDataSetChangedByCode(it)
                    }
                }
            }
        }
        )
    }


    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.playCateListResult.observe(this.viewLifecycleOwner, Observer { result ->
            result?.success?.let {
                if (it) {
                    dataBinding.tabCat.removeAllTabs()
                    for (row in result.rows) {
                        dataBinding.tabCat.addTab(dataBinding.tabCat.newTab().setText(row.name), false)
                    }
                }
            }
        })

        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner, Observer {

            it?.oddsDetailData?.matchOdd?.matchInfo?.startTime?.let { time ->
                dataBinding.tvTime.text = TimeUtil.stampToDate(time.toLong())
            }

            it?.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                it.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                    val strVerse = getString(R.string.verse_)
                    val strMatch = "$home${strVerse}$away"
                    val color = ContextCompat.getColor(requireContext(), R.color.text_focus)
                    val startPosition = strMatch.indexOf(strVerse)
                    val endPosition = startPosition + strVerse.length
                    val style = SpannableStringBuilder(strMatch)
                    style.setSpan(ForegroundColorSpan(color), startPosition, endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    dataBinding.tvMatch.text = style

                }
            }
        })

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner, Observer {
            oddsDetailListAdapter?.oddsDetailListData?.clear()
            oddsDetailListAdapter?.oddsDetailListData?.addAll(it)
            dataBinding.tabCat.getTabAt(0)?.select()
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, Observer {
            oddsDetailListAdapter?.setBetInfoList(it)
        })

        viewModel.isParlayPage.observe(this.viewLifecycleOwner, Observer {
            oddsDetailListAdapter?.setCurrentMatchId(if (it) matchId else null)
        })

        viewModel.betInfoResult.observe(this.viewLifecycleOwner, Observer {
            if (!it.success) {
                ToastUtil.showBetResultToast(requireActivity(), it.msg, false)
            }
        })


        socketObserve()

    }

    private fun socketObserve() {
        viewModel.matchOddsChange.observe(viewLifecycleOwner, Observer{
            if (it == null) return@Observer
            //TODO Cheryl: 改變UI (取odds list 中的前兩個, 做顯示判斷, 根據)
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


    override fun getBetInfoList(odd: Odd) {
        viewModel.getBetInfoList(listOf(org.cxct.sportlottery.network.bet.Odd(odd.id, odd.odds)))
    }


    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }


    fun back() {
        //比照h5特別處理退出動畫
        val animation: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.exit_to_right)
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

}