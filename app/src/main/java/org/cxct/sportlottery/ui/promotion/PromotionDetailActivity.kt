package org.cxct.sportlottery.ui.promotion

import android.content.Context
import android.content.Intent
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityPromotionDetailBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.formatHTML


class PromotionDetailActivity: BindingActivity<MainHomeViewModel, ActivityPromotionDetailBinding>() {

    companion object{
       fun start(context: Context, data: ActivityImageList){
           context.startActivity(Intent(context,PromotionDetailActivity::class.java).apply {
               putExtra("ActivityImageList",data)
           })
       }
    }
    val activityDatas:ActivityImageList? by lazy { intent?.getParcelableExtra("ActivityImageList") }
    override fun onInitView() {
      setStatusbar(R.color.color_FFFFFF, true)
      binding.customToolBar.setOnBackPressListener {
          onBackPressed()
      }
        activityDatas?.let {
            binding.okWebView.loadData((it.contentText?:"").formatHTML(),"text/html", null)
        }

    }
}