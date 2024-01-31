package org.cxct.sportlottery.network.bet

import android.os.Parcel
import android.os.Parcelable
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.common.enums.ChannelType

@KeepMembers
data class FastBetDataBean (
    val matchType: MatchType,
    val gameType: GameType,
    val playCateCode: String? = "",
    val playCateName: String?,
    val matchInfo: MatchInfo,
    val matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd? = null,
    val odd: org.cxct.sportlottery.network.odds.Odd,
    val subscribeChannelType: ChannelType,
    val betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    val playCateMenuCode: String? = null,
    val otherPlayCateName: String? = null,
    var categoryCode: String? = null,
): Parcelable {
    constructor(parcel: Parcel) : this(
        TODO("matchType"),
        TODO("gameType"),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(MatchInfo::class.java.classLoader)!!,
        parcel.readParcelable(Odd::class.java.classLoader)!!,
        parcel.readParcelable(org.cxct.sportlottery.network.outright.odds.MatchOdd::class.java.classLoader)!!,
        TODO("subscribeChannelType"),
        TODO("betPlayCateNameMap"),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(playCateCode)
        parcel.writeString(playCateName)
        parcel.writeParcelable(matchInfo, flags)
        parcel.writeParcelable(odd, flags)
        parcel.writeString(playCateMenuCode)
        parcel.writeString(otherPlayCateName)
    }

    companion object CREATOR : Parcelable.Creator<FastBetDataBean> {
        override fun createFromParcel(parcel: Parcel): FastBetDataBean {
            return FastBetDataBean(parcel)
        }

        override fun newArray(size: Int): Array<FastBetDataBean?> {
            return arrayOfNulls(size)
        }
    }
}
