package org.cxct.sportlottery.ui.sport.endcard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.common.loading.LoadingAdapter
import org.cxct.sportlottery.databinding.FragmentEndcardHomeBinding
import org.cxct.sportlottery.ui.base.BaseFragment

class EndCardHomeFragment: BaseFragment<EndCardVM, FragmentEndcardHomeBinding>() {

    private lateinit var loadingHolder: Gloading.Holder

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loadingHolder = Gloading
            .from(LoadingAdapter(bgColor = context().getColor(R.color.color_0E131F)))
            .wrap(super.createRootView(inflater, container, savedInstanceState))
        return loadingHolder.wrapper
    }

    override fun loading(message: String?) {
        loadingHolder.showLoading()
    }

    override fun dismissLoading() {
        loadingHolder.showLoadSuccess()
    }

    override fun onInitView(view: View) {
        binding.root.setBackgroundColor(Color.TRANSPARENT)
        loading()
        binding.root.postDelayed({ dismissLoading() }, 15_000)
    }
}