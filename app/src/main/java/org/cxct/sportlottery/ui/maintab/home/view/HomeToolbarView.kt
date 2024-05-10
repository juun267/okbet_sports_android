package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.list.OKPlaySelectPop
import org.cxct.sportlottery.ui.sport.list.TodayMenuPop
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.StreamerTextView
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import splitties.views.padding

class HomeToolbarView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : FrameLayout(context, attrs, defStyle) {

    init {
        12.dp.let { setPadding(6.dp, 6.dp, it, it) }
        addChildView()
    }

    private lateinit var ivLogo: ImageView
    private lateinit var searchView: View
    lateinit var searchIcon: View
    lateinit var betlistIcon: View
    private lateinit var userMoneyView: LinearLayout
    private lateinit var banlanceView: LinearLayout
    lateinit var tvUserMoney: TextView
    private lateinit var ivRefreshMoney: ImageView
    private lateinit var btnDeposit: TextView
    private lateinit var ivMails: ImageView
    private lateinit var loginLayout: LinearLayout
    private lateinit var tvLogin: TextView
    private lateinit var tvRegist: TextView

    private lateinit var fragment: LifecycleOwner
    private lateinit var logoEvent: View.OnClickListener
    private lateinit var viewModel: BaseSocketViewModel
    private var userModelEnable = true
    private var onlyShowSeach = true
    private var mailsIcon = R.drawable.icon_mails
    private var mailsIcon1 = R.drawable.icon_mails1
    private var linOKPlay: LinearLayout?=null
    var onPlaySelectListener: ((position:Int) -> Unit)?=null
    private val okPlaySelectPop by lazy { OKPlaySelectPop(context){ position ->
        onPlaySelectListener?.invoke(position)
    }
    }

    private fun addChildView() {


        ivLogo = AppCompatImageView(context)
        ivLogo.setImageResource(R.drawable.logo_okbet_color)
        addView(ivLogo, LayoutParams(-2, 36.dp).apply {
            leftMargin = 6.dp
        })

        addSearchView()
        addUserView()
        addLoginBtn()
        fitsSystemStatus()
        updateMailsIcon()
    }

