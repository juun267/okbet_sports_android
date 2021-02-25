package org.cxct.sportlottery.ui.vip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.stx.xhb.androidx.XBanner
import com.stx.xhb.androidx.entity.BaseBannerInfo
import com.stx.xhb.androidx.transformers.Transformer
import kotlinx.android.synthetic.main.activity_vip.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.base.BaseNoticeViewModel
import org.cxct.sportlottery.ui.home.MainViewModel

class VipActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip)

        initView()
    }

    private fun initView() {
        bubble_level_one.isSelected = true
        banner_vip_level.apply {
            setPageTransformer(Transformer.Default)
            setBannerData(R.layout.item_banner_member_level, setupBannerLevelRequirement())
            loadImage { banner, model, view, position ->
                val tvLevel: TextView? = view?.findViewById(R.id.tv_level)
                val ivLevel: ImageView? = view?.findViewById(R.id.iv_level_icon)
                //TODO Dean : 階級會員名稱、所需成長值
                val tvLevelName: TextView? = view?.findViewById(R.id.tv_level_name)
                val tvGrowthRequirement: TextView? = view?.findViewById(R.id.tv_growth_requirement)
                val dataItem = (model as BannerLevelCard)
                tvLevel?.text = getString(dataItem.xBannerUrl.level)
                ivLevel?.setImageDrawable(ContextCompat.getDrawable(this@VipActivity, dataItem.xBannerUrl.levelIcon))
            }
        }
    }

    private fun setupBannerLevelRequirement(): List<BannerLevelCard> {
        //TODO Dean : 根據api回傳更新該階級所需成長值
        return mutableListOf<BannerLevelCard>().apply {
            Level.values().forEach {
                this.add(BannerLevelCard(it.levelRequirement))
            }
        }
    }
}