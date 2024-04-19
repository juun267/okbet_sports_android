package org.cxct.sportlottery.common.enums

import org.cxct.sportlottery.R


object UserVipType{
    fun getLevelIcon(levelId: Int): Int{
         return when(levelId){
             1069-> R.drawable.ic_vip_lv0
             1070-> R.drawable.ic_vip_lv1
             1071-> R.drawable.ic_vip_lv2
             1072-> R.drawable.ic_vip_lv3
             1073-> R.drawable.ic_vip_lv4
             1074-> R.drawable.ic_vip_lv5
             1075-> R.drawable.ic_vip_lv6
             1076-> R.drawable.ic_vip_lv7
             1077-> R.drawable.ic_vip_lv8
             1078-> R.drawable.ic_vip_lv9
             1079-> R.drawable.ic_vip_lv10
             else-> R.drawable.ic_vip_lv0
         }
    }
    fun getLevelTagIcon(levelId: Int): Int{
        return when(levelId){
            1069-> R.drawable.ic_vip_lv_tag0
            1070-> R.drawable.ic_vip_lv_tag1
            1071-> R.drawable.ic_vip_lv_tag2
            1072-> R.drawable.ic_vip_lv_tag3
            1073-> R.drawable.ic_vip_lv_tag4
            1074-> R.drawable.ic_vip_lv_tag5
            1075-> R.drawable.ic_vip_lv_tag6
            1076-> R.drawable.ic_vip_lv_tag7
            1077-> R.drawable.ic_vip_lv_tag8
            1078-> R.drawable.ic_vip_lv_tag9
            1079-> R.drawable.ic_vip_lv_tag10
            else-> R.drawable.ic_vip_lv_tag0
        }
    }
}