    private fun addSearchView() {

        searchView = LinearLayout(context).apply { gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL }
        val wh = 24.dp
        searchIcon = AppCompatImageView(context).apply {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_search_home)
            DrawableCompat.setTint(drawable!!.mutate(), ContextCompat.getColor(context, R.color.color_0651e5))
            setImageDrawable(drawable)
            minimumHeight = wh
            minimumWidth = wh
            (searchView as ViewGroup).addView(this)
        }
        betlistIcon = AppCompatImageView(context).apply {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_betlist_home)
            DrawableCompat.setTint(drawable!!.mutate(), ContextCompat.getColor(context, R.color.color_0651e5))
            setImageDrawable(drawable)
            minimumHeight = wh
            minimumWidth = wh
            (searchView as ViewGroup).addView(this, LayoutParams(-2,-2).apply { leftMargin = 12.dp })
        }
        addView(searchView, LayoutParams(-1, 26.dp).apply {
            leftMargin = 16.dp
            gravity = Gravity.BOTTOM
        })
    }

    private fun addUserView() {
        userMoneyView = LinearLayout(context).apply {
            2.dp.let { setPadding(0, it, 0, it) }
        }

        val dp32 = 32.dp
        banlanceView = LinearLayout(context)
        banlanceView.gravity = Gravity.CENTER_VERTICAL
        6.dp.let { banlanceView.setPadding(it, it, it, it) }
        banlanceView.background = DrawableCreatorUtils.getCommonBackgroundStyle(20.dp, R.color.color_20b8d2f8, R.color.color_b8d2f8, 1)
        userMoneyView.addView(banlanceView, LinearLayout.LayoutParams(-2, dp32))

        tvUserMoney = AppCompatTextView(context).apply {
            typeface = Typeface.DEFAULT_BOLD
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            setTextColor(resources.getColor(R.color.color_FFFFFF_414655))
            banlanceView.addView(this, LayoutParams(-2, -2))
        }

        ivRefreshMoney = ImageView(context).apply {
            setImageResource(R.drawable.ic_refresh_green)
            val ivParams = LayoutParams(-2, -2)
            ivParams.leftMargin = 4.dp
            ivParams.rightMargin = ivParams.leftMargin
            banlanceView.addView(this, ivParams)
        }

        btnDeposit = AppCompatTextView(context)
        val p10 = 10.dp
        btnDeposit.setPadding(p10, 0, p10, 0)
        btnDeposit.textSize = 14f
        btnDeposit.minWidth = 72.dp
        btnDeposit.gravity = Gravity.CENTER
        btnDeposit.setText(R.string.J285)
        btnDeposit.background = DrawableCreatorUtils.getGradientBackgroundStyle(20.dp, R.color.color_1DA0FF, R.color.color_0050e6)
        btnDeposit.setTextColor(Color.WHITE)
        userMoneyView.addView(btnDeposit, LayoutParams(-2, dp32).apply { leftMargin = 8.dp })

        ivMails = AppCompatImageView(context)
        ivMails.setImageResource(R.drawable.icon_mails)
        userMoneyView.addView(ivMails, LayoutParams(dp32, dp32).apply { leftMargin = 8.dp })
        ivMails.setOnClickListener { InfoCenterActivity.startWith(context, mailsNum > 0) }
        addView(userMoneyView, LayoutParams(-2, -2, Gravity.RIGHT or Gravity.BOTTOM))
    }

    private fun addLoginBtn() {
        loginLayout = LinearLayout(context).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        }

        val params = LayoutParams(-2, 30.dp)
        params.rightMargin = 8.dp
        tvLogin = createBtnText(R.string.J134, R.drawable.bg_blue_radius_15)
        tvLogin.background = DrawableCreatorUtils.getCommonBackgroundStyle(20.dp, R.color.color_20b8d2f8, R.color.color_b8d2f8, 1)
        tvLogin.setTextColor(resources.getColor(R.color.color_025BE8))
        loginLayout.addView(tvLogin, params)

        params.rightMargin = 0.dp
        tvRegist = createRegistBtnText(R.string.J151, R.drawable.bg_btn_home_register)
        loginLayout.addView(tvRegist,params)

        addView(loginLayout, LayoutParams(-2, -2, Gravity.RIGHT or Gravity.BOTTOM))
    }

    @SuppressLint("RestrictedApi")
    private fun createBtnText(text: Int, background: Int): TextView {
        return AppCompatTextView(context).apply {
            minWidth = 74.dp
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            textSize = 14f
            setText(text)
            //自动调整大小
            setAutoSizeTextTypeWithDefaults(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            setAutoSizeTextTypeUniformWithConfiguration(10, 14, 1, TypedValue.COMPLEX_UNIT_SP)
            val padding = 10.dp
            setPadding(padding, 0, padding, 0)
            setBackgroundResource(background)
            setTextColor(resources.getColor(R.color.color_FFFFFF))
        }
    }
    @SuppressLint("RestrictedApi")
    private fun createRegistBtnText(text: Int, background: Int): TextView {
        return StreamerTextView(context).apply {
            minWidth = 74.dp
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            elevation = 2.dp.toFloat()
            textSize = 14f
            setText(text)
            //自动调整大小
            setAutoSizeTextTypeWithDefaults(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            setAutoSizeTextTypeUniformWithConfiguration(10, 14, 1, TypedValue.COMPLEX_UNIT_SP)
            val padding = 10.dp
            setPadding(padding, 0, padding, 0)
            setBackgroundResource(background)
            setTextColor(resources.getColor(R.color.color_FFFFFF))
        }
    }

    private fun initObserver() = viewModel.run {
        if (userModelEnable) {
            isLogin.observe(fragment) { setupLogin() }
            userMoney.observe(fragment) {
                it?.let { bindMoneyText(it) }
            }

            InfoCenterRepository.totalUnreadMsgCount.observe(fragment) {
                mailsNum = it
                updateMailsIcon()
            }
        }
    }

    private var mailsNum = 0
    private fun updateMailsIcon() {
        if (::ivMails.isInitialized) {
            ivMails.setImageResource(if (mailsNum > 0) mailsIcon1 else mailsIcon)
        }
    }

    private fun bindMoneyText(money: Double) {
        tvUserMoney.text = "$showCurrencySign ${TextUtil.format(money)}"
        tvUserMoney.setVisibilityByMarketSwitch()
        ivRefreshMoney.setVisibilityByMarketSwitch()
    }

    private fun setupLogin() {
        if (onlyShowSeach){
            searchView.visible()
            loginLayout.gone()
            userMoneyView.gone()
            return
        }
        if (!LoginRepository.isLogined()) {
            loginLayout.visible()
            searchView.gone()
            userMoneyView.gone()
            return
        }

        loginLayout.gone()
        if (userModelEnable) {
            searchView.gone()
            userMoneyView.visible()
            bindMoneyText(LoginRepository.userMoney())
            btnDeposit.setOnClickListener {
                ToGcashDialog.showByClick { viewModel.checkRechargeKYCVerify() }
            }
        } else {
            searchView.visible()
            userMoneyView.gone()
        }

    }

    fun attach(
        lifecycleOwner: LifecycleOwner,
        mainTabActivity: MainTabActivity,
        viewModel: BaseSocketViewModel,
        moneyViewEnable: Boolean = true,
        onlyShowSeach: Boolean = false,
    ) = attach(lifecycleOwner, { mainTabActivity.backMainHome() }, viewModel, moneyViewEnable, onlyShowSeach)

    fun attach(
        lifecycleOwner: LifecycleOwner,
        logoEvent: OnClickListener,
        viewModel: BaseSocketViewModel,
        moneyViewEnable: Boolean = true,
        onlyShowSeach: Boolean = false,
    ) {
        this.fragment = lifecycleOwner
        this.logoEvent = logoEvent
        this.viewModel = viewModel
        this.userModelEnable = moneyViewEnable
        this.onlyShowSeach = onlyShowSeach

        initView()
        initObserver()
        fragment.doOnResume(-1) { onRefreshMoney() }
    }

    private fun initView() {
        setupLogin()
        userMoneyView.gone()
        tvLogin.setOnClickListener { context.startLogin() }
        tvRegist.setOnClickListener { LoginOKActivity.startRegist(context) }
        ivRefreshMoney.setOnClickListener { onRefreshMoney() }
        ivLogo.setOnClickListener(logoEvent)
    }

    private var refreshTimeTag = 0L
    private fun onRefreshMoney() {
        val time = System.currentTimeMillis()
        if (!LoginRepository.isLogined() && time - refreshTimeTag < 1500) {
            return
        }

        refreshTimeTag = time
        ivRefreshMoney.startAnimation(RotateAnimation(
            0f,
            720f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = 1000
        })
        viewModel.getMoneyAndTransferOut()
    }
    fun setupOKPlay(){
        if (OKGamesRepository.okPlayBean!=null){
            linOKPlay = LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                val tvName = AppCompatTextView(context).apply {
                    typeface = Typeface.DEFAULT_BOLD
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
                    setTextColor(resources.getColor(R.color.color_313F56))
                    addView(this, LayoutParams(-2, -2))
                    text = "OKSport"
                }
                val ivArrow = ImageView(context).apply {
                    setImageResource(R.drawable.ic_arrow_down_triangle)
                    val ivParams = LayoutParams(16.dp, 16.dp)
                    ivParams.leftMargin = 4.dp
                    addView(this, ivParams)
                }
                setOnClickListener {
                    okPlaySelectPop.apply {
                        this.tvName = tvName
                        this.ivArrow = ivArrow
                    }
                    okPlaySelectPop.showAsDropDown(this,(width-122.dp)/2,5.dp)
                }
                linOKPlay?.setPadding(5.dp,5.dp,5.dp,5.dp)
            }
            addView(linOKPlay, LayoutParams(-2, -2, Gravity.RIGHT or Gravity.BOTTOM).apply {
                rightMargin = 120.dp
            })
        }else{
            if (linOKPlay!=null){
                removeView(linOKPlay)
                linOKPlay = null
            }
        }
    }
}