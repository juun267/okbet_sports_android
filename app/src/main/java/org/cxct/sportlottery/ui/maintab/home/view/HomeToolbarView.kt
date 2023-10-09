package org.cxct.sportlottery.ui.maintab.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.doOnResume
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.StreamerTextView

class HomeToolbarView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayoutCompat(context, attrs, defStyle) {

    init {
        if (background == null) {
            setBackgroundResource(R.color.color_F8F9FD)
        }
        12.dp.let { setPadding(6.dp, it, it, it) }
        gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        addChildView()
    }

    lateinit var ivMenuLeft: ImageView
    lateinit var ivLogo: ImageView
    private lateinit var searchView: View
    lateinit var searchIcon: View
    lateinit var userMoneyView: LinearLayout
    lateinit var tvUserMoney: TextView
    lateinit var ivRefreshMoney: ImageView
    lateinit var loginLayout: LinearLayout
    lateinit var tvLogin: TextView
    lateinit var tvRegist: TextView

    private lateinit var fragment: LifecycleOwner
    private lateinit var activity: MainTabActivity
    private lateinit var viewModel: BaseOddButtonViewModel
    private var userModelEnable = true
    private var onlyShowSeach = true

    private fun addChildView() {
        ivMenuLeft = AppCompatImageView(context)
        ivMenuLeft.setImageResource(R.drawable.ic_home_menu)
        6.dp.let { ivMenuLeft.setPadding(it, it, it, it) }

        val wh = 36.dp
        addView(ivMenuLeft, LayoutParams(wh, wh))

        ivLogo = AppCompatImageView(context)
        ivLogo.setImageResource(R.drawable.logo_okbet_color)
        addView(ivLogo, LayoutParams(-2, 32.dp))

        addSearchView()
        addUserView()
        addLoginBtn()
        fitsSystemStatus()
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

        addView(searchView, LayoutParams(-1, 26.dp).apply { leftMargin = 16.dp })
    }

    private fun addUserView() {
        userMoneyView = LinearLayout(context).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        }

        tvUserMoney = AppCompatTextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            setTextColor(resources.getColor(R.color.color_FFFFFF_414655))
            userMoneyView.addView(this, LayoutParams(-2, -2))
        }

        ivRefreshMoney = ImageView(context).apply {
            setImageResource(R.drawable.ic_refresh_green)
            val ivParams = LayoutParams(-2, -2)
            ivParams.gravity = Gravity.CENTER_VERTICAL
            ivParams.leftMargin = 8.dp
            ivParams.rightMargin = 4.dp
            userMoneyView.addView(this, ivParams)
        }
        addView(userMoneyView, LayoutParams(0, -2, 1f))
    }

    private fun addLoginBtn() {
        loginLayout = LinearLayout(context).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        }

        val params = LayoutParams(-2, 30.dp)
        params.rightMargin = 8.dp
        tvLogin = createBtnText(R.string.J134, R.drawable.bg_blue_radius_15)
        loginLayout.addView(tvLogin, params)

        tvRegist = createRegistBtnText(R.string.J151, R.drawable.bg_orange_radius_15)
        loginLayout.addView(tvRegist,params)

        addView(loginLayout, LayoutParams(0, -2, 1f))
    }

    @SuppressLint("RestrictedApi")
    private fun createBtnText(text: Int, background: Int): TextView {
        return AppCompatTextView(context).apply {
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
        if (viewModel.isLogin.value != true) {
            loginLayout.visible()
            searchView.gone()
            userMoneyView.gone()
            return
        }



        loginLayout.gone()
        if (userModelEnable) {
            searchView.gone()
            userMoneyView.visible()
            bindMoneyText(viewModel.userMoney?.value ?: 0.0)
        } else {
            searchView.visible()
            userMoneyView.gone()
        }

    }

    fun attach(
        fragment: Fragment,
        activity: MainTabActivity,
        viewModel: BaseOddButtonViewModel,
        moneyViewEnable: Boolean = true,
        onlyShowSeach: Boolean = false,
    ) {
        this.fragment = fragment
        this.activity = activity
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
        tvLogin.setOnClickListener { activity.startLogin() }
        tvRegist.setOnClickListener { LoginOKActivity.startRegist(context) }
        ivRefreshMoney.setOnClickListener { onRefreshMoney() }
        if (fragment !is MainHomeFragment) {
            ivLogo.setOnClickListener { activity.backMainHome() }
        }
    }

    private var refreshTimeTag = 0L
    fun onRefreshMoney() {
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

    fun setHalloweenStyle() {
        setBackgroundResource(R.drawable.bg_home_toolbar_h)
        (ivLogo.layoutParams as LinearLayout.LayoutParams).let {
            it.height = 39.dp
            it.leftMargin = 12.dp
        }
        ivLogo.setImageResource(R.drawable.logo_okbet_color_h)
        ivMenuLeft.setImageResource(R.drawable.ic_home_menu_2)
        ivMenuLeft.setPadding(0, 0, 0, 0)
        tvLogin.setBackgroundResource(R.drawable.btn_login_h)
        tvRegist.setBackgroundResource(R.drawable.btn_register_h)
    }
}