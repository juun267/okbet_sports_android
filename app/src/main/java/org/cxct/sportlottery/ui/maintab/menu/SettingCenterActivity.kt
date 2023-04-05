package org.cxct.sportlottery.ui.maintab.menu

import android.os.Bundle
import android.view.Gravity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_setting_center.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.LanguageManager.makeUseLanguage


class SettingCenterActivity : BaseActivity<MainViewModel>(MainViewModel::class) {

    private val oddsTypeList by lazy {
        sConfigData?.handicapShow?.split(",")?.filter { it.isNotEmpty() }
    }
    private val oddsTypeAdapter by lazy {
        OddsTypeAdapter(oddsTypeList)
    }
    private val betWayAdapter by lazy {
        BetWayAdapter(listOf(LocalUtils.getString(R.string.accept_any_change_in_odds),
            LocalUtils.getString(R.string.accept_better_change_in_odds),
            LocalUtils.getString(R.string.accept_never_change_in_odds)))
    }

    private lateinit var languageAdapter: LanguageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_2b2b2b_ffffff, true)
        setContentView(R.layout.activity_setting_center)
        initToolbar();
        initView()
        initObserver()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.setting_center)
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun initView() {
        initOddsTypeView()
        initLanguageView()
        initBetWayView()
        getOddsType()
        iv_btn_service.setServiceClick(supportFragmentManager)
    }

    private fun initObserver() {
        viewModel.oddsType.observe(this) {
            setOddsType(it)
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
            rv_odds_type.layoutManager = GridLayoutManager(this, 2)
            rv_odds_type.adapter = oddsTypeAdapter
            MultiLanguagesApplication.mInstance.mOddsType.value?.let {
                setOddsType(it)
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
                this?.let {
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
            makeUseLanguage()
        )
        languageAdapter.setOnItemClickListener { adapter, view, position ->
            viewModel.betInfoRepository.clear()
            selectLanguage(languageAdapter.data[position])
        }
        rv_language.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_language.adapter = languageAdapter
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if (LanguageManager.getSelectLanguageName() != select.key) {
            this?.let {
                LanguageManager.saveSelectLanguage(it, select)
                MainTabActivity.reStart(it)
            }
        }
    }

    private fun initBetWayView() {
        betWayAdapter.setOnItemClickListener { adapter, view, position ->
            betWayAdapter.setSelectPos(position)
            val option: Int = when (position) {
                0 -> {
                    OddsModeUtil.accept_any_odds
                }
                1 -> {
                    OddsModeUtil.accept_better_odds
                }
                else -> {
                    OddsModeUtil.never_accept_odds_change
                }
            }
            viewModel.updateOddsChangeOption(option)
        }
        rv_betway.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_betway.adapter = betWayAdapter
        val userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        when (userInfo?.oddsChangeOption ?: 0) {
            OddsModeUtil.accept_any_odds -> betWayAdapter.setSelectPos(0)
            OddsModeUtil.accept_better_odds -> betWayAdapter.setSelectPos(1)
            OddsModeUtil.never_accept_odds_change -> betWayAdapter.setSelectPos(2)
        }

        tvOddsChangedTips.setOnClickListener {
            showOddsChangeTips()
        }
    }

    private fun showOddsChangeTips() {
        val dialog = CustomAlertDialog(this)
        dialog.setTitle(getString(R.string.str_if_accept_odds_changes_title))
        val message = """
                    ${getString(R.string.str_if_accept_odds_changes_des_subtitle)}
                    
                    ${getString(R.string.str_if_accept_odds_changes_des1)}
                    
                    ${getString(R.string.str_if_accept_odds_changes_des2)}
                    
                     ${getString(R.string.str_if_accept_odds_changes_des3)}
                """.trimIndent()
        dialog.setMessage(message)
        dialog.setCanceledOnTouchOutside(true)
        dialog.isCancelable = true
        dialog.setNegativeButtonText(null)
        dialog.setPositiveButtonText(getString(R.string.str_ok_i_got_it))
        dialog.setGravity(Gravity.START)
        dialog.mScrollViewMarginHorizon = 20
        dialog.setPositiveClickListener {
            dialog.dismiss()
        }
        dialog.show(supportFragmentManager, null)
    }
}
