package org.cxct.sportlottery.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_menu.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainActivity
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.ui.menu.results.ResultsSettlementActivity
import org.cxct.sportlottery.util.LanguageManager

/**
 * 遊戲右側功能選單
 */
class MenuFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    companion object {
        private const val TAG = "MenuFragment"
    }

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

        initView()
        initEvent()
    }

    private fun initView() {
        tv_language.text = when (LanguageManager.getSelectLanguage(tv_language.context)) {
            LanguageManager.Language.ZH -> getString(R.string.language_cn)
            LanguageManager.Language.EN -> getString(R.string.language_en)
            else -> getString(R.string.language_en)
        }
    }

    private fun initEvent() {
        btn_change_language.setOnClickListener {
            context?.run {
                ChangeLanguageDialog(this).show()
            }
        }

        btn_close.setOnClickListener {
            mDownMenuListener?.onClick(btn_close)
        }

        menu_game_result.setOnClickListener {
            startActivity(Intent(activity, ResultsSettlementActivity::class.java))
            mDownMenuListener?.onClick(menu_game_result)
        }

        menu_sign_out.setOnClickListener {
            viewModel.logout()
            context?.run {
                MainActivity.reStart(this)
            }
        }
    }

    /**
     * 選單選擇結束，需透過 listener 讓上層關閉 選單
     */
    fun setDownMenuListener(listener: View.OnClickListener?) {
        mDownMenuListener = listener
    }
}