package org.cxct.sportlottery.ui.withdraw

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize  //val payMaya: Boolean
data class TransferTypeAddSwitch(val bankTransfer: Boolean, val cryptoTransfer: Boolean, val walletTransfer: Boolean,) : Parcelable
