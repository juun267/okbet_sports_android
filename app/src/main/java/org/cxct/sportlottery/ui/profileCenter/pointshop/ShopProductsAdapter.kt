package org.cxct.sportlottery.ui.profileCenter.pointshop

import androidx.core.view.isVisible
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemShopGoodsBinding
import org.cxct.sportlottery.net.point.data.Product
import org.cxct.sportlottery.util.TextUtil

class ShopProductsAdapter: BindingAdapter<Product, ItemShopGoodsBinding>() {

    override fun onBinding(position: Int, binding: ItemShopGoodsBinding, item: Product) {
        val productEnable = item.storage > 0
        val viewAlpha = if (productEnable) 1f else 0.6f

        binding.tvProductName.text = item.name
        item.setupProductDiscountView(
            tvDiscount = binding.tvDiscount,
            ivDiscount = binding.ivDiscount
        )
        item.setupProductImage(
            tvFundValue = binding.tvFundValue,
            ivProduct = binding.ivProduct
        )

        binding.ivProductValueType.setImageResource(item.valueType.moneyImage)

        binding.tvProductPrice.apply {
            text = item.getProductPrice(context)
        }

        binding.tvStorage.text = TextUtil.formatMoney2(item.storage)

        binding.root.isEnabled = productEnable
        binding.viewSoldOut.root.isVisible = !productEnable
        binding.tvProductName.alpha = viewAlpha
        binding.ivProductValueType.alpha = viewAlpha
        binding.tvProductPrice.alpha = viewAlpha
        binding.tvStorage.alpha = viewAlpha
    }

}