package org.cxct.sportlottery.ui.maintab.menu

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.budiyev.android.codescanner.BarcodeUtils
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_main_left2.*
import kotlinx.android.synthetic.main.item_view_payment_method.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentMainLeft2Binding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.news.NewsHomeFragment
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.sport.esport.ESportFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.view.MainMenuItemView
import org.cxct.sportlottery.view.PictureSelectUtil
import org.cxct.sportlottery.view.dialog.ScanErrorDialog
import org.cxct.sportlottery.view.dialog.ScanPhotoDialog
import timber.log.Timber

class MainLeftFragment2 : BindingFragment<MainViewModel, FragmentMainLeft2Binding>() {


    private fun getIconSelector(selected: Int, unSelected: Int): Drawable {
        val selectDrawable = resources.getDrawable(selected)
        val unSelecteDrawable = resources.getDrawable(unSelected)
        return DrawableCreator.Builder()
            .setSelectedDrawable(selectDrawable)
            .setUnSelectedDrawable(unSelecteDrawable)
            .setPressedDrawable(selectDrawable)
            .setUnPressedDrawable(unSelecteDrawable)
            .build()
    }

    private inline fun getMainTabActivity() = activity as MainTabActivity

    private var lastItem: MainMenuItemView? = null


    // 新增菜单在这里修改
    private fun initMenuItem() = binding.run {
        menuSport.setItem(
            getIconSelector(R.drawable.ic_left_menu_sport_sel, R.drawable.ic_left_menu_sport_nor),
            R.string.B001
        ){
            //检查是否关闭入口
            checkSportStatus(requireActivity() as BaseActivity<*>){
                lastItem=menuSport
                close()
                getMainTabActivity().jumpToInplaySport()
            }
        }.apply {
            isVisible=StaticData.okSportOpened()
        }

        menuOKLive.setItem(
            getIconSelector(R.drawable.ic_left_menu_oklive_sel, R.drawable.ic_left_menu_oklive_nor),
            R.string.P160
        ){
            lastItem=menuOKLive
            close()
            getMainTabActivity().jumpToOkLive()
        }.apply {
            isVisible = !getMarketSwitch() && StaticData.okLiveOpened()
        }

        menuOKGames.setItem(
            getIconSelector(R.drawable.ic_left_menu_okgame_sel, R.drawable.ic_left_menu_oklive_nor),
            R.string.J203
        ){
            lastItem=menuOKGames
            close()
            getMainTabActivity().jumpToOkLive()
        }.apply {
            isVisible = !getMarketSwitch() && StaticData.okGameOpened()
        }

        menuESport.setItem(
            getIconSelector(R.drawable.ic_left_menu_esport_sel, R.drawable.ic_left_menu_esport_nor),
            R.string.esports
        ){
            lastItem=menuESport
            close()
            getMainTabActivity().jumpToOkLive()
        }.apply {
            isVisible = !getMarketSwitch() && StaticData.okBingoOpened()
            hideBottomLine()
        }
        menuPromo.setItem(
            getIconSelector(R.drawable.ic_left_menu_promo_sel, R.drawable.ic_left_menu_promo_nor),
            R.string.B005
        ){
            close()
            menuPromo.bindPromoClick {}
        }.apply {
            setVisibilityByMarketSwitch()
        }
        menuAffiliate.setItem(
            getIconSelector(R.drawable.ic_left_menu_affiliate_sel, R.drawable.ic_left_menu_affiliate_nor),
            R.string.B015
        ){
            close()
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(binding.root.context),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }.apply {
            setVisibilityByMarketSwitch()
        }

        menuNews.setItem(
            getIconSelector(R.drawable.ic_left_menu_news_sel, R.drawable.ic_left_menu_news_nor),
            R.string.B015
        ){
            lastItem=menuNews
            close()
            getMainTabActivity().jumpToNews()
        }

        menuSupport.setItem(
            getIconSelector(R.drawable.ic_left_menu_custom_sel, R.drawable.ic_left_menu_custom_nor),
            R.string.LT050
        ).setServiceClick(parentFragmentManager) { close() }

        menuVerify.setItem(
            getIconSelector(R.drawable.ic_left_menu_verify_sel, R.drawable.ic_left_menu_verify_nor),
            R.string.N914
        ){
            close()
            loginedRun(requireContext()) { startActivity(VerifyIdentityActivity::class.java) }
        }

        menuAboutUs.setItem(
            getIconSelector(R.drawable.ic_left_menu_aboutus_sel, R.drawable.ic_left_menu_aboutus_nor),
            R.string.B015
        ){
            close()
            getMainTabActivity().jumpToNews()
        }
        menuLanguage.setItem(
            getIconSelector(R.drawable.ic_left_menu_language_sel, R.drawable.ic_left_menu_language_nor),
            R.string.M169
        ){
            menuLanguage.isSelected = !rvLanguage.isVisible
            if (menuLanguage.isSelected){
                showLanguageList()
            }
        }

        menuScan.setItem(
            getIconSelector(R.drawable.ic_left_menu_scan_sel, R.drawable.ic_left_menu_scan_nor),
            R.string.N908
        ){
            close()
            scanQR()
        }.hideBottomLine()

    }

