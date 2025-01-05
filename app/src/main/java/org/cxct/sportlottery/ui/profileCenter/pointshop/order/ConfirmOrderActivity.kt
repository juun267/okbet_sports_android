package org.cxct.sportlottery.ui.profileCenter.pointshop.order


import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.UpdatePointEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityComfirmOrderBinding
import org.cxct.sportlottery.net.point.data.Product
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.view.setColors

class ConfirmOrderActivity: BaseSocketActivity<PointShopViewModel, ActivityComfirmOrderBinding>(PointShopViewModel::class) {

    companion object{
        fun start(context: Context, product: Product) {
            context.startActivity(Intent(context, ConfirmOrderActivity::class.java).apply {
                putExtra("product", product)
            })
        }
    }

    override fun pageName() = "积分商城确认订单"

    private val product by lazy { intent.getParcelableExtra<Product>("product")!! }
    private var customerName: String? = null
    private var customerPhone: String? = null
    private var customerAddress: String? = null

    override fun onInitView() {
        setStatusbar(R.color.color_F6F7F8, true)
        initView()
        initObservable()
    }

    private fun initObservable() {
        viewModel.redeemResult.collectWith(lifecycleScope){
            hideLoading()
            if (it.success){
               PurchaseResultDialog.newInstance(
                   PurchaseResultDialog.STATUS_SUCCESS,
                   getString(R.string.A093)
               ).show(supportFragmentManager)
                EventBusUtil.post(UpdatePointEvent())
            }else{
                PurchaseResultDialog.newInstance(PurchaseResultDialog.STATUS_FAIL, it.msg).show(supportFragmentManager)
            }
        }
    }

    private fun initView()=binding.run{
        toolBar.binding.root.setBackgroundResource(R.color.color_F6F7F8)
        toolBar.binding.tvToolbarTitle.setColors(R.color.color_000000)
        linAddress.isVisible= product.type==2
        product.setupProductImage(tvFundValue = tvFundValue, ivProduct = ivImage)
        tvName.text = product.name
        tvPrice.text = TextUtil.formatMoney2(product.realPoints)
        tvProductPrice.text = TextUtil.formatMoney2(product.points)
        tvDiscount.text = if (product.discount>0) "${TextUtil.formatMoney2(product.discount)}%" else null
        tvTotalPrice.text = TextUtil.formatMoney2(product.realPoints)
        toolBar.setOnBackPressListener { finish() }
        linAddress.setOnClickListener {
            resultLauncher.launch(Intent(this@ConfirmOrderActivity, ReceiveInfoActivity::class.java).apply {
                putExtra("customerName", customerName)
                putExtra("customerPhone",customerPhone)
                putExtra("customerAddress",customerAddress)
            })
        }
        tvPay.setOnClickListener {
            loading()
            viewModel.redeemProduct(product.id, customerName, customerPhone, customerAddress, 1)
        }
        checkButtonEnable()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            customerName = data?.getStringExtra("customerName")
            customerPhone = data?.getStringExtra("customerPhone")
            customerAddress = data?.getStringExtra("customerAddress")
            binding.tvAddress.text = customerAddress
            binding.tvCustomerName.text = customerName
            binding.tvCustomerPhone.text = customerPhone
            checkButtonEnable()
        }
    }
    private fun checkButtonEnable(){
        val completeAddress = if (product.type==2)
            !customerName.isNullOrEmpty()&&!customerPhone.isNullOrEmpty()&&!customerAddress.isNullOrEmpty()
        else
            true
        binding.linNameAndPhone.isVisible = !customerPhone.isNullOrEmpty()&&!customerAddress.isNullOrEmpty()
        binding.tvPay.setBtnEnable(completeAddress)
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result?.data?.let {
                customerName = it.getStringExtra("customerName")
                customerPhone = it.getStringExtra("customerPhone")
                customerAddress = it.getStringExtra("customerAddress")
                binding.tvAddress.text = customerAddress
                binding.tvCustomerName.text = customerName
                binding.tvCustomerPhone.text = "+63 ${customerPhone}"
                checkButtonEnable()
            }
        }
    }

}