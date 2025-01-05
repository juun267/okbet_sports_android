package org.cxct.sportlottery.ui.profileCenter.pointshop.order


import android.app.Activity
import android.content.Intent
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_receive_info.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityReceiveInfoBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopViewModel
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.view.checkRegisterListener

class ReceiveInfoActivity: BaseActivity<PointShopViewModel, ActivityReceiveInfoBinding>() {

    override fun pageName() = "积分商城收货信息"

    override fun onInitView() {
        setStatusbar(R.color.color_F0F5FA, true)
        initView()
    }
    private fun initView()=binding.run{
        toolBar.binding.root.setBackgroundResource(R.color.color_F0F5FA)
        toolBar.setOnBackPressListener { finish() }
        tvSure.setOnClickListener {
            val intent = Intent().apply {
                putExtra("customerName", etName.text.toString().trim())
                putExtra("customerPhone",etPhone.text.toString().trim())
                putExtra("customerAddress",etAddress.text.toString().trim())
            }
           setResult(Activity.RESULT_OK,intent)
           finish()
        }
        etName.setText(intent?.getStringExtra("customerName"))
        etPhone.setText(intent?.getStringExtra("customerPhone"))
        etAddress.setText(intent?.getStringExtra("customerAddress"))
        etName.checkRegisterListener {
            checkBtnEnable()
        }
        etPhone.checkRegisterListener {
           val errTips = when {
                it.isBlank() -> getString(R.string.error_input_empty)
                !VerifyConstUtil.verifyPhoneByLength10(it) -> {
                    getString(R.string.pls_enter_correct_mobile)
                }
                else -> null
            }
            binding.tvPhoneTips.text = errTips
            binding.tvPhoneTips.isVisible = !errTips.isNullOrEmpty()
            checkBtnEnable()
        }
        etAddress.checkRegisterListener {
            checkBtnEnable()
        }
        checkBtnEnable()
    }
    private fun checkBtnEnable(){
        val completeName = !etName.text.isNullOrEmpty()
        val completePhone = VerifyConstUtil.verifyPhoneByLength10(etPhone.text.trim())
        val completeAddress = !etAddress.text.isNullOrEmpty()
        binding.tvSure.setBtnEnable(completeName&&completePhone&&completeAddress)
    }

}