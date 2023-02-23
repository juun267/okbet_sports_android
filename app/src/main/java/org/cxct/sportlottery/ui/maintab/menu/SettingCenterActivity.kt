package org.cxct.sportlottery.ui.maintab.menu

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_setting_center.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.maintab.LanguageAdapter
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*


class SettingCenterActivity : BaseActivity<MainViewModel>(MainViewModel::class) {

    private val oddsTypeList by lazy {
        sConfigData?.handicapShow?.split(",")?.filter { it.isNotEmpty() }
    }
    private val oddsTypeAdapter by lazy {
        OddsTypeAdapter(oddsTypeList)
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
        getOddsType()
        iv_btn_service.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(supportFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this, serviceUrl)
                }
            }
        }
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
            listOf(
                LanguageManager.Language.EN,
                LanguageManager.Language.PHI,
                LanguageManager.Language.ZH,
                LanguageManager.Language.VI,
            )
        )
        languageAdapter.setOnItemClickListener { adapter, view, position ->
            viewModel.betInfoRepository.clear()
            selectLanguage(languageAdapter.data[position])
        }
        rv_language.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_language.adapter = languageAdapter
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if (SPUtil.getInstance(this).getSelectLanguage() != select.key) {
            this?.let {
                LanguageManager.saveSelectLanguage(it, select)
                MainTabActivity.reStart(it)
            }
        }
    }

}
