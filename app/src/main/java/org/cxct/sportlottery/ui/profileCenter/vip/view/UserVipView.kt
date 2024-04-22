package org.cxct.sportlottery.ui.profileCenter.vip.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.postDelayed
import org.cxct.sportlottery.databinding.ViewUserVipBinding
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import splitties.systemservices.layoutInflater

class UserVipView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewUserVipBinding.inflate(layoutInflater,this,true)
    lateinit var viewModel: ProfileCenterViewModel
    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() =binding.run {
        setOnClickListener {

        }
    }
    fun setup(fragment: ProfileCenterFragment) {
        viewModel = fragment.viewModel
        postDelayed(500){
            binding.vipProgressView.setProgress(50)
        }
    }

}