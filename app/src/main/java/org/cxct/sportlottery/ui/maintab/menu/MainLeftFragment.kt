package org.cxct.sportlottery.ui.maintab.menu

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.budiyev.android.codescanner.BarcodeUtils
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.tbruyelle.rxpermissions2.RxPermissions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentMainLeftBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.game.perya.MiniGameListFragment
import org.cxct.sportlottery.ui.maintab.home.ambassador.AmbassadorActivity
import org.cxct.sportlottery.ui.maintab.home.ambassador.AmbassadorInfo
import org.cxct.sportlottery.ui.maintab.home.news.NewsHomeFragment
import org.cxct.sportlottery.ui.maintab.menu.adapter.AmbassadorAdapter
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.invite.InviteActivity
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskCenterActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopActivity
import org.cxct.sportlottery.ui.profileCenter.vip.VipBenefitsActivity
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
    private fun initAmbassadorList() {
        if (binding.rvAmbassador.adapter != null) {
            return
        }
        val ambassadorAdapter = AmbassadorAdapter().apply {
            setList(AmbassadorInfo.ambassadorList.keys)
            setOnItemClickListener { adapter, _, position ->
//                this.selectPos = position
                startActivity(Intent(requireActivity(),AmbassadorActivity::class.java).apply {
                    putExtra(AmbassadorActivity.KEY_AMBASSADOR,AmbassadorInfo.ambassadorList[getItem(position)])
                })
            }
        }
        binding.rvAmbassador.layoutManager = GridLayoutManager(context, 4)
        binding.rvAmbassador.addItemDecoration(GridItemDecoration(10.dp, 8.dp, Color.TRANSPARENT,false))
        binding.rvAmbassador.adapter = ambassadorAdapter
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
        ConfigRepository.config.observe(this){
            binding.menuPerya.isVisible = StaticData.miniGameOpened()
            binding.menuVip.isVisible = StaticData.vipOpened()
            binding.menuInvite.isVisible = StaticData?.inviteUserOpened()
            binding.menuTaskCenter.isVisible = StaticData?.taskCenterOpened()
            binding.menuPointShop.isVisible = StaticData?.pointShopOpened()
        }
        viewModel.taskRedDotEvent.collectWith(lifecycleScope){
            binding.menuTaskCenter.ivDot().isVisible = it
        }
    }
    // 新增菜单在这里修改
    private fun initMenuItem() = binding.run {
        val cxt = binding.root.context
        menuPerya.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_perya_sel, R.drawable.ic_left_menu_perya_nor),
            R.string.P452
        ){
            close()
            getMainTabActivity().jumpToPerya()
        }.apply {
            isVisible = StaticData.miniGameOpened()
        }

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
            R.string.B24
        ){
            reSelected(menuOKLive)
            close()
            getMainTabActivity().jumpToOkLive()
        }.apply {
            isVisible = !getMarketSwitch() && StaticData.okLiveOpened()
        }
        menuOKGames.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_okgame_sel, R.drawable.ic_left_menu_okgame_nor),
            R.string.B23
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
            PromotionListActivity.startFrom(context(), "主页侧边栏菜单")
        }.apply {
            setVisibilityByMarketSwitch()
        }
        menuTaskCenter.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_taskcenter_sel, R.drawable.ic_left_menu_taskcenter_nor),
            R.string.A025
        ){
            close()
            startActivity(TaskCenterActivity::class.java)
        }.apply {
            setSummaryStatus(true,R.string.A049, ContextCompat.getColor(requireContext(),R.color.color_A7B2C4))
            isVisible = StaticData.taskCenterOpened()
            ivDot().isVisible = LoginRepository.isLogined()
        }
        menuPointShop.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_gift, R.drawable.ic_left_menu_gift),
            R.string.A051
        ){
            startActivity(PointShopActivity::class.java)
        }.apply {
            setSummaryTag(R.drawable.ic_point_tag_new)
            isVisible = StaticData.pointShopOpened()
        }
        menuInvite.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_invite_sel, R.drawable.ic_left_menu_invite_nor),
            R.string.P462
        ){
            loginedRun(requireContext(),true){
                close()
                startActivity(InviteActivity::class.java)
            }
        }.apply {
            isVisible = StaticData.inviteUserOpened()
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
        menuVip.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_vip_sel, R.drawable.ic_left_menu_vip_sel),
            R.string.P371
        ){
            loginedRun(requireContext(),true){
                close()
                startActivity(VipBenefitsActivity::class.java)
            }
        }.apply {
            isVisible = StaticData.vipOpened()
        }
        menuNews.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_news_sel, R.drawable.ic_left_menu_news_nor),
            R.string.N909
        ){
            reSelected(menuNews)
            close()
            getMainTabActivity().jumpToNews()
        }
        menuAmbassador.setItem(
            cxt.getIconSelector(R.drawable.ic_left_menu_ambassador_sel, R.drawable.ic_left_menu_ambassador_nor),
            R.string.P471
        ) {
            initAmbassadorList()
            val selected = rvAmbassador.isGone
            menuAmbassador.isSelected = selected
            menuAmbassador.isEnabled = false
            menuAmbassador.showBottomLine(!selected)
            rvAmbassador.isVisible = selected
            menuAmbassador.ivArrow()
                .animate()
                .rotation(if (selected) 90f else 0f)
                .withEndAction { menuAmbassador.isEnabled = true }
                .start()
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
            requireActivity().jumpToKYC()
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
            MiniGameListFragment::class.java-> binding.menuPerya
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

        VerifiedType.getVerifiedType(userInfo).let{
            setVerify(enable = true, clickAble = true,
                text = it.nameResId,
                statusColor = ContextCompat.getColor(requireContext(),it.colorResId))
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