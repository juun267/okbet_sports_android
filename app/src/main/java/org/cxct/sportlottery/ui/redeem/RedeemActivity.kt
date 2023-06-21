package org.cxct.sportlottery.ui.redeem

import android.os.Bundle
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_withdraw_commission_detail.custom_tool_bar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityRedeemBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.view.dialog.RedeemDialog

class RedeemActivity : BaseSocketActivity<RedeemViewModel>(RedeemViewModel::class) {


    private lateinit var binding: ActivityRedeemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedeemBinding.inflate(layoutInflater)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(binding.root)
        initView()

    }


    private fun initView() {
        custom_tool_bar.setOnBackPressListener {
            finish()
        }
        binding.btnReset.setOnClickListener {
            binding.etRedeemCode.setText("")

        }
        binding.btnSubmit.setOnClickListener {
            val redemmCode = binding.etRedeemCode.text.toString()
            showRedeemDialog(redemmCode,"Sorry")
        }
        binding.rbtnRedeem.setOnClickListener {
            binding.layoutRedemm.isVisible = true
            binding.lineRedeem.isVisible = true
            binding.lineRedeemHis.isVisible = false
            binding.layoutHistoryRedeem.isVisible = false
        }
        binding.rbtnRedeemHis.setOnClickListener {
            binding.layoutRedemm.isVisible = false
            binding.lineRedeem.isVisible = false
            binding.lineRedeemHis.isVisible = true
            binding.layoutHistoryRedeem.isVisible = true
        }
    }


    private fun showRedeemDialog(msg: String,title: String) {
        val dialog = RedeemDialog(this)
        dialog.setContentMsg(msg)
        dialog.setTitle(title)
        dialog.show()
    }

}