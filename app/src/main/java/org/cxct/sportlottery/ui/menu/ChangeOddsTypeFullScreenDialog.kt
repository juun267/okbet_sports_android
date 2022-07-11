package org.cxct.sportlottery.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_change_odd_type_full_screen.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.game.menu.LeftMenuFragment
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.isHandicapShowSetup

/**
 * @app_destination 盤口設定
 */
class ChangeOddsTypeFullScreenDialog : BaseDialog<MainViewModel>(MainViewModel::class) {

    //啟用的盤口清單
    private val oddsTypeList by lazy { sConfigData?.handicapShow?.split(",")?.filter { it.isNotEmpty() } }
    private val oddsRadioButtonIdList = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_change_odd_type_full_screen, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setWindowAnimations(R.style.LeftMenu)

        initOddsTypeView(view)
        initEvent(view)
        initObserver()
        getOddsType()
    }


    private fun initOddsTypeView(rootView: View?) {
        rootView?.apply {
            //須按照後端配置的參數進行排序, 若沒有相關參數則按照原本排序
            if (!oddsTypeList.isNullOrEmpty()) {
                oddsTypeRg.removeAllViews()

                oddsRadioButtonIdList.clear()

                oddsTypeList?.forEach { oddsType ->
                    val radioButtonLayoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.WRAP_CONTENT,
                        RadioGroup.LayoutParams.MATCH_PARENT
                    )

                    radioButtonLayoutParams.height = 48.dp

                    val radioButton = RadioButton(context)
                    with(radioButton) {
                        //配置ViewId, 選中項目時需使用此Id
                        val radioButtonId = View.generateViewId()
                        radioButton.id = radioButtonId
                        oddsRadioButtonIdList.add(radioButtonId)

                        when (oddsType) {
                            HandicapType.EU.name -> {
                                text = getString(R.string.odd_type_eu)
                                setOnClickListener {
                                    selectOddsType(OddsType.EU)
                                }
                            }
                            HandicapType.HK.name -> {
                                text = getString(R.string.odd_type_hk)
                                setOnClickListener {
                                    selectOddsType(OddsType.HK)
                                }
                            }
                            HandicapType.MY.name -> {
                                text = getString(R.string.odd_type_mys)
                                setOnClickListener {
                                    selectOddsType(OddsType.MYS)
                                }
                            }
                            HandicapType.ID.name -> {
                                text = getString(R.string.odd_type_idn)
                                setOnClickListener {
                                    selectOddsType(OddsType.IDN)
                                }
                            }
                        }

                        setButtonDrawable(R.drawable.selector_odds_type)
                        compoundDrawablePadding = 10.dp
                        setPadding(10.dp, 0, 0, 0)
                    }

                    oddsTypeRg.addView(radioButton, radioButtonLayoutParams)
                }
            } else {
                rb_eu?.setOnClickListener {
                    selectOddsType(OddsType.EU)
                }

                rb_hk?.setOnClickListener {
                    selectOddsType(OddsType.HK)
                }

                rb_mys?.setOnClickListener {
                    selectOddsType(OddsType.MYS)
                }

                rb_idn?.setOnClickListener {
                    selectOddsType(OddsType.IDN)
                }
            }
        }
    }

    private fun initEvent(rootView: View?) {
        rootView?.apply {

            img_back?.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            img_close?.setOnClickListener {
                parentFragmentManager.findFragmentByTag(LeftMenuFragment::class.java.simpleName)
                    ?.let {
                        (it as DialogFragment).dismiss()
                    }
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun initObserver() {
        viewModel.oddsType.observe(viewLifecycleOwner) {
            setOddsType(it)
        }
    }


    private fun getOddsType() {
        MultiLanguagesApplication.mInstance.getOddsType()
    }


    private fun setOddsType(oddsType: OddsType) {
        when (isHandicapShowSetup()) {
            //有配置盤口參數
            true -> {
                val radioButtonIndex = oddsTypeList?.indexOf(oddsType.code)
                radioButtonIndex?.let {
                    oddsTypeRg.check(oddsRadioButtonIdList[it])
                }
            }
            //未配置盤口參數 使用預設的View
            false -> {
                context?.let {
                    when (oddsType) {
                        OddsType.EU -> {
                            rb_eu.isChecked = true
                        }
                        OddsType.HK -> {
                            rb_hk.isChecked = true
                        }
                        OddsType.MYS -> {
                            rb_mys.isChecked = true
                        }
                        OddsType.IDN -> {
                            rb_idn.isChecked = true
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