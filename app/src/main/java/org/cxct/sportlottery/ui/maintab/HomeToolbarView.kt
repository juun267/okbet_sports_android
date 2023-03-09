package org.cxct.sportlottery.ui.maintab

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseOddButtonViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.startLogin

class HomeToolbarView: LinearLayout {

    companion object {
        private val textStyle by lazy { ResourcesCompat.getFont(MultiLanguagesApplication.appContext, R.font.din_bold) }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setBackgroundResource(R.color.color_B3F0F5FA)
        val padding = 14.dp
        setPadding(padding, 0, padding, 0)
        gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        addChildView()
    }

    lateinit var ivMenuLeft: ImageView
    lateinit var ivLogo: ImageView
    lateinit var searchView: LinearLayout
    lateinit var userMoneyView: LinearLayout
    lateinit var tvUserMoney: TextView
    lateinit var ivRefreshMoney: ImageView
    lateinit var loginLayout: LinearLayout
    lateinit var tvLogin: TextView

    private lateinit var fragment: LifecycleOwner
    private lateinit var activity: MainTabActivity
    private lateinit var viewModel: BaseOddButtonViewModel
    private var userModelEnable = true

    private fun addChildView() {
        ivMenuLeft = AppCompatImageView(context)
        ivMenuLeft.setImageResource(R.drawable.icon_menu)
        addView(ivMenuLeft, LayoutParams(-2, 24.dp))

        ivLogo = AppCompatImageView(context)
        ivLogo.setImageResource(R.drawable.logo_okbet_color)
        addView(ivLogo, LayoutParams(-2, 32.dp).apply { leftMargin = 10.dp })

        addSearchView()
        addUserView()
        addLoginBtn()
    }

    private fun addSearchView() {
        searchView = LinearLayout(context).apply {
            gone()
            setBackgroundResource(R.drawable.bg_search_radius_18)
            gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            val padding = 10.dp
            setPadding(padding, 0, padding, 0)
        }

        AppCompatImageView(context).run {
            val wh = 16.dp
            setImageResource(R.drawable.ic_search_home)
            searchView.addView(this, LayoutParams(wh, wh))
        }

        AppCompatTextView(context).run {
            setText(R.string.text_search)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            setTextColor(resources.getColor(R.color.color_A7B2C4))
            searchView.addView(this, LayoutParams(-2, -2).apply { leftMargin = 5.dp })
        }

        addView(searchView, LayoutParams(0, 26.dp, 1f).apply { leftMargin = 16.dp })
    }

    private fun addUserView() {
        userMoneyView = LinearLayout(context).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        }

        tvUserMoney = AppCompatTextView(context).apply {
            typeface = textStyle
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

        tvLogin = AppCompatTextView(context).apply {
            minWidth = 60.dp
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            textSize = 14f
            text =
                "${resources.getString(R.string.btn_login)} / ${resources.getString(R.string.btn_register)}"
            val padding = 10.dp
            setPadding(padding, 0, padding, 0)
            setBackgroundResource(R.drawable.bg_blue_radius_15)
            setTextColor(resources.getColor(R.color.color_FFFFFF))

            loginLayout.addView(this, LayoutParams(-2, 30.dp))
        }

        addView(loginLayout, LayoutParams(0, -2, 1f))
    }

    private fun initObserver() = viewModel.run {
        if (userModelEnable) {
            isLogin.observe(fragment) { setupLogin() }
            userMoney.observe(fragment) {
                it?.let { tvUserMoney.text = "${sConfigData?.systemCurrencySign} ${TextUtil.format(it)}" }
            }
        }
    }

    private fun setupLogin() {
        if (viewModel.isLogin.value == true) {
            loginLayout.gone()
            if (userModelEnable) {
                searchView.gone()
                userMoneyView.visible()
            } else {
                searchView.visible()
                userMoneyView.gone()
            }
            return
        }

        loginLayout.visible()
        searchView.gone()
        userMoneyView.gone()
    }

    fun attach(fragment: Fragment, activity: MainTabActivity, viewModel: BaseOddButtonViewModel, moneyViewEnable: Boolean = true) {
        this.fragment = fragment
        this.activity = activity
        this.viewModel = viewModel
        this.userModelEnable = moneyViewEnable

        initView()
        initObserver()
    }

    private fun initView() {
        layoutParams.height = 40.dp
        layoutParams.width = -1
        setupLogin()
        userMoneyView.gone()
        tvLogin.setOnClickListener { activity.startLogin() }
        ivRefreshMoney.setOnClickListener { onRefreshMoney() }
        if (fragment !is MainHomeFragment) {
            ivLogo.setOnClickListener { activity.jumpToHome(0) }
        }
    }

    fun onRefreshMoney() {
        ivRefreshMoney.startAnimation(RotateAnimation(0f,
            720f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f).apply {
            duration = 1000
        })
        viewModel.getMoney()
    }
}