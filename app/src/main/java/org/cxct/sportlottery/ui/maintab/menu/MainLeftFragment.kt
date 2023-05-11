package org.cxct.sportlottery.ui.maintab.menu

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.budiyev.android.codescanner.BarcodeUtils
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_main_left.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.PictureSelectUtil
import org.cxct.sportlottery.view.dialog.ScanErrorDialog
import org.cxct.sportlottery.view.dialog.ScanPhotoDialog
import timber.log.Timber

class MainLeftFragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private val oddsTypeList by lazy {
        sConfigData?.handicapShow?.split(",")?.filter { it.isNotEmpty() }
    }
    private val oddsTypeAdapter by lazy {
        OddsTypeAdapter(oddsTypeList)
    }

    private lateinit var languageAdapter: LanguageAdapter

    var fromPage = 0
        set(value) {
            field = value
            if (lin_home != null) {
                clearAllSelect()
                lin_home.isSelected = value == 0
                lin_live.isSelected = value == 1
                lin_slot.isSelected = value == 4
                lin_poker.isSelected = value == 5
            }
            if (isAdded) {
                viewModel.getInPlayList()
                viewModel.getLiveRoundCount()
            }
        }

    override fun layoutId() = R.layout.fragment_main_left

    override fun onBindView(view: View) {
        initView()
        initOddsTypeView()
        initLanguageView()
        initObserver()
        getOddsType()
        fromPage = fromPage
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
        lin_slot.gone()
        lin_home.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).backMainHome()
        }
        lin_sport.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToEarlySport()
        }
        lin_inplay.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToInplaySport()
        }
        lin_live.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToLive()
        }
        lin_poker.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToOKGames()
        }
        lin_slot.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToOKGames()
        }
        lin_promotion.setVisibilityByMarketSwitch()
        lin_promotion.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            JumpUtil.toInternalWeb(
                requireContext(), Constants.getPromotionUrl(
                    viewModel.token, LanguageManager.getSelectLanguage(requireContext())
                ), getString(R.string.promotion)
            )
        }
        lin_odds_type.setOnClickListener {
            var isSelected = !lin_odds_type.isSelected
            clearAllSelect()
            lin_odds_type.isSelected = isSelected
            rv_odds_type.isVisible = isSelected
        }
        lin_contactus.setServiceClick(childFragmentManager) { EventBusUtil.post(MenuEvent(false)) }
        lin_language.setOnClickListener {
            var isSelected = !lin_language.isSelected
            clearAllSelect()
            lin_language.isSelected = isSelected
            rv_language.isVisible = isSelected
        }
        lin_aboutus.setVisibilityByMarketSwitch()
        lin_aboutus.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAboutUsUrl(requireContext()),
                getString(R.string.about_us)
            )
        }
        lin_term.setVisibilityByMarketSwitch()
        lin_term.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAgreementRuleUrl(requireContext()),
                getString(R.string.terms_conditions)
            )
        }
        tv_version.text = "V${BuildConfig.VERSION_NAME}"
        lin_scan.setOnClickListener {
            val scanPhotoDialog = ScanPhotoDialog(requireContext())
            scanPhotoDialog.tvCameraScanClickListener = {
                RxPermissions(this).request(Manifest.permission.CAMERA).subscribe { onNext ->
                    if (onNext) {
                        startActivity(Intent(requireContext(), ScannerActivity::class.java))
                    } else {
                        ToastUtil.showToast(
                            requireContext(),
                            LocalUtils.getString(R.string.N980)
                        )
                    }
                }.isDisposed


            }
            scanPhotoDialog.tvAlbumClickListener = {
                selectAlbum()
            }
            scanPhotoDialog.show()
        }
    }

    private fun selectAlbum() {
        PictureSelectUtil.pictureSelect(requireActivity(),
            object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: MutableList<LocalMedia>?) {
                    val firstImage = result?.firstOrNull()
                    val bitmap = BitmapFactory.decodeFile(firstImage?.compressPath)
                    val bitResult = BarcodeUtils.decodeBitmap(bitmap)
                    Timber.d("bitmap:${bitResult}")
                    val newUrl =
                        Constants.getPrintReceiptScan(bitResult.toString())
                    if (newUrl.isNotEmpty()) {
                        JumpUtil.toInternalWeb(
                            requireContext(),
                            href = newUrl,
                             getString(R.string.N890)
                        )
                    } else {
                        val errorDialog = activity?.let { ScanErrorDialog(it) }
                        errorDialog?.show()
                    }
                }

                override fun onCancel() {
                }

            })

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
            LanguageManager.makeUseLanguage()
        )
        languageAdapter.setOnItemClickListener { adapter, view, position ->
            viewModel.betInfoRepository.clear()
            selectLanguage(languageAdapter.data[position])
        }
        rv_language.layoutManager = GridLayoutManager(context, 2)
        rv_language.adapter = languageAdapter
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if (LanguageManager.getSelectLanguageName() != select.key) {
            context?.let {
                LanguageManager.saveSelectLanguage(it, select)
                MainTabActivity.reStart(it)
            }
        }
    }

    fun clearAllSelect() {
        lin_home.isSelected = false
        lin_live.isSelected = false
        lin_slot.isSelected = false
        lin_poker.isSelected = false
        lin_language.isSelected = false
        rv_language.isVisible = false
        lin_odds_type.isSelected = false
        rv_odds_type.isVisible = false
    }


}