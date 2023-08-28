package org.cxct.sportlottery.network.index.config

import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
class HomeGameBean (val gameSort:String,val uniqueName:String,val status:Int)
//status =0 关闭  1开启