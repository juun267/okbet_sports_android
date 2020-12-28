package org.cxct.sportlottery.ui.odds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
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


class OddsDetailFragment : Fragment() {

    companion object {
        const val GAME_TYPE = "gameType"
        const val TYPE_NAME = "typeName"
        const val MATCH_ID = "matchId"
        const val ODDS_TYPE = "oddsType"
        const val HEIGHT = "height"

        fun newInstance(gameType: String?, typeName: String?, matchId: String, oddsType: String, height: Int) =
            OddsDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(GAME_TYPE, gameType)
                    putString(TYPE_NAME, typeName)
                    putString(MATCH_ID, matchId)
                    putString(ODDS_TYPE, oddsType)
                    putInt(HEIGHT, height)
                }
            }
    }

    private val oddsDetailViewModel: OddsDetailViewModel by viewModel()

    private var gameType: String? = null
    private var typeName: String? = null
    private var matchId: String? = null
    private var oddsType: String? = null
    private var height: Int? = null

    private val oddsDetailListData = ArrayList<OddsDetailListData>()

    private lateinit var dataBinding: FragmentOddsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameType = it.getString(GAME_TYPE)
            typeName = it.getString(TYPE_NAME)
            matchId = it.getString(MATCH_ID)
            oddsType = it.getString(ODDS_TYPE)
            height = it.getInt(HEIGHT)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_odds_detail, container, false);
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dataBinding()
        initUI()
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

        height?.let {
            rv_detail.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, it)
        }

        (rv_detail.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        list()
    }


    private fun getData() {
        matchId?.let { matchId ->
            oddsType?.let { oddsType ->
                oddsDetailViewModel.getOddsDetail(matchId, oddsType)
            }
        }

        oddsDetailViewModel.oddsDetailResult.observe(requireActivity(), Observer {

            tv_time.text = TimeUtil.stampToDate(it?.oddsDetailData?.matchOdd?.matchInfo?.startTime!!.toLong())

            it.oddsDetailData.matchOdd.odds.forEach { (key, value) ->
                oddsDetailListData.add(OddsDetailListData(key, TextUtil.split(value.typeCodes), value.name, value.odds, false))
            }

            gameType?.let { gameType ->
                oddsDetailViewModel.getPlayCateList(gameType)
            }
        })

        oddsDetailViewModel.playCateListResult.observe(requireActivity(), Observer { result ->
            when (result) {
                is PlayCateListResult -> {
                    for (element in result.rows) {
                        tab_cat.addTab(tab_cat.newTab().setText(element.name), false)
                    }
                    tab()
                }
            }
        })
    }


    private fun tab() {
        tab_cat.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                (rv_detail.adapter as OddsDetailListAdapter).notifyDataSetChangedByCode(oddsDetailViewModel.playCateListResult.value!!.rows[tab!!.position].code)
            }
        }
        )
        tab_cat.getTabAt(0)?.select()
    }


    private fun list() {
        rv_detail.apply {
            adapter = OddsDetailListAdapter(oddsDetailListData)
            layoutManager = LinearLayoutManager(requireContext())
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
                val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
                ft.remove(this@OddsDetailFragment)
                ft.commit()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        this.view?.startAnimation(animation)
    }

}