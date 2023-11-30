package org.cxct.sportlottery.network.sport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
@KeepMembers
data class IndexResourceJsonResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val indexResourceList: IndexResourceList?
): BaseResult() {
    @KeepMembers
    data class IndexResourceList(
        val gameTypeResources: List<GameTypeResource>,
        val matchStatusResources: List<MatchStatusResource>
    ) {
        @KeepMembers
        data class GameTypeResource(
            val gameType: String,
            val playCates: List<PlayCate>,
            val playInfos: List<PlayInfo>,
            val playCateMenuDetails: List<PlayCateMenuDetail>
        ) {
            @KeepMembers
            data class PlayCate(
                val code: String,
                var nameMap: Map<String?, String?>,
                @Json(name = "supportOddsTypeSwitch")
                private val originalSupportOddsTypeSwitch: Int, //是否支持切换盘口, 0： 不支持， 1： 支持
                val isDynamicSpread: Int,
                val hasExtInfo: Int
            ) {
                /**
                 * 是否支持切换盘口, 0： 不支持， 1： 支持
                 */
                val supportOddsTypeSwitch = originalSupportOddsTypeSwitch == 1
            }

            @KeepMembers
            data class PlayInfo(
                val code: String,
                val nameMap: Map<String, String>
            )

            @KeepMembers
            data class PlayCateMenuDetail(
                val code: String,
                val nameMap: Map<String, String>
            )
        }

        @KeepMembers
        data class MatchStatusResource(
            val status: String,
            val nameMap: Map<String, String>
        )
    }
}
