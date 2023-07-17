package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.stx.xhb.androidx.XBanner
import kotlinx.android.synthetic.main.layout_home_top.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.LayoutHomeTopBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeFragment
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.promotion.PromotionDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import timber.log.Timber

class HomeTopView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), XBanner.OnItemClickListener {
    private val venuesAdapter=RecyclerVenuesAdapter()
    val binding: LayoutHomeTopBinding

    companion object{
        const val OkSport="OKSports"
        const val OkGame="OKGames"
        const val OkBingo="OKBingo"
        const val OkLive="OKLive"
    }
    init {
        orientation = VERTICAL
        binding = LayoutHomeTopBinding.inflate(LayoutInflater.from(context), this)

        binding.recyclerVenues.layoutManager=GridLayoutManager(context,2)
        binding.recyclerVenues.adapter=venuesAdapter
        initLogin()
        initSportEnterStatus()
        initHomeVenues()
    }

    /**
     * 检测体育服务是否关闭
     */
    @SuppressLint("NotifyDataSetChanged")
    fun initSportEnterStatus() {
        venuesAdapter.notifyDataSetChanged()
    }

    private fun setUpBanner() {
        val imageType = 2
        val lang = LanguageManager.getSelectLanguage(context).key
        var imageList = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == lang && !it.imageName1.isNullOrEmpty() && !(getMarketSwitch() && it.isHidden)
        }?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })
        val loopEnable = (imageList?.size ?: 0) > 1
        if (imageList.isNullOrEmpty()) {
            return
        }
        var xbanner = findViewById<XBanner>(R.id.topBanner)
        xbanner.setHandLoop(loopEnable)
        xbanner.setOnItemClickListener(this@HomeTopView)
        xbanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }
        val host = sConfigData?.resServerHost
        val images = imageList.map {
            Timber.d("host:$host url1:${host + it.imageName1}")
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
        }
        //opt1 ->ImageType = 5,为活动轮播图
        //opt2 ->后台有配置
        //满足以上两点 -> 显示活动轮播图r
        if (images.isNotEmpty()) {
            xbanner.visible()
        }
        xbanner.setBannerData(images.toMutableList())
    }

    fun setUpPromoteBanner(list: List<ActivityImageList>){
        //优惠banne让判断是否首页显示
        val promoteImages=list.filter { it.frontPageShow==1 }
        //优惠活动
        setUpPromoteView(promoteImages)
    }
    private fun setUpPromoteView(imageList: List<ActivityImageList>) {
        val promoteAdapter =
            object : BaseQuickAdapter<ActivityImageList, BaseViewHolder>(R.layout.item_promote_view) {
                override fun convert(holder: BaseViewHolder, item: ActivityImageList) {
                    val view = holder.getView<ImageView>(R.id.ivItemPromote)
                    view.load(sConfigData?.resServerHost+item.indexImage, R.drawable.img_banner01)
                }
            }
        promoteAdapter.setNewInstance(imageList.toMutableList())
        promoteAdapter.setOnItemClickListener { adapter, view, position ->
            val itemData = promoteAdapter.getItem(position)
            PromotionDetailActivity.start(context, itemData)
        }
        binding.rcvPromote.apply {
            adapter = promoteAdapter
            if (onFlingListener == null) {
                PagerSnapHelper().attachToRecyclerView(binding.rcvPromote)
            }
        }

    }

    override fun onItemClick(banner: XBanner, model: Any, view: View, position: Int) {
        jumpToOthers(model)

    }

    private fun jumpToOthers(model: Any) {
        val jumpUrl = (model as XBannerImage).jumpUrl
        if (jumpUrl.isNullOrEmpty()) {
            return
        }

        if (jumpUrl!!.contains("sweepstakes")) {
            JumpUtil.toLottery(context, Constants.getLotteryH5Url(context, LoginRepository.token))
        } else {
            JumpUtil.toInternalWeb(context, jumpUrl, "")
        }

    }

    private fun initLogin() {
        if (LoginRepository.isLogined()) {
            binding.depositLayout.setVisibilityByMarketSwitch()
            return
        }

        binding.loginLayout.visible()
        binding.tvLogin.setOnClickListener {
            context.startActivity(
                Intent(
                    context, LoginOKActivity::class.java
                )
            )
        }
        binding.tvRegist.setOnClickListener { LoginOKActivity.startRegist(context) }

    }

    fun setup(fragment: MainHomeFragment) {
        initVenuesItemClick(fragment)
        ConfigRepository.onNewConfig(fragment) {
            setUpBanner()
            fragment.viewModel.getActivityImageListH5()
        }
        fragment.viewModel.activityImageList.observe(fragment){
            setUpPromoteBanner(it)
        }
        if (!LoginRepository.isLogined()) {
            binding.ivGoogle.setOnClickListener {
                LoginOKActivity.googleLoging(
                    context
                )
            }
            return
        }

        initRechargeClick(fragment)
    }

    private fun initRechargeClick(fragment: MainHomeFragment) {

        val depositClick = OnClickListener {
             ToGcashDialog.showByClick(fragment.viewModel){
                 fragment.viewModel.checkRechargeKYCVerify()
             }
        }

        setOnClickListeners(
            binding.tvDeposit,
            binding.ivGLive,
            binding.ivPaymaya,
            binding.ivPayX,
            binding.ivFortunepay,
        ) {
            depositClick.onClick(it)
        }


        fragment.viewModel.isRechargeShowVerifyDialog.observe(fragment.viewLifecycleOwner) {
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                VerifyIdentityDialog().show(fragment.childFragmentManager, null)
            } else {
                fragment.loading()
                fragment.viewModel.checkRechargeSystem()
            }
        }

        fragment.viewModel.rechargeSystemOperation.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                context.startActivity(Intent(context, MoneyRechargeActivity::class.java))
                return@observe
            }

            fragment.showPromptDialog(
                context.getString(R.string.prompt),
                context.getString(R.string.message_recharge_maintain)
            ) {}

        }

    }


    /**
     * 初始化首页场馆列表
     */
    private fun initHomeVenues(){
        sConfigData?.homeGamesList?.forEach {
            //市场开关   okGames和 世界杯
            if(it.gameName== OkGame||(it.gameName== OkBingo&&StaticData.worldCupOpened())){
                //开关为false
                if(!getMarketSwitch()){
                    //添加okGames
                    venuesAdapter.addData(it)
                }
            }else{
                venuesAdapter.addData(it)
            }
        }
    }


    private fun initVenuesItemClick(fragment: MainHomeFragment){
        venuesAdapter.setOnItemClickListener{_,_,position->
            val item=venuesAdapter.data[position]
            when(item.gameName){
                //体育
                OkSport->{
                    fragment.jumpToInplaySport()
                }
                OkGame->{
                    (fragment.activity as MainTabActivity).jumpToOKGames()
                }
                //bingo
                OkBingo->{
                    //开启世界杯才有点击
                    if(StaticData.worldCupOpened()){
                        (fragment.activity as MainTabActivity).jumpToWorldCup()
                    }
                }
            }
        }
    }
}