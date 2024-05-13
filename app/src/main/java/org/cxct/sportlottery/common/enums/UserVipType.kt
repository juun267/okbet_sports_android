package org.cxct.sportlottery.common.enums

import android.widget.ImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.repository.StaticData

object UserVipType{
    const val LEVEL_CODE_0 = "PT"
    const val LEVEL_CODE_1 = "BY"
    const val LEVEL_CODE_2 = "HJ"
    const val LEVEL_CODE_3 = "BJ"
    const val LEVEL_CODE_4 = "ZS"
    const val LEVEL_CODE_5 = "ZZ"
    const val LEVEL_CODE_6 = "v7"
    const val LEVEL_CODE_7 = "v8"
    const val LEVEL_CODE_8 = "v9"
    const val LEVEL_CODE_9 = "v10"
    const val LEVEL_CODE_10 = "v11"

    const val REWARD_TYPE_PROMOTE = 4
    const val REWARD_TYPE_BIRTHDAY = 5
    const val REWARD_TYPE_WEEKLY = 6
    const val REWARD_TYPE_PACKET = 7



    fun ImageView.setLevelIcon(levelCode: String?){
        if (!StaticData.vipOpened()||levelCode.isNullOrEmpty()){
            gone()
        }else {
            visible()
            setImageResource(when (levelCode) {
                LEVEL_CODE_0 -> R.drawable.ic_vip_lv0
                LEVEL_CODE_1 -> R.drawable.ic_vip_lv1
                LEVEL_CODE_2 -> R.drawable.ic_vip_lv2
                LEVEL_CODE_3 -> R.drawable.ic_vip_lv3
                LEVEL_CODE_4 -> R.drawable.ic_vip_lv4
                LEVEL_CODE_5 -> R.drawable.ic_vip_lv5
                LEVEL_CODE_6 -> R.drawable.ic_vip_lv6
                LEVEL_CODE_7 -> R.drawable.ic_vip_lv7
                LEVEL_CODE_8 -> R.drawable.ic_vip_lv8
                LEVEL_CODE_9 -> R.drawable.ic_vip_lv9
                LEVEL_CODE_10 -> R.drawable.ic_vip_lv10
                else -> R.color.transparent
            })
        }
    }
    fun ImageView.setLevelTagIcon(levelCode: String?){
        if (!StaticData.vipOpened()||levelCode.isNullOrEmpty()){
            gone()
        }else{
            visible()
            setImageResource(when(levelCode){
                LEVEL_CODE_0-> R.drawable.ic_vip_lv_tag0
                LEVEL_CODE_1-> R.drawable.ic_vip_lv_tag1
                LEVEL_CODE_2-> R.drawable.ic_vip_lv_tag2
                LEVEL_CODE_3-> R.drawable.ic_vip_lv_tag3
                LEVEL_CODE_4-> R.drawable.ic_vip_lv_tag4
                LEVEL_CODE_5-> R.drawable.ic_vip_lv_tag5
                LEVEL_CODE_6-> R.drawable.ic_vip_lv_tag6
                LEVEL_CODE_7-> R.drawable.ic_vip_lv_tag7
                LEVEL_CODE_8-> R.drawable.ic_vip_lv_tag8
                LEVEL_CODE_9-> R.drawable.ic_vip_lv_tag9
                LEVEL_CODE_10-> R.drawable.ic_vip_lv_tag10
                else-> R.color.transparent
            })
        }
    }

    fun getVipCard(level: Int): Int {
        return when(level) {
            0 -> R.drawable.bg_vip0
            1 -> R.drawable.bg_vip1
            2 -> R.drawable.bg_vip2
            3 -> R.drawable.bg_vip3
            4 -> R.drawable.bg_vip4
            5 -> R.drawable.bg_vip5
            6 -> R.drawable.bg_vip6
            7 -> R.drawable.bg_vip7
            8 -> R.drawable.bg_vip8
            9 -> R.drawable.bg_vip9
            10 -> R.drawable.bg_vip10
            else -> R.drawable.bg_vip0
        }
    }
}