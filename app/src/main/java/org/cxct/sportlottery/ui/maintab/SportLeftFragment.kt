package org.cxct.sportlottery.ui.maintab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_sport_left.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.isHandicapShowSetup
import org.cxct.sportlottery.util.observe

class SportLeftFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    companion object {
        fun newInstance(): SportLeftFragment {
            val args = Bundle()
            val fragment = SportLeftFragment()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_sport_left, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initOddsTypeView()
        initObserver()
        getOddsType()
        viewModel.getInPlayCount()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initView() {

        lin_odds_type.setOnClickListener {
            isExpendOddsType = !isExpendOddsType
            rv_odds_type.isVisible = isExpendOddsType
            lin_odds_type.isSelected = isExpendOddsType
            lin_odds_type.isSelected = isExpendOddsType
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
//        viewModel.countByInPlay.observe(viewLifecycleOwner) {
//            tv_inplay_count.text = it.toString()
//        }
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


}