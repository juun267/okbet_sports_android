package org.cxct.sportlottery.ui.menu

import android.annotation.SuppressLint
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
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.favorite.MyFavoriteActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterActivity
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.sportRecord.BetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil

/**
 * 遊戲右側功能選單
 */
@SuppressLint("SetTextI18n")
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
        initEvent()
        setupVersion()
        getOddsType()
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
            if (it)
                getMoney()
        })

        viewModel.isCreditAccount.observe(viewLifecycleOwner, {
            updateUIVisibility(it)
        })

        viewModel.userMoney.observe(viewLifecycleOwner, Observer { money ->
            tv_money.text = "￥" + money?.let { it -> TextUtil.formatMoney(it) }
        })

        viewModel.userInfo.observe(viewLifecycleOwner, Observer {
            updateUI(it?.iconUrl, it?.userName, it?.nickName, it?.fullName, StaticData.getTestFlag(it?.testFlag))
        })
    }

    private fun initEvent() {

        //個人中心
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

        //我的賽事
        menu_my_favorite.setOnClickListener {
            startActivity(Intent(context, MyFavoriteActivity::class.java))
            mDownMenuListener?.onClick(menu_my_favorite)
        }

        //其他投注記錄
        menu_other_bet_record.setOnClickListener {
            startActivity(Intent(context, OtherBetRecordActivity::class.java))
            mDownMenuListener?.onClick(menu_other_bet_record)
        }

        //會員層級
        menu_member_level.setOnClickListener {
            startActivity(Intent(context, VipActivity::class.java))
            mDownMenuListener?.onClick(menu_member_level)
        }

        //賽果結算
        menu_game_result.setOnClickListener {
            startActivity(Intent(activity, ResultsSettlementActivity::class.java))
            mDownMenuListener?.onClick(menu_game_result)
        }

        //遊戲規則
        menu_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(requireContext(), Constants.getGameRuleUrl(requireContext()), getString(R.string.game_rule))
            mDownMenuListener?.onClick(menu_game_rule)
        }

        //版本更新
        menu_version_update.setOnClickListener {
            startActivity(Intent(activity, VersionUpdateActivity::class.java))
            mDownMenuListener?.onClick(menu_version_update)
        }

        //退出登入
        btn_sign_out.setOnClickListener {
            viewModel.doLogoutCleanUser {
                context?.run {
                    if (sConfigData?.thirdOpen == FLAG_OPEN)
                        MainActivity.reStart(this)
                    else
                        GameActivity.reStart(this)
                }
            }
            mDownMenuListener?.onClick(btn_sign_out)
        }
    }

    private fun setupVersion() {
        tv_version.text = getString(R.string.label_version, BuildConfig.VERSION_NAME)
    }

    private fun getOddsType() {
        viewModel.getOddsType()
    }

    private fun updateUIVisibility(isCreditAccount: Boolean){
        //其他投注記錄 信用盤 或 第三方關閉 隱藏
        menu_other_bet_record.visibility = if (isCreditAccount || sConfigData?.thirdOpen != FLAG_OPEN) {
            View.GONE
        } else {
            View.VISIBLE
        }

        menu_member_level.visibility = if (sConfigData?.thirdOpen != FLAG_OPEN) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun updateUI(iconUrl: String?, userName: String?, nickName: String?, fullName: String?, testFlag: TestFlag?) {
        Glide.with(this)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
            .into(iv_head) //載入頭像

        tv_name.text = when (testFlag) {
            TestFlag.GUEST -> fullName
            else -> {
                if (nickName.isNullOrEmpty()) {
                    userName
                } else {
                    nickName
                }
            }
        }

        menu_profile_center.visibility = if (testFlag == TestFlag.GUEST) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    /**
     * 選單選擇結束，需透過 listener 讓上層關閉 選單
     */
    fun setDownMenuListener(listener: View.OnClickListener?) {
        mDownMenuListener = listener
    }
}