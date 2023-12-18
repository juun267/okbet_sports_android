package org.cxct.sportlottery.ui.maintab.menu

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.budiyev.android.codescanner.BarcodeUtils
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.tbruyelle.rxpermissions2.RxPermissions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentLeftOthersBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.news.SportNewsActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.PictureSelectUtil
import org.cxct.sportlottery.view.dialog.ScanErrorDialog
import org.cxct.sportlottery.view.dialog.ScanPhotoDialog
import org.cxct.sportlottery.view.onClick
import timber.log.Timber

class LeftOthersFragment:BindingSocketFragment<SportLeftMenuViewModel,FragmentLeftOthersBinding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity

    override fun onInitView(view: View) = binding.run {
        //初始化盘口列表
        binding.nodeHandicap
            .setTitle(getString(R.string.J117))
            .setNodeChild(viewModel.getHandicapConfig())
            .setOnChildClick {
                //改变盘口选择
                viewModel.changeHandicap(it.data as String)
            }.alwaysExpand()

        //初始化投注玩法
        binding.nodeBetRule
            .setTitle(getString(R.string.str_bet_way))
            .setNodeChild(viewModel.getBettingRulesData())
            .setOnChildClick {
                //更新投注玩法
                viewModel.updateOddsChangeOption(it.data as Int)
            }.alwaysExpand()
        initMenuItems()
    }

    private fun initMenuItems() = binding.run {
        menuAboutUs.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_aboutus_sel, R.drawable.ic_left_menu_aboutus_nor),
            R.string.B015
        ){
            close()
            getMainTabActivity().jumpToNews()
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
            menuLanguage.isSelected = !rvLanguage.isVisible
            if (menuLanguage.isSelected){
                showLanguageList()
            }
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
        }.hideBottomLine()

    }
    private fun scanQR() {

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

        scanPhotoDialog.tvAlbumClickListener = { selectAlbum() }
        scanPhotoDialog.show()
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
    private fun showLanguageList() {
        binding.rvLanguage.visible()
        if (binding.rvLanguage.adapter != null) {
            return
        }

        val languageAdapter = LanguageAdapter(LanguageManager.makeUseLanguage())
        binding.rvLanguage.layoutManager = GridLayoutManager(context, 2)
        binding.rvLanguage.adapter = languageAdapter
        languageAdapter.setOnItemClickListener { adapter, _, position ->
            viewModel.betInfoRepository.clear()
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