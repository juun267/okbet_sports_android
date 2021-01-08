package org.cxct.sportlottery.ui.odds

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class OddsDetailFragment : Fragment(), Animation.AnimationListener {

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

    private val oddsDetailListData = ArrayList<OddsDetailListData>()

    private lateinit var dataBinding: FragmentOddsDetailBinding

    private val oddsDetailViewModel: OddsDetailViewModel by viewModel()

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
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dataBinding()
        initUI()
        observeData()
        getData()
    }


    private fun dataBinding() {
        dataBinding.apply {
            view = this@OddsDetailFragment
            oddsDetailViewModel = this@OddsDetailFragment.oddsDetailViewModel
            lifecycleOwner = this@OddsDetailFragment
        }
    }


    private fun initUI() {
        tv_type_name.text = typeName

        (rv_detail.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        rv_detail.apply {
            adapter = OddsDetailListAdapter(oddsDetailListData)
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
                    oddsDetailViewModel.playCateListResult.value?.rows?.get(t)?.code?.let {
                        (rv_detail.adapter as OddsDetailListAdapter).notifyDataSetChangedByCode(it)
                    }
                }
            }
        }
        )
    }


    private fun observeData() {

        oddsDetailViewModel.playCateListResult.observe(requireActivity(), Observer { result ->
            when (result) {
                is PlayCateListResult -> {
                    tab_cat.removeAllTabs()
                    for (element in result.rows) {
                        tab_cat.addTab(tab_cat.newTab().setText(element.name), false)
                    }

                    matchId?.let { matchId ->
                        oddsType?.let { oddsType ->
                            oddsDetailViewModel.getOddsDetail(matchId, oddsType)
                        }
                    }
                }
            }
        })

        oddsDetailViewModel.oddsDetailResult.observe(requireActivity(), Observer {

            it?.oddsDetailData?.matchOdd?.matchInfo?.startTime?.let { time ->
                tv_time.text = TimeUtil.stampToDate(time.toLong())
            }

            oddsDetailListData.clear()
            it?.oddsDetailData?.matchOdd?.odds?.forEach { (key, value) ->
                oddsDetailListData.add(OddsDetailListData(key, TextUtil.split(value.typeCodes), value.name, value.odds, false))
            }

            tab_cat.getTabAt(0)?.select()

        })
    }


    private fun getData() {
        gameType?.let { gameType ->
            oddsDetailViewModel.getPlayCateList(gameType)
        }
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