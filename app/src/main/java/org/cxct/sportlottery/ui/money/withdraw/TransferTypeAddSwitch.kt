package org.cxct.sportlottery.ui.money.withdraw

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize  //val payMaya: Boolean
@Keep
data class TransferTypeAddSwitch(
    val bankTransfer: Boolean,
    val cryptoTransfer: Boolean,
    val walletTransfer: Boolean,
    val paymataTransfer: Boolean,
) : Parcelable
