package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentLeftSportGameBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopActivity
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskCenterActivity
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.util.*

class LeftGameFragment: BaseFragment<SportLeftMenuViewModel, FragmentLeftSportGameBinding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity

    override fun onInitView(view: View) {
        initBanners()
        initMenuItems()
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        initObservable()
        setBannerStatus()
        binding.menuSupport.setServiceClick(getMainTabActivity().supportFragmentManager){ close() }
    }
    private fun initBanners() = binding.run{
        cvOkLive.setOnClickListener {
            close()
            getMainTabActivity().jumpToOkLive()
        }
        cvOkGame.setOnClickListener {
            close()
            getMainTabActivity().jumpToOKGames()
        }
        cvESport.setOnClickListener {
            close()
            getMainTabActivity().jumpToESport()
        }
    }
    private fun initObservable() {
        ConfigRepository.config.observe(this) {
            binding.menuTaskCenter.isVisible = StaticData.taskCenterOpened()
            binding.menuPointShop.isVisible = StaticData.pointShopOpened()
        }
        viewModel.taskRedDotEvent.collectWith(lifecycleScope){
            binding.menuTaskCenter.ivDot().isVisible = it
        }
    }
    fun setBannerStatus() = binding.run{
        if (StaticData.okLiveOpened()){
            cvOkLive.show()
        }else{
            cvOkLive.gone()
        }
        if (StaticData.okGameOpened()){
            cvOkGame.show()
        }else{
            cvOkGame.gone()
        }
        if (StaticData.okBingoOpened()){
            cvESport.show()
            maintenESport.root.isVisible = getSportEnterIsClose()
            cvESport.isEnabled = !getSportEnterIsClose()
        }else{
            cvESport.gone()
        }
    }
    private fun initMenuItems() = binding.run {
        menuPromo.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_promo_sel, R.drawable.ic_left_menu_promo_nor),
            R.string.B005
        ){
            close()
            PromotionListActivity.startFrom(context(), "主页体育侧边栏菜单")
        }.apply {
            setVisibilityByMarketSwitch()
        }
        menuTaskCenter.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_taskcenter_sel, R.drawable.ic_left_menu_taskcenter_nor),
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
            requireContext().getIconSelector(R.drawable.ic_left_menu_gift, R.drawable.ic_left_menu_gift),
            R.string.A051
        ){
            startActivity(PointShopActivity::class.java)
        }.apply {
            setSummaryTag(R.drawable.ic_point_tag_new)
            isVisible = StaticData.pointShopOpened()
        }
        menuAffiliate.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_affiliate_sel, R.drawable.ic_left_menu_affiliate_nor),
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
            requireContext().getIconSelector(R.drawable.ic_left_menu_news_sel, R.drawable.ic_left_menu_news_nor),
            R.string.N909
        ){
            close()
            getMainTabActivity().jumpToNews()
        }

        menuSupport.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_custom_sel, R.drawable.ic_left_menu_custom_nor),
            R.string.LT050
        ).setServiceClick(getMainTabActivity().supportFragmentManager) { close() }

        menuVerify.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_verify_sel, R.drawable.ic_left_menu_verify_nor),
            R.string.N914
        ){
            close()
            requireActivity().jumpToKYC()
        }.showBottomLine(false)
    }
    fun close() {
        getMainTabActivity().closeDrawerLayout()
    }

}