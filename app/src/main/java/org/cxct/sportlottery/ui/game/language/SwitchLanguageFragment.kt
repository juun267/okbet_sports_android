package org.cxct.sportlottery.ui.game.language

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.*
import kotlinx.android.synthetic.main.content_bet_info_item.view.*
import kotlinx.android.synthetic.main.content_bet_info_item_quota_detail.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.button_bet
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.button_fast_bet_setting
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.kv_keyboard
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.tv_add_to_bet_info
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.tv_current_money
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.tv_odd_content_changed
import kotlinx.android.synthetic.main.fragment_bottom_sheet_betinfo_item.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.snackbar_login_notify.view.*
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.*
import kotlinx.android.synthetic.main.view_bet_info_close_message.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBottomSheetBetinfoItemBinding
import org.cxct.sportlottery.databinding.FragmentSwitchLanguageBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.network.error.BetAddErrorParser
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.bet.list.*
import org.cxct.sportlottery.ui.bet.list.receipt.BetInfoCarReceiptDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.selflimit.SelfLimitFrozeImportantDialog
import org.cxct.sportlottery.util.*


class SwitchLanguageFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),View.OnClickListener{

    private lateinit var binding: FragmentSwitchLanguageBinding

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                (activity as GameActivity).onBackPressed()
            }
            binding.llChina -> {
                selectLanguage(LanguageManager.Language.ZH)
            }
            binding.llEnglish -> {
                selectLanguage(LanguageManager.Language.EN)
            }
            binding.llVietnam -> {
                selectLanguage(LanguageManager.Language.VI)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSwitchLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ColorDrawable(Color.TRANSPARENT);
        initView()
    }

    private fun initView(){
        binding.ivBack.setOnClickListener(this)
        binding.llEnglish.setOnClickListener(this)
        binding.llChina.setOnClickListener(this)
        binding.llVietnam.setOnClickListener(this)
        when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> {
                binding.tvChina.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_317FFF_0760D4))
            }
            LanguageManager.Language.EN -> {
                binding.tvEnglish.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_317FFF_0760D4))
            }
            LanguageManager.Language.VI -> {
                binding.tvVietnam.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_317FFF_0760D4))
            }
        }
        if(sConfigData?.supportLanguage!!.contains("zh")){
            binding.llChina.visibility = View.VISIBLE
        }
        if(sConfigData?.supportLanguage!!.contains("vi")){
            binding.llVietnam.visibility = View.VISIBLE
        }
        if(sConfigData?.supportLanguage!!.contains("en")){
            binding.llEnglish.visibility = View.VISIBLE
        }
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if(SPUtil.getInstance(context).getSelectLanguage() != select.key){
            activity?.run {
                LanguageManager.saveSelectLanguage(this, select)
//                if (sConfigData?.thirdOpen == FLAG_OPEN)
//                    MainActivity.reStart(this)
//                else
                GamePublicityActivity.reStart(this)
            }
        }
    }

}
