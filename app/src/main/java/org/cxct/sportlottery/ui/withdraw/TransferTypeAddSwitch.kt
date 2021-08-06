package org.cxct.sportlottery.ui.withdraw

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TransferTypeAddSwitch(val bankTransfer: Boolean, val cryptoTransfer: Boolean) : Parcelable
