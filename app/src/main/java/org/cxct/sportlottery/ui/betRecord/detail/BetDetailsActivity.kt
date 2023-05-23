package org.cxct.sportlottery.ui.betRecord.detail

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBetDetailsBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.betList.BetListViewModel
import org.cxct.sportlottery.util.setupSportStatusChange

/**
 * 串关组合详情
 */
class BetDetailsActivity : BaseSocketActivity<BetListViewModel>(BetListViewModel::class){

    private lateinit var binding: ActivityBetDetailsBinding

    private val betDetailsFragment by lazy { BetDetailsFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        binding = ActivityBetDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initData()
        replaceFragment(R.id.fl_container,betDetailsFragment)
    }

    private fun initData(){
        val bundle = Bundle()
        var data: Row? = intent.getParcelableExtra("data")
        if (data == null) {
            val detailRow: org.cxct.sportlottery.network.bet.settledDetailList.Row? =
                intent?.getParcelableExtra("detailRow")
            if (detailRow != null) {
                bundle.putParcelable("detailRow", detailRow)
            }
        } else {
            bundle.putParcelable("data", data)
        }
        betDetailsFragment.arguments = bundle

        //体育服务关闭  需要退出页面
        setupSportStatusChange(this){
            finish()
        }

    }


    private fun initView() {
        custom_tool_bar.setOnBackPressListener {
            finish()
        }
    }

    open fun setTitleName(title: String) {
        binding.customToolBar.titleText = title
    }


}