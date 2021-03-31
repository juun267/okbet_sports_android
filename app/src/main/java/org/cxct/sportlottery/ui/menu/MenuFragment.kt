package org.cxct.sportlottery.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_menu.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.ToastUtil

/**
 * 遊戲右側功能選單
 */
class MenuFragment : BaseSocketFragment<MainViewModel>(MainViewModel::class) {
    private var mDownMenuListener: View.OnClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseBtn()
        initObserve()
        initSocketObserver()
        initEvent()
        setupSelectLanguage()
        setupVersion()
    }

    private fun setupCloseBtn() {
        btn_close.setOnClickListener {
            mDownMenuListener?.onClick(btn_close)
        }
    }

    private fun getMoney() {
        viewModel.getMoney()
    }

    private fun initObserve() {
        viewModel.isLogin.observe(viewLifecycleOwner, Observer {
            if (it) {
                getMoney()
            }
        })
        viewModel.userMoney.observe(viewLifecycleOwner, Observer {
            tv_money.text = "￥" + ArithUtil.toMoneyFormat(it)
        })

        viewModel.userInfo.observe(viewLifecycleOwner, Observer {
            updateUI(it?.iconUrl, it?.userName, it?.nickName)
        })
    }

    private fun initSocketObserver() {
        receiver.userMoney.observe(viewLifecycleOwner, Observer {
            tv_money.text = "￥" + ArithUtil.toMoneyFormat(it)
        })
    }

    private fun initEvent() {
        btn_change_language.setOnClickListener {
            ChangeLanguageDialog().show(parentFragmentManager, null)
            mDownMenuListener?.onClick(btn_change_language)
        }

        menu_profile_center.setOnClickListener {
            when (viewModel.userInfo.value?.testFlag) {
                TestFlag.NORMAL.index -> {
                    startActivity(Intent(context, ProfileCenterActivity::class.java))
                }
                null -> { //尚未登入
                    startActivity(Intent(context, LoginActivity::class.java))
                }
                else -> { //遊客
                    ToastUtil.showToastInCenter(context, getString(R.string.message_guest_no_permission))
                }
            }
            mDownMenuListener?.onClick(menu_profile_center)
        }

        menu_member_level.setOnClickListener {
            startActivity(Intent(context, VipActivity::class.java))
            mDownMenuListener?.onClick(menu_member_level)
        }

        menu_game_result.setOnClickListener {
            startActivity(Intent(activity, ResultsSettlementActivity::class.java))
            mDownMenuListener?.onClick(menu_game_result)
        }

        menu_version_update.setOnClickListener {
            startActivity(Intent(activity, VersionUpdateActivity::class.java))
            mDownMenuListener?.onClick(menu_version_update)
        }

        btn_sign_out.setOnClickListener {
            viewModel.doLogoutCleanUser()
            context?.run {
                MainActivity.reStart(this)
            }
            mDownMenuListener?.onClick(btn_sign_out)
        }

    }

    private fun setupSelectLanguage() {
        btn_change_language.text = when (LanguageManager.getSelectLanguage(btn_change_language.context)) {
            LanguageManager.Language.ZH -> getString(R.string.language_cn)
            LanguageManager.Language.ZHT -> getString(R.string.language_zht)
            LanguageManager.Language.VI -> getString(R.string.language_vi)
            LanguageManager.Language.EN -> getString(R.string.language_en)
        }
    }

    private fun setupVersion() {
        tv_version.text = getString(R.string.label_version, BuildConfig.VERSION_NAME)
    }

    private fun updateUI(iconUrl: String?, userName: String?, nickName: String?) {
        Glide.with(this)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
            .into(iv_head) //載入頭像

        tv_name.text = if (nickName.isNullOrEmpty()) {
            userName
        } else {
            nickName
        }
    }

    /**
     * 選單選擇結束，需透過 listener 讓上層關閉 選單
     */
    fun setDownMenuListener(listener: View.OnClickListener?) {
        mDownMenuListener = listener
    }
}