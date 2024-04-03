package org.cxct.sportlottery.ui.maintab.menu

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.budiyev.android.codescanner.BarcodeUtils
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.tbruyelle.rxpermissions2.RxPermissions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentMainLeftBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.news.NewsHomeFragment
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.ui.sport.SportFragment
import org.cxct.sportlottery.ui.sport.esport.ESportFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.MainMenuItemView
import org.cxct.sportlottery.view.PictureSelectUtil
import org.cxct.sportlottery.view.dialog.ScanErrorDialog
import org.cxct.sportlottery.view.dialog.ScanPhotoDialog
import timber.log.Timber

class MainLeftFragment : BaseFragment<MainHomeViewModel, FragmentMainLeftBinding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity

    private var lastItem: MainMenuItemView? = null


    override fun onInitView(view: View) {
        initView()
        initMenuItem()
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
        binSelected()
    }

    private fun initView() = binding.run {
        linHead.setPadding(linHead.paddingLeft,
            ImmersionBar.getStatusBarHeight(requireActivity()),linHead.paddingRight,linHead.paddingBottom)
        promotionView.setup(this@MainLeftFragment)
        ivClose.setOnClickListener { close() }
        ivHome.setOnClickListener {
            getMainTabActivity().backMainHome()
            close()
        }
    }

    private fun initLanguageList() {
        if (binding.rvLanguage.adapter != null) {
            return
        }

        val languageAdapter = LanguageAdapter(LanguageManager.makeUseLanguage())
        binding.rvLanguage.layoutManager = GridLayoutManager(context, 2)
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

    private fun initObserver() {
        viewModel.userInfo.observe(this) {
            bindVerifyStatus(userInfo = it)
        }
    }
    // 新增菜单在这里修改
    private fun initMenuItem() = binding.run {
        val cxt = binding.root.context
        menuSport.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_sport_sel, R.drawable.ic_left_menu_sport_nor),
            R.string.B001
        ){
            //检查是否关闭入口
            checkSportStatus(requireActivity() as BaseActivity<*,*>){
                close()
                getMainTabActivity().jumpToInplaySport()
            }
        }.apply {
            isVisible=StaticData.okSportOpened()
        }
        menuOKLive.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_oklive_sel, R.drawable.ic_left_menu_oklive_nor),
            R.string.P160
        ){
            reSelected(menuOKLive)
            close()
            getMainTabActivity().jumpToOkLive()
        }.apply {
            isVisible = !getMarketSwitch() && StaticData.okLiveOpened()
        }
        menuOKGames.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_okgame_sel, R.drawable.ic_left_menu_okgame_nor),
            R.string.J203
        ){
            reSelected(menuOKGames)
            close()
            getMainTabActivity().jumpToOKGames()
        }.apply {
            isVisible = !getMarketSwitch() && StaticData.okGameOpened()
        }
        menuESport.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_esport_sel, R.drawable.ic_left_menu_esport_nor),
            R.string.esports
        ){
            reSelected(menuESport)
            close()
            getMainTabActivity().jumpToESport()
        }.apply {
            isVisible = !getMarketSwitch() && StaticData.okBingoOpened()
            showBottomLine(false)
        }
        linMainMenu.isVisible = menuSport.isVisible || menuOKLive.isVisible || menuOKGames.isVisible || menuESport.isVisible
        menuPromo.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_promo_sel, R.drawable.ic_left_menu_promo_nor),
            R.string.B005
        ){
            close()
            startActivity(PromotionListActivity::class.java)
        }.apply {
            setVisibilityByMarketSwitch()
        }
        menuAffiliate.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_affiliate_sel, R.drawable.ic_left_menu_affiliate_nor),
            R.string.B015
        ){
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(binding.root.context),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }.apply {
            setVisibilityByMarketSwitch()
        }
        menuNews.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_news_sel, R.drawable.ic_left_menu_news_nor),
            R.string.N909
        ){
            reSelected(menuNews)
            close()
            getMainTabActivity().jumpToNews()
        }

        menuSupport.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_custom_sel, R.drawable.ic_left_menu_custom_nor),
            R.string.LT050
        ).setServiceClick(getMainTabActivity().supportFragmentManager)

        menuVerify.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_verify_sel, R.drawable.ic_left_menu_verify_nor),
            R.string.N914
        ){
            close()
            if (LoginRepository.isLogined()){
                startActivity(VerifyIdentityActivity::class.java)
            }else{
                requireActivity().startLogin()
            }
        }

        menuAboutUs.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_aboutus_sel, R.drawable.ic_left_menu_aboutus_nor),
            R.string.about_us
        ){
            JumpUtil.toInternalWeb(requireContext(),
                Constants.getAboutUsUrl(requireContext()),getString(R.string.about_us))
        }

        menuLanguage.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_language_sel, R.drawable.ic_left_menu_language_nor),
            R.string.M169
        ) {
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

        menuScan.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_scan_sel, R.drawable.ic_left_menu_scan_nor),
            R.string.N908
        ){
            scanQR()
        }.showBottomLine(false)

    }

    private fun close() {
        getMainTabActivity().closeDrawerLayout()
    }

    private var currentContent: Class<BaseFragment<*,*>>? = null

    fun openWithFragment(menuContentFragment: Class<BaseFragment<*,*>>?) {
        currentContent = menuContentFragment
        lastItem?.isSelected =false
        if (menuContentFragment == null) {
            return
        }
        binSelected()
    }

    private fun binSelected()  {
        if (!isAdded){
            return
        }
        lastItem?.isSelected =false
        lastItem = when(currentContent) {
            SportFragment::class.java -> binding.menuSport
            OKGamesFragment::class.java -> binding.menuOKGames
            OKLiveFragment::class.java -> binding.menuOKLive
            ESportFragment::class.java -> binding.menuESport
            NewsHomeFragment::class.java -> binding.menuNews
            else -> null
        }
        lastItem?.isSelected=true
    }
    fun reSelected(itemView: MainMenuItemView){
        lastItem?.isSelected = false
        itemView.isSelected = true
        lastItem = itemView
    }
    private fun bindVerifyStatus(userInfo: UserInfo?) = binding.run {
        if (getMarketSwitch()) {
            menuVerify.setVisibilityByMarketSwitch()
            return@run
        }
        menuVerify.isVisible = sConfigData?.realNameWithdrawVerified.isStatusOpen()
                || sConfigData?.realNameRechargeVerified.isStatusOpen() || !getMarketSwitch()

        when (userInfo?.verified) {
            VerifiedType.PASSED.value -> {
                setVerify(enable = true, clickAble = true,
                    text = R.string.kyc_passed,
                    statusColor = ContextCompat.getColor(requireContext(),R.color.color_1CD219))

            }
            VerifiedType.NOT_YET.value,VerifiedType.VERIFIED_FAILED.value -> {
                setVerify(enable = true, clickAble = true,
                    text = R.string.kyc_unverified,
                    statusColor = ContextCompat.getColor(requireContext(),R.color.color_FF2E00))
            }
            VerifiedType.VERIFYING.value,VerifiedType.VERIFIED_WAIT.value -> {
                setVerify(enable = true,
                    clickAble = true,
                    text = R.string.kyc_unverifing,
                    statusColor = resources.getColor(R.color.color_6D7693))

            }
            VerifiedType.REVERIFIED_NEED.value -> {
                setVerify(enable = true,
                    clickAble = true,
                    text = R.string.P211,
                    statusColor = resources.getColor(R.color.color_6D7693))

            }
            VerifiedType.REVERIFYING.value -> {
                setVerify(enable = true,
                    clickAble = true,
                    text = R.string.P196,
                    statusColor = resources.getColor(R.color.color_6D7693))
            }
            else -> {
                setVerify(enable = true,
                    clickAble = true,
                    text = R.string.kyc_unverified,
                    statusColor = resources.getColor(R.color.color_FF2E00))
            }
        }
    }

    private inline fun setVerify(enable: Boolean, clickAble: Boolean, text: Int, statusColor: Int) = binding.run  {
        menuVerify.isEnabled = enable
        menuVerify.isClickable = clickAble
        menuVerify.setSummaryStatus(enable, text, statusColor)
    }

    private fun scanQR() {

        val scanPhotoDialog = ScanPhotoDialog()
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

}