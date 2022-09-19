package org.cxct.sportlottery.ui.maintab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_main_left.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.ui.profileCenter.timezone.TimeZoneActivity
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
    private lateinit var oddsTypeAdapter: OddsTypeAdapter
    private val oddsPriceList = listOf(
        "自动接受更好赔率",
        "自动接受任何赔率",
        "不接受任何赔率变动"
    )
    private lateinit var oddsPriceAdapter: OddsPriceAdapter

    private lateinit var languageAdapter: LanguageAdapter

    private var isExpendOddsType = false
    private var isExpendOddsPrice = false
    private var isExpendLanguage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_left, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initOddsTypeView()
        initOddsPriceView()
        initLanguageView()
        initObserver()
        getOddsType()
        viewModel.getMessageCount()
    }

    override fun onResume() {
        super.onResume()
        cb_appearance.isChecked = MultiLanguagesApplication.isNightMode
        tv_language.text = LanguageManager.getLanguageStringResource(requireContext())
        iv_language.setImageResource(LanguageManager.getLanguageFlag(requireContext()))

        setMessageCount(viewModel.totalUnreadMsgCount.value)
        setLogin()
    }

    private fun initView() {
        iv_menu_back.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
        }
        lin_message.setOnClickListener {
            startActivity(Intent(requireContext(), NewsActivity::class.java))
        }
        lin_odds_type.setOnClickListener {
            isExpendOddsType = !isExpendOddsType
            rv_odds_type.isVisible = isExpendOddsType
            lin_odds_type.isSelected = isExpendOddsType
        }
        lin_betting_setting.setOnClickListener {
            isExpendOddsPrice = !isExpendOddsPrice
            rv_odds_price.isVisible = isExpendOddsPrice
            lin_betting_setting.isSelected = isExpendOddsPrice
        }
        lin_language.setOnClickListener {
            isExpendLanguage = !isExpendLanguage
            rv_language.isVisible = isExpendLanguage
            lin_language.isSelected = isExpendLanguage
        }
        cb_appearance.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                MultiLanguagesApplication.saveNightMode(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                MultiLanguagesApplication.saveNightMode(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        //在線客服 (取代原有的客服懸浮按鈕)
        lin_customer.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    activity?.supportFragmentManager?.let { it1 ->
                        ServiceDialog().show(
                            it1,
                            null
                        )
                    }
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl2) }
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
                }
            }
        }
        //常見問題
        lin_question.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getFAQsUrl(requireContext()),
                getString(R.string.faqs)
            )
        }
        //常見問題
        lin_timezone.setOnClickListener {
            startActivity(Intent(requireActivity(), TimeZoneActivity::class.java))
        }
        //代理加盟
        tv_affiliate.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(requireContext()),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }
        //聯繫我們
        tv_contact.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getContactUrl(requireContext()),
                getString(R.string.contact)
            )
        }
        //關於我們
        tv_about_us.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAboutUsUrl(requireContext()),
                getString(R.string.about_us)
            )
        }

        //博彩責任
        tv_responsible.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getDutyRuleUrl(requireContext()),
                getString(R.string.responsible)
            )
        }

        //規則與條款
        tv_terms.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAgreementRuleUrl(requireContext()),
                getString(R.string.terms_conditions)
            )
        }
        //隱私權條款
        tv_privacy.setVisibilityByCreditSystem()
        tv_privacy.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getPrivacyRuleUrl(requireContext()),
                resources.getString(R.string.privacy_policy)
            )
        }

    }

    private fun initOddsTypeView() {
        if (!oddsTypeList.isNullOrEmpty()) {
            oddsTypeAdapter = OddsTypeAdapter(oddsTypeList)
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
            setLogin()
        }
        viewModel.oddsType.observe(viewLifecycleOwner) {
            setOddsType(it)
        }
        viewModel.totalUnreadMsgCount.observe(viewLifecycleOwner) {
            setMessageCount(it)
        }
    }


    private fun getOddsType() {
        MultiLanguagesApplication.mInstance.getOddsType()
    }


    private fun setOddsType(oddsType: OddsType) {
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

    private fun initOddsPriceView() {
        if (!oddsPriceList.isNullOrEmpty()) {
            oddsPriceAdapter = OddsPriceAdapter(oddsPriceList)
            oddsPriceAdapter.setOnItemClickListener { adapter, view, position ->
                oddsPriceAdapter.setSelectPos(position)
            }
            rv_odds_price.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            rv_odds_price.adapter = oddsPriceAdapter

        }
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

    private fun setMessageCount(num: Int?) {
        if (num == null) {
            tv_message_count.visibility = View.GONE
        } else {
            tv_message_count.visibility = if (num > 0) View.VISIBLE else View.GONE
            tv_message_count.text = num.toString()
        }
    }

    private fun setLogin() {
        if (viewModel.isLogin.value == true) {
            lin_message.visibility = View.VISIBLE
            lin_odds_type.visibility = View.VISIBLE
//            lin_betting_setting.visibility = View.VISIBLE
        } else {
            lin_message.visibility = View.GONE
            lin_odds_type.visibility = View.GONE
//            lin_betting_setting.visibility = View.GONE
        }
    }

}