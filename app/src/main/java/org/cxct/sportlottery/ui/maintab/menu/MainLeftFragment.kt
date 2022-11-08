package org.cxct.sportlottery.ui.maintab.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_main_left.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.maintab.LanguageAdapter
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.EventBus

class MainLeftFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    companion object {
        fun newInstance(): MainLeftFragment {
            val args = Bundle()
            val fragment = MainLeftFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val oddsTypeList by lazy {
        sConfigData?.handicapShow?.split(",")?.filter { it.isNotEmpty() }
    }
    private val oddsTypeAdapter by lazy {
        OddsTypeAdapter(oddsTypeList)
    }

    private lateinit var languageAdapter: LanguageAdapter

    private var isExpendOddsType = false
    private var isExpendLanguage = false
    var fromPage = 0
        set(value) {
            field = value
            if (isAdded) {
                lin_home.isSelected = value == 0
                lin_live.isSelected = value == 1
                lin_slot.isSelected = value == 4
                lin_poker.isSelected = value == 5
                viewModel.getInPlayList()
                viewModel.getLiveRoundCount()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_main_left, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initOddsTypeView()
        initLanguageView()
        initObserver()
        getOddsType()
        viewModel.getInPlayList()
        viewModel.getLiveRoundCount()
    }

    override fun onResume() {
        super.onResume()
        cb_appearance.isChecked = MultiLanguagesApplication.isNightMode
        tv_language.text = LanguageManager.getLanguageStringResource(requireContext())
    }

    private fun initView() {
        lin_home.isSelected = fromPage == 0
        lin_home.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToHome(0)
        }
        lin_sport.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(MatchType.EARLY, GameType.FT)
        }
        lin_inplay.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
        }
        lin_live.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToHome(1)
        }
        lin_poker.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToHome(4)
        }
        lin_slot.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToHome(3)
        }
        lin_promotion.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getPromotionUrl(
                    viewModel.token,
                    LanguageManager.getSelectLanguage(requireContext())
                ),
                getString(R.string.promotion))
        }
        lin_odds_type.setOnClickListener {
            if (isExpendLanguage) {
                lin_language.performClick()
            }
            isExpendOddsType = !isExpendOddsType
            rv_odds_type.isVisible = isExpendOddsType
            lin_odds_type.isSelected = isExpendOddsType
        }
        lin_contactus.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(childFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(requireContext(), serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(requireContext(), serviceUrl)
                }
            }
        }
        lin_language.setOnClickListener {
            if (isExpendOddsType) {
                lin_odds_type.performClick()
            }
            isExpendLanguage = !isExpendLanguage
            rv_language.isVisible = isExpendLanguage
            lin_language.isSelected = isExpendLanguage
        }

    }

    private fun initOddsTypeView() {
        if (!oddsTypeList.isNullOrEmpty()) {
            oddsTypeAdapter.setOnItemClickListener { adapter, view, position ->
                when (oddsTypeList!![position]) {
                    HandicapType.EU.name -> selectOddsType(OddsType.EU)
                    HandicapType.HK.name -> selectOddsType(OddsType.HK)
                    HandicapType.MY.name -> selectOddsType(OddsType.MYS)
                    HandicapType.ID.name -> selectOddsType(OddsType.IDN)
                }

            }
            rv_odds_type.layoutManager = GridLayoutManager(context, 2)
            rv_odds_type.adapter = oddsTypeAdapter
            MultiLanguagesApplication.mInstance.mOddsType.value?.let {
                setOddsType(it)
            }
        }
    }

    private fun initObserver() {
        viewModel.isLogin.observe(viewLifecycleOwner) {
//            setLogin()
        }
        viewModel.oddsType.observe(viewLifecycleOwner) {
            setOddsType(it)
        }
        viewModel.countByInPlay.observe(viewLifecycleOwner) {
            tv_inplay_count.text = it.toString()
        }
        viewModel.liveRoundCount.observe(viewLifecycleOwner) {
            if (it == "0") {
                tv_live_count.text = null
            } else {
                tv_live_count.text = it
            }
        }
    }


    private fun getOddsType() {
        MultiLanguagesApplication.mInstance.getOddsType()
    }

    private fun showOddsType(oddsType: OddsType) {
        when (oddsType) {
            OddsType.EU -> {
                tv_odds_type.text = getString(R.string.odd_type_eu)
            }
            OddsType.HK -> {
                tv_odds_type.text = getString(R.string.odd_type_hk)
            }
            OddsType.MYS -> {
                tv_odds_type.text = getString(R.string.odd_type_mys)
            }
            OddsType.IDN -> {
                tv_odds_type.text = getString(R.string.odd_type_idn)
            }
        }

    }

    private fun setOddsType(oddsType: OddsType) {
        showOddsType(oddsType)
        when (isHandicapShowSetup()) {
            //有配置盤口參數
            true -> {
                val selectIndex = oddsTypeList?.indexOf(oddsType.code)
                selectIndex?.let {
                    oddsTypeAdapter.setSelectPos(it)
                }
            }
            //未配置盤口參數 使用預設的View
            false -> {
                context?.let {
                    when (oddsType) {
                        OddsType.EU -> {
                            oddsTypeAdapter.setSelectPos(0)
                        }
                        OddsType.HK -> {
                            oddsTypeAdapter.setSelectPos(1)
                        }
                        OddsType.MYS -> {
                            oddsTypeAdapter.setSelectPos(2)
                        }
                        OddsType.IDN -> {
                            oddsTypeAdapter.setSelectPos(3)
                        }
                    }
                }
            }
        }
    }

    private fun selectOddsType(oddsType: OddsType) {
        viewModel.saveOddsType(oddsType)
    }


    private fun initLanguageView() {
        languageAdapter = LanguageAdapter(
            listOf(
                LanguageManager.Language.ZH,
                LanguageManager.Language.EN,
                LanguageManager.Language.VI,
                LanguageManager.Language.TH
            )
        )
        languageAdapter.setOnItemClickListener { adapter, view, position ->
            viewModel.betInfoRepository.clear()
            selectLanguage(languageAdapter.data[position])
        }
        rv_language.layoutManager = GridLayoutManager(context, 2)
        rv_language.adapter = languageAdapter
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if (SPUtil.getInstance(context).getSelectLanguage() != select.key) {
            context?.let {
                LanguageManager.saveSelectLanguage(it, select)
                MainTabActivity.reStart(it)
            }
        }
    }


}