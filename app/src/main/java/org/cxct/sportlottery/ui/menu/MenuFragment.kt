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
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainActivity
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.LanguageManager

/**
 * 遊戲右側功能選單
 */
class MenuFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
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

        initObserve()
        initEvent()
        setupSelectLanguage()
        setupVersion()
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
            updateUI(it?.iconUrl, it?.userName)
        })
    }

    private fun initEvent() {
        btn_change_language.setOnClickListener {
            ChangeLanguageDialog().show(parentFragmentManager, null)
            mDownMenuListener?.onClick(btn_change_language)
        }

        menu_profile_center.setOnClickListener {
            startActivity(Intent(context, ProfileCenterActivity::class.java))
            mDownMenuListener?.onClick(menu_profile_center)
        }

        menu_member_level.setOnClickListener {
            //TODO 會員層級
        }

        menu_game_result.setOnClickListener {
            startActivity(Intent(activity, ResultsSettlementActivity::class.java))
            mDownMenuListener?.onClick(menu_game_result)
        }

        menu_version_update.setOnClickListener {
            //TODO 版本更新
        }

        menu_sign_out.setOnClickListener {
            viewModel.logout()
            context?.run {
                MainActivity.reStart(this)
            }
            mDownMenuListener?.onClick(menu_sign_out)
        }

    }

    private fun setupSelectLanguage() {
        tv_language.text = when (LanguageManager.getSelectLanguage(tv_language.context)) {
            LanguageManager.Language.ZH -> getString(R.string.language_cn)
            LanguageManager.Language.EN -> getString(R.string.language_en)
            else -> getString(R.string.language_en)
        }
    }

    private fun setupVersion() {
        tv_version.text = getString(R.string.label_version, BuildConfig.VERSION_NAME)
    }

    private fun updateUI(iconUrl: String?, userName: String?) {
        Glide.with(this)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_head))
            .into(iv_head) //載入頭像

        tv_name.text = userName
    }

    /**
     * 選單選擇結束，需透過 listener 讓上層關閉 選單
     */
    fun setDownMenuListener(listener: View.OnClickListener?) {
        mDownMenuListener = listener
    }
}