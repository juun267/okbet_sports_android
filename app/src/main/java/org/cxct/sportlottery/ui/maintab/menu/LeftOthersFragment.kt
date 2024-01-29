package org.cxct.sportlottery.ui.maintab.menu

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.budiyev.android.codescanner.BarcodeUtils
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.tbruyelle.rxpermissions2.RxPermissions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentLeftOthersBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.news.SportNewsActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.PictureSelectUtil
import org.cxct.sportlottery.view.dialog.ScanErrorDialog
import org.cxct.sportlottery.view.dialog.ScanPhotoDialog
import timber.log.Timber

class LeftOthersFragment:BaseSocketFragment<SportLeftMenuViewModel,FragmentLeftOthersBinding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity

    override fun onInitView(view: View) = binding.run {
        //初始化盘口列表
        binding.nodeHandicap
            .setTitle(getString(R.string.J117))
            .hideBottomLine()
            .setNodeChild(viewModel.getHandicapConfig())
            .setOnChildClick {
                //改变盘口选择
                viewModel.changeHandicap(it.data as String)
            }.alwaysExpand()

        //初始化投注玩法
        binding.nodeBetRule
            .setTitle(getString(R.string.str_bet_way))
            .hideBottomLine()
            .setNodeChild(viewModel.getBettingRulesData())
            .setOnChildClick {
                //更新投注玩法
                if (LoginRepository.isLogined()){
                    viewModel.updateOddsChangeOption(it.data as Int)
                }else{
                    requireActivity().startLogin()
                }
            }.alwaysExpand()
        initMenuItems()
    }

    private fun initMenuItems() = binding.run {
        menuAboutUs.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_aboutus_sel, R.drawable.ic_left_menu_aboutus_nor),
            R.string.about_us
        ){
            close()
            JumpUtil.toInternalWeb(requireContext(),
                Constants.getAboutUsUrl(requireContext()),getString(R.string.about_us))
        }

        menuScan.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_scan_sel, R.drawable.ic_left_menu_scan_nor),
            R.string.N908
        ){
            close()
            scanQR()
        }
        menuLanguage.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_language_sel, R.drawable.ic_left_menu_language_nor),
            R.string.M169
        ){
            initLanguageList()
            val selected = rvLanguage.isGone
            menuLanguage.isSelected = selected
            menuLanguage.isEnabled = false
            menuLanguage.showBottomLine(!selected)
            rvLanguage.isVisible = selected
            menuLanguage.ivArrow()
                .animate()
                .rotation(if (selected) 90f else 0f)
                .withEndAction { menuLanguage.isEnabled = true }
                .start()
        }.apply {
            setBoldSelected(false)
        }
        menuAnnouncement.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_announcement_nor, R.drawable.ic_left_menu_announcement_nor),
            R.string.LT054COPY
        ){
            close()
            requireActivity().startActivity(SportNewsActivity::class.java)
        }
        menuResult.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_result_nor, R.drawable.ic_left_menu_result_nor),
            R.string.game_result
        ){
            close()
            startActivity(Intent(requireActivity(), ResultsSettlementActivity::class.java))
        }

        menuRules.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_rules_nor, R.drawable.ic_left_menu_rules_nor),
            R.string.N947
        ){
            close()
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getGameRuleUrl(requireContext()),
                getString(R.string.game_rule)
            )
        }.showBottomLine(false)

    }
    private fun scanQR() {

        val scanPhotoDialog = ScanPhotoDialog()
        scanPhotoDialog.tvCameraScanClickListener = {
            RxPermissions(this).request(Manifest.permission.CAMERA).subscribe { onNext ->
                if (onNext) {
                    startActivity(Intent(requireContext(), ScannerActivity::class.java))
                } else {
                    ToastUtil.showToast(
                        requireContext(), getString(R.string.N980)
                    )
                }
            }.isDisposed
        }

        scanPhotoDialog.tvAlbumClickListener = { selectAlbum() }
        scanPhotoDialog.show(childFragmentManager)
    }
    private fun selectAlbum() {
        PictureSelectUtil.pictureSelect(requireActivity(),
            object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
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
    private fun initLanguageList() {
        if (binding.rvLanguage.adapter != null) {
            return
        }
        val languageAdapter = LanguageAdapter(LanguageManager.makeUseLanguage())
        binding.rvLanguage.layoutManager = GridLayoutManager(requireContext(),2)
        binding.rvLanguage.addItemDecoration(GridItemDecoration(8.dp, 10.dp, Color.TRANSPARENT,false))
        binding.rvLanguage.adapter = languageAdapter
        languageAdapter.setOnItemClickListener { adapter, _, position ->
            BetInfoRepository.clear()
            selectLanguage(adapter.getItem(position) as LanguageManager.Language)
        }
    }
    private fun selectLanguage(select: LanguageManager.Language) {
        if (LanguageManager.getSelectLanguageName() != select.key) {
            context?.let {
                LanguageManager.saveSelectLanguage(it, select)
                MainTabActivity.reStart(it, true)
            }
        }
    }
    fun close() {
        getMainTabActivity().closeDrawerLayout()
    }
}