    private fun close() {
        getMainTabActivity().closeDrawerLayout()
    }

    private var currentContent: Class<BaseFragment<*>>? = null

    fun openWithFragment(menuContentFragment: Class<BaseFragment<*>>?) {
        currentContent = menuContentFragment
        lastItem?.isSelected =false
        if (menuContentFragment == null) {
            return
        }
        binSelected()
    }

    private fun binSelected() = when(currentContent) {
        OKGamesFragment::class.java -> binding.menuOKGames.isSelected=true
        OKLiveFragment::class.java -> binding.menuOKLive.isSelected=true
        NewsHomeFragment::class.java -> binding.menuNews.isSelected=true
        ESportFragment::class.java -> binding.menuESport.isSelected=true
        else -> {}

    }

    override fun onInitView(view: View) {
        initView()
        initMenuItem()
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
//        promotionItem.group.setVisibilityByMarketSwitch()
//        bindVerifyStatus(UserInfoRepository.userInfo.value)
//        binSelected()
    }

    private fun initView() = binding.run {
        promotionView.setup(this@MainLeftFragment2 as BaseFragment<MainHomeViewModel>)
        ivClose.setOnClickListener { close() }
        ivHome.setOnClickListener {
            getMainTabActivity().backMainHome()
            close()
        }
    }

    private fun showLanguageList() {
        binding.rvLanguage.visible()
        if (binding.rvLanguage.adapter != null) {
            return
        }

        val languageAdapter = LanguageAdapter(LanguageManager.makeUseLanguage())
//        languageItem.tvName.text = LanguageManager.getLanguageStringResource(context)
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

    private fun initObserver() {
        viewModel.userInfo.observe(this) {
            bindVerifyStatus(userInfo = it)
        }
    }

    private fun bindVerifyStatus(userInfo: UserInfo?) = binding.run {
        if (getMarketSwitch()) {
            menuVerify.setVisibilityByMarketSwitch()
            return@run
        }
        menuVerify.isVisible = sConfigData?.realNameWithdrawVerified.isStatusOpen()
                || sConfigData?.realNameRechargeVerified.isStatusOpen() || !getMarketSwitch()

        when (userInfo?.verified) {
            ProfileActivity.VerifiedType.PASSED.value -> {
                setVerify(true, true,
                    R.string.kyc_passed,
                    resources.getColor(R.color.color_1CD219))

            }
            ProfileActivity.VerifiedType.NOT_YET.value,ProfileActivity.VerifiedType.VERIFIED_FAILED.value -> {
                setVerify(true, true,
                    R.string.kyc_unverified,
                    resources.getColor(R.color.color_FF2E00))
            }
            ProfileActivity.VerifiedType.VERIFYING.value,ProfileActivity.VerifiedType.VERIFIED_WAIT.value -> {
                setVerify(true, true, R.string.kyc_unverifing, resources.getColor(R.color.color_6D7693))

            }
            ProfileActivity.VerifiedType.REVERIFIED_NEED.value -> {
                setVerify(true, true, R.string.P211, resources.getColor(R.color.color_6D7693))

            }
            ProfileActivity.VerifiedType.REVERIFYING.value -> {
                setVerify(true, true, R.string.P196, resources.getColor(R.color.color_6D7693))
            }
            else -> {
                setVerify(true, true, R.string.kyc_unverified, resources.getColor(R.color.color_6D7693))
            }
        }
    }

    private inline fun setVerify(enable: Boolean, clickAble: Boolean, text: Int, statusColor: Int) = binding.run  {
        menuVerify.isEnabled = enable
        menuVerify.isClickable = clickAble
        menuVerify.binding.tvSummary.show()
        menuVerify.binding.tvSummary.setText(text)
        menuVerify.binding.tvSummary.setTextColor(statusColor)
        menuVerify.binding.ivArrow.isVisible = enable
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